package com.plingtech.tagreader;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database( entities = {ScannedTag.class}, version = 2, exportSchema = false)
@TypeConverters({DataConverters.class})
public abstract class TagsDatabase extends RoomDatabase {

    public abstract ScannedTagDao scannedTagDao();

    private static volatile TagsDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static TagsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TagsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TagsDatabase.class, "tags_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
