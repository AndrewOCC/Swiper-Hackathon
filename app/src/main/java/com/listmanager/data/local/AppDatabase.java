package com.listmanager.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ListItemEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract ListItemDao listItemDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    AppDatabase database = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "list-manager.db")
                            .build();
                    databaseWriteExecutor.execute(() -> seedIfEmpty(database));
                    instance = database;
                }
            }
        }
        return instance;
    }

    public static ExecutorService getWriteExecutor() {
        return databaseWriteExecutor;
    }

    private static void seedIfEmpty(AppDatabase database) {
        ListItemDao dao = database.listItemDao();
        if (dao.getCount() == 0) {
            dao.insertAll(SampleData.createInboxItems());
        }
    }
}
