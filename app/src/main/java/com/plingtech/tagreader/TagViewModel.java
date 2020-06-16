package com.plingtech.tagreader;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TagViewModel extends AndroidViewModel {

    private static final String TAG = "TagsViewModel";
    private TagsRepository mRepository;
    //private LiveData<List<ScannedTag>> mAllTags;
    private LiveData<List<TagView>> mTagsToView;
    private LiveData<Integer> mTagCount;

    public TagViewModel (Application application) {
        super(application);
        mRepository = new TagsRepository(application);
        mTagsToView = mRepository.getTagsToView();
        mTagCount = mRepository.getTagCount();
    }

    LiveData<List<TagView>> getTagsToView() { return mTagsToView; }

    LiveData<Integer> getTagCount() { return mTagCount; }

    List<ScannedTag> getAllTags() throws ExecutionException, InterruptedException { return mRepository.getAllTags(); }

    List<String> getAllRfid() throws ExecutionException, InterruptedException { return mRepository.getAllRfid(); }

    public void insertTag (ScannedTag tag) {
        Log.d(TAG, "Adding tag: "+tag.toString());
        mRepository.insert(tag);
    }

    public void deleteAllTags() {
        Log.d(TAG, "Deleting All Tags");
        mRepository.deleteAllTags();
    }

    /*
    boolean alreadyScanned(String rfid, String ts) {
        Iterator<ScannedTag> it = mRepository.getAllTags().iterator();
        while (it.hasNext()) {
            ScannedTag t = it.next();
            if (t.getRfid().equals(rfid)) {
                t.incrementCount();
                t.setTimestamp(ts);
                it.remove();
                insertTag(t);
                return true;
            }
        }
        return false;
    }
    */

}
