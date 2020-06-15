package com.plingtech.tagreader;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.plingtech.tagreader.ScannedTag;

import java.nio.channels.SelectableChannel;
import java.util.List;

@Dao
public interface ScannedTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTag(ScannedTag tag);

    @Query("DELETE FROM tags_table")
    void deleteAll();

    @Query("SELECT * FROM tags_table ORDER BY timestamp ASC")
    LiveData<List<ScannedTag>> getAllTags();

    @Query("SELECT rfid FROM tags_table GROUP BY rfid")
    List<String> getAllRfid();
}
