package com.listmanager.data;

import com.listmanager.model.ListCategory;
import com.listmanager.model.ListItem;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ItemRepository {

    private final Map<ListCategory, List<ListItem>> lists = new EnumMap<>(ListCategory.class);

    public ItemRepository() {
        for (ListCategory category : ListCategory.values()) {
            lists.put(category, new ArrayList<>());
        }
        seedSampleItems();
    }

    public List<ListItem> getItems(ListCategory category) {
        return new ArrayList<>(lists.get(category));
    }

    public void moveItem(ListCategory from, int index, ListCategory to) {
        List<ListItem> source = lists.get(from);
        if (index < 0 || index >= source.size()) {
            return;
        }

        ListItem item = source.remove(index);
        lists.get(to).add(0, item);
    }

    public void deleteItem(ListCategory from, int index) {
        List<ListItem> source = lists.get(from);
        if (index >= 0 && index < source.size()) {
            source.remove(index);
        }
    }

    public void resetSampleItems() {
        for (ListCategory category : ListCategory.values()) {
            lists.get(category).clear();
        }
        seedSampleItems();
    }

    private void seedSampleItems() {
        List<ListItem> inbox = lists.get(ListCategory.INBOX);
        inbox.add(new ListItem("1", "Plan weekly meals", "Draft a menu and shopping list for the week."));
        inbox.add(new ListItem("2", "Book dentist appointment", "Schedule a check-up before the end of the month."));
        inbox.add(new ListItem("3", "Organize desk", "Sort papers, recycle old notes, and tidy cables."));
        inbox.add(new ListItem("4", "Call Alex", "Follow up on the project timeline and next steps."));
        inbox.add(new ListItem("5", "Read saved article", "Finish the long-form piece saved from last week."));
        inbox.add(new ListItem("6", "Update budget", "Review subscriptions and adjust monthly categories."));
    }
}
