package com.listmanager.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.listmanager.data.ItemRepository;
import com.listmanager.model.ListCategory;
import com.listmanager.model.ListItem;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final ItemRepository repository;
    private final MutableLiveData<ListCategory> currentCategory = new MutableLiveData<>(ListCategory.INBOX);
    private final LiveData<List<ListItem>> visibleItems;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ItemRepository(application);
        visibleItems = Transformations.switchMap(
                currentCategory,
                category -> repository.observeItems(
                        category != null ? category : ListCategory.INBOX
                )
        );
    }

    public LiveData<ListCategory> getCurrentCategory() {
        return currentCategory;
    }

    public LiveData<List<ListItem>> getVisibleItems() {
        return visibleItems;
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

    public void swipeLeft(int position) {
        ListCategory category = getCurrentCategoryValue();

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

    public void swipeRight(int position) {
        ListCategory category = getCurrentCategoryValue();

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
    }
}
