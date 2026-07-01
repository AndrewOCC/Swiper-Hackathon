package com.listmanager.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "list_items")
public class ListItemEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String title;

    @NonNull
    public String description;

    @NonNull
    public String category;

    public long sortOrder;

    public ListItemEntity(@NonNull String id,
                          @NonNull String title,
                          @NonNull String description,
                          @NonNull String category,
                          long sortOrder) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.sortOrder = sortOrder;
    }
}
