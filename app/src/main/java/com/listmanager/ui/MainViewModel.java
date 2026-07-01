package com.listmanager.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.listmanager.data.ItemRepository;
import com.listmanager.model.ListCategory;
import com.listmanager.model.ListItem;

import java.util.List;

public class MainViewModel extends ViewModel {

    private final ItemRepository repository;
    private final MutableLiveData<ListCategory> currentCategory = new MutableLiveData<>(ListCategory.INBOX);
    private final MutableLiveData<List<ListItem>> visibleItems = new MutableLiveData<>();

    public MainViewModel() {
        repository = new ItemRepository();
        publishCurrentList();
    }

    public LiveData<ListCategory> getCurrentCategory() {
        return currentCategory;
    }

    public LiveData<List<ListItem>> getVisibleItems() {
        return visibleItems;
    }

    public void selectCategory(ListCategory category) {
        currentCategory.setValue(category);
        publishCurrentList();
    }

    public void swipeLeft(int position) {
        ListCategory category = currentCategory.getValue();
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
                repository.moveItem(ListCategory.STARRED, position, ListCategory.INBOX);
                break;
            default:
                break;
        }

        publishCurrentList();
    }

    public void swipeRight(int position) {
        ListCategory category = currentCategory.getValue();
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

        publishCurrentList();
    }

    public void refreshItems() {
        repository.resetSampleItems();
        currentCategory.setValue(ListCategory.INBOX);
        publishCurrentList();
    }

    private void publishCurrentList() {
        ListCategory category = currentCategory.getValue();
        if (category == null) {
            category = ListCategory.INBOX;
        }
        visibleItems.setValue(repository.getItems(category));
    }
}
