package com.listmanager.model;

import java.util.Objects;
import java.util.UUID;

public class ListItem {

    private final String id;
    private final String title;
    private final String description;

    public ListItem(String title, String description) {
        this(UUID.randomUUID().toString(), title, description);
    }

    public ListItem(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ListItem)) {
            return false;
        }
        ListItem listItem = (ListItem) other;
        return Objects.equals(id, listItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
