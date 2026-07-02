package com.listmanager.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.listmanager.data.ItemRepository;
import com.listmanager.model.ListCategory;
import com.listmanager.model.ListItem;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final ItemRepository repository;
    private final MutableLiveData<ListCategory> currentCategory = new MutableLiveData<>(ListCategory.INBOX);
    private final MutableLiveData<String> editingItemId = new MutableLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ItemRepository(application);
    }

    public LiveData<ListCategory> getCurrentCategory() {
        return currentCategory;
    }

    public LiveData<String> getEditingItemId() {
        return editingItemId;
    }

    public String getEditingItemIdValue() {
        return editingItemId.getValue();
    }

    public LiveData<List<ListItem>> observeItems(ListCategory category) {
        return repository.observeItems(category);
    }

    public ListCategory getCurrentCategoryValue() {
        ListCategory category = currentCategory.getValue();
        return category != null ? category : ListCategory.INBOX;
    }

    public void selectCategory(ListCategory category) {
        currentCategory.setValue(category);
    }

    public void selectNextPanel() {
        ListCategory category = currentCategory.getValue();
        if (category != null) {
            selectCategory(category.nextPanel());
        }
    }

    public void selectPreviousPanel() {
        ListCategory category = currentCategory.getValue();
        if (category != null) {
            selectCategory(category.previousPanel());
        }
    }

    public void swipeLeft(ListCategory category, int position) {
        if (category == null) {
            return;
        }

        switch (category) {
            case INBOX:
                repository.moveItem(ListCategory.INBOX, position, ListCategory.ARCHIVED);
                break;
            case ARCHIVED:
                repository.deleteItem(ListCategory.ARCHIVED, position);
                break;
            case STARRED:
                repository.moveItem(ListCategory.STARRED, position, ListCategory.INBOX);
                break;
            default:
                break;
        }
    }

    public void swipeRight(ListCategory category, int position) {
        if (category == null) {
            return;
        }

        switch (category) {
            case INBOX:
                repository.moveItem(ListCategory.INBOX, position, ListCategory.STARRED);
                break;
            case ARCHIVED:
                repository.moveItem(ListCategory.ARCHIVED, position, ListCategory.INBOX);
                break;
            case STARRED:
                break;
            default:
                break;
        }
    }

    public void refreshItems() {
        repository.resetSampleItems();
        currentCategory.setValue(ListCategory.INBOX);
        editingItemId.setValue(null);
    }

    public ListItem createItemAt(ListCategory category, int insertIndex) {
        ListItem item = new ListItem("", "");
        repository.insertItem(category, insertIndex, item);
        editingItemId.setValue(item.getId());
        return item;
    }

    public void saveItem(String id, String title, String description) {
        String trimmedTitle = title != null ? title.trim() : "";
        String trimmedDescription = description != null ? description.trim() : "";
        if (trimmedTitle.isEmpty() && trimmedDescription.isEmpty()) {
            repository.deleteItemById(id);
        } else {
            repository.updateItem(id, trimmedTitle, trimmedDescription);
        }
        if (id.equals(editingItemId.getValue())) {
            editingItemId.setValue(null);
        }
    }

    public void startEditingItem(@NonNull String itemId) {
        editingItemId.setValue(itemId);
    }

    public void clearEditingItem() {
        editingItemId.setValue(null);
    }

    public void receiveSharedText(@NonNull String title, @NonNull String description) {
        repository.insertItem(ListCategory.INBOX, 0, new ListItem(title, description));
        currentCategory.setValue(ListCategory.INBOX);
    }

    public void deleteItem(String id) {
        if (id == null) {
            return;
        }
        if (id.equals(editingItemId.getValue())) {
            editingItemId.setValue(null);
        }
        repository.deleteItemById(id);
    }
}
