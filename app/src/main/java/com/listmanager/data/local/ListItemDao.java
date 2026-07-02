package com.listmanager.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ListItemDao {

    @Query("SELECT * FROM list_items WHERE category = :category ORDER BY sortOrder ASC, title ASC")
    LiveData<List<ListItemEntity>> observeItems(String category);

    @Query("SELECT * FROM list_items WHERE category = :category ORDER BY sortOrder ASC, title ASC")
    List<ListItemEntity> getItemsSync(String category);

    @Query("SELECT MIN(sortOrder) FROM list_items WHERE category = :category")
    Long getMinSortOrder(String category);

    @Query("SELECT COUNT(*) FROM list_items")
    int getCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ListItemEntity> items);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ListItemEntity item);

    @Update
    void update(ListItemEntity item);

    @Query("SELECT * FROM list_items WHERE id = :id LIMIT 1")
    ListItemEntity getById(String id);

    @Query("DELETE FROM list_items WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM list_items")
    void deleteAll();
}
