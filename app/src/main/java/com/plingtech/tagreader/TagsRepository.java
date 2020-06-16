package com.plingtech.tagreader;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TagsRepository {

    private ScannedTagDao mTagDao;
    private LiveData<List<TagView>> mTagsToView;
    private LiveData<Integer> mTagCount;

    TagsRepository(Application application) {
        TagsDatabase db = TagsDatabase.getDatabase(application);
        mTagDao = db.scannedTagDao();
        mTagsToView = mTagDao.getTagsToView();
        mTagCount = mTagDao.getTagCount();
    }

    //LiveData<List<ScannedTag>> getAllTags() { return mAllTags; }

    LiveData<List<TagView>> getTagsToView() { return mTagsToView; }

    LiveData<Integer> getTagCount() { return mTagCount; }

    void insert(ScannedTag tag) {
        TagsDatabase.databaseExecutor.execute(() -> {
            mTagDao.insertTag(tag);
        });
    }

    void deleteAllTags() {
        TagsDatabase.databaseExecutor.execute(() -> {
            mTagDao.deleteAllTags();
        });
    }

    public List<ScannedTag> getAllTags() throws ExecutionException, InterruptedException {
        Future<List<ScannedTag>> allTags = TagsDatabase.databaseExecutor.submit(() -> mTagDao.getAllTags());
        return allTags.get();
    }

    public List<String> getAllRfid() throws ExecutionException, InterruptedException {
        Future<List<String>> getAllRfids = TagsDatabase.databaseExecutor.submit(() -> mTagDao.getAllRfid());
        return getAllRfids.get();
    }

}
