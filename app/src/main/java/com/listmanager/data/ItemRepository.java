package com.listmanager.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.listmanager.data.local.AppDatabase;
import com.listmanager.data.local.ListItemDao;
import com.listmanager.data.local.ListItemEntity;
import com.listmanager.data.local.SampleData;
import com.listmanager.model.ListCategory;
import com.listmanager.model.ListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ItemRepository {

    private final ListItemDao dao;
    private final ExecutorService executor;

    public ItemRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        dao = database.listItemDao();
        executor = AppDatabase.getWriteExecutor();
    }

    public LiveData<List<ListItem>> observeItems(ListCategory category) {
        return Transformations.map(
                dao.observeItems(category.name()),
                this::mapEntities
        );
    }

    public void moveItem(ListCategory from, int index, ListCategory to) {
        executor.execute(() -> {
            List<ListItemEntity> sourceItems = dao.getItemsSync(from.name());
            if (index < 0 || index >= sourceItems.size()) {
                return;
            }

            ListItemEntity entity = sourceItems.get(index);
            entity.category = to.name();
            entity.sortOrder = nextSortOrder(to);
            dao.update(entity);
        });
    }

    public void deleteItem(ListCategory from, int index) {
        executor.execute(() -> {
            List<ListItemEntity> sourceItems = dao.getItemsSync(from.name());
            if (index < 0 || index >= sourceItems.size()) {
                return;
            }
            dao.deleteById(sourceItems.get(index).id);
        });
    }

    public void insertItem(ListCategory category, int insertIndex, ListItem item) {
        final int targetIndex = Math.max(0, insertIndex);
        executor.execute(() -> {
            List<ListItemEntity> items = dao.getItemsSync(category.name());
            int clampedIndex = targetIndex;
            if (clampedIndex > items.size()) {
                clampedIndex = items.size();
            }
            long sortOrder = sortOrderForInsert(items, clampedIndex, category);
            dao.insert(new ListItemEntity(
                    item.getId(),
                    item.getTitle(),
                    item.getDescription(),
                    category.name(),
                    sortOrder
            ));
        });
    }

    public void updateItem(String id, String title, String description) {
        executor.execute(() -> {
            ListItemEntity entity = dao.getById(id);
            if (entity == null) {
                return;
            }
            entity.title = title;
            entity.description = description;
            dao.update(entity);
        });
    }

    public void deleteItemById(String id) {
        executor.execute(() -> dao.deleteById(id));
    }

    public void resetSampleItems() {
        executor.execute(() -> {
            dao.deleteAll();
            dao.insertAll(SampleData.createInboxItems());
        });
    }

    private long nextSortOrder(ListCategory category) {
        Long minSortOrder = dao.getMinSortOrder(category.name());
        if (minSortOrder == null) {
            return System.currentTimeMillis();
        }
        return minSortOrder - 1;
    }

    private long sortOrderForInsert(List<ListItemEntity> items, int insertIndex, ListCategory category) {
        if (items.isEmpty() || insertIndex <= 0) {
            return nextSortOrder(category);
        }
        if (insertIndex >= items.size()) {
            return items.get(items.size() - 1).sortOrder + 1;
        }
        long previous = items.get(insertIndex - 1).sortOrder;
        long next = items.get(insertIndex).sortOrder;
        if (next - previous > 1) {
            return previous + ((next - previous) / 2);
        }
        return next - 1;
    }

    private List<ListItem> mapEntities(List<ListItemEntity> entities) {
        List<ListItem> items = new ArrayList<>();
        if (entities == null) {
            return items;
        }
        for (ListItemEntity entity : entities) {
            items.add(new ListItem(entity.id, entity.title, entity.description));
        }
        return items;
    }
}
