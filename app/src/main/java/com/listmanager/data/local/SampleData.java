package com.listmanager.data.local;

import com.listmanager.model.ListCategory;

import java.util.ArrayList;
import java.util.List;

public final class SampleData {

    private SampleData() {
    }

    public static List<ListItemEntity> createInboxItems() {
        List<ListItemEntity> items = new ArrayList<>();
        items.add(create("1", "Plan weekly meals",
                "Draft a menu and shopping list for the week.", 0));
        items.add(create("2", "Book dentist appointment",
                "Schedule a check-up before the end of the month.", 100));
        items.add(create("3", "Organize desk",
                "Sort papers, recycle old notes, and tidy cables.", 200));
        items.add(create("4", "Call Alex",
                "Follow up on the project timeline and next steps.", 300));
        items.add(create("5", "Read saved article",
                "Finish the long-form piece saved from last week.", 400));
        items.add(create("6", "Update budget",
                "Review subscriptions and adjust monthly categories.", 500));
        return items;
    }

    private static ListItemEntity create(String id, String title, String description, long sortOrder) {
        return new ListItemEntity(
                id,
                title,
                description,
                ListCategory.INBOX.name(),
                sortOrder
        );
    }
}
