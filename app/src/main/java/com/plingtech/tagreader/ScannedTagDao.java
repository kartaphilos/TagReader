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
    void deleteAllTags();

    @Query("SELECT COUNT(rfid), rfid, time_scanned FROM tags_table GROUP BY rfid ORDER BY time_scanned ASC")
    LiveData<List<TagView>> getTagsToView();

    @Query("SELECT COUNT(DISTINCT rfid) FROM tags_table;")
    LiveData<Integer> getTagCount();

    @Query("SELECT * FROM tags_table ORDER BY time_scanned ASC")
    List<ScannedTag> getAllTags();

    @Query("SELECT rfid FROM tags_table GROUP BY rfid")
    List<String> getAllRfid();  //For clipboard copy
}
