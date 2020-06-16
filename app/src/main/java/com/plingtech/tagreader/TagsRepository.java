package com.plingtech.tagreader;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    void insert(ScannedTag tag) {
        TagsDatabase.databaseExecutor.execute(() -> {
            mTagDao.insertTag(tag);
        });
    }

    public List<String> getAllRfid() throws ExecutionException, InterruptedException {
        Future<List<String>> getAllRfids = TagsDatabase.databaseExecutor.submit(() -> mTagDao.getAllRfid());
        return getAllRfids.get();
    }

}
