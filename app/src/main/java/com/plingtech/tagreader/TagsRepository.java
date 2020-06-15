package com.plingtech.tagreader;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TagsRepository {

    private ScannedTagDao mTagDao;
    private LiveData<List<ScannedTag>> mAllTags;

    TagsRepository(Application application) {
        TagsDatabase db = TagsDatabase.getDatabase(application);
        mTagDao = db.scannedTagDao();
        mAllTags = mTagDao.getAllTags();
    }

    LiveData<List<ScannedTag>> getAllTags() {
        return mAllTags;
    }

    List<String> getAllRfid() { return mTagDao.getAllRfid(); }

    void insert(ScannedTag tag) {
        TagsDatabase.databaseWriteExecutor.execute(() -> {
            mTagDao.insertTag(tag);
        });
    }
}
