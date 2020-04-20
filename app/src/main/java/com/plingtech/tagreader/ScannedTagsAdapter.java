package com.plingtech.tagreader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plingtech.tagreader.databinding.RecyclerViewItemBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

class ScannedTagsAdapter extends RecyclerView.Adapter<ScannedTagsAdapter.ViewHolder> {

    public boolean alreadyScanned(String rfid, String ts) {
        Iterator<ScannedTag> it = data.iterator();
        while (it.hasNext()) {
            ScannedTag t = it.next();
            if (t.getTagRfid() == rfid) {
                t.incrementCount();
                t.setTimestamp(ts);
                it.remove();
                addTag(t);
                return true;
            }
        }
        return false;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        //ImageView stockType;
        TextView tagRfid;
        TextView tagNlis;
        TextView scanTime;

        ViewHolder(RecyclerViewItemBinding tagBinding) {
            super(tagBinding.getRoot());
            tagRfid = tagBinding.tagRfid;
            tagNlis = tagBinding.tagNlis;
            //stockType = tagBinding.stockType;
            scanTime = tagBinding.scanTime;
        }
    }

    //if (stockType == ScannedTag.CATTLE)  holder.line1.setText(String.format("MOO", item.description));
    private static final String TAG = "Adapter";
    private final List<ScannedTag> data = new ArrayList<>();
    private boolean emptyList = true;

    public ScannedTagsAdapter(List<ScannedTag> tags) {
        //for (ScannedTag t : tags) data.add(t);
        data.addAll(tags);
    }

    void addTag(ScannedTag tag) {
        Log.d(TAG, "Adding tag to recycler view: "+tag.toString());
        if (emptyList) {
            Log.d(TAG,"Empty List - clearing fake tag");
            emptyList = false;
            data.clear();
        }
        data.add(tag);
        notifyDataSetChanged();
    }

    public List<ScannedTag> getAllTags() {
        return data;
    }

    public List<String> getAllTagRfids() {
        List<String> rfids = new ArrayList<>();
        for (ScannedTag t : data) {
            Log.d(TAG, "RFID get: "+t.getTagRfid());
            rfids.add(t.getTagRfid());
        }
        Log.d(TAG,"All Tags: "+rfids);
        return rfids;
    }

    void clearScanResults() {
        data.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ScannedTag tag = data.get(position);
        holder.tagRfid.setText(tag.getTagRfid());
        holder.scanTime.setText(tag.getTimestamp());
    }

    @NonNull
    @Override
    public ScannedTagsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerViewItemBinding tagBinding = RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(tagBinding);
    }

}
