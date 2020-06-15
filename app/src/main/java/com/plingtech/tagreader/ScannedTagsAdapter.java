package com.plingtech.tagreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plingtech.tagreader.databinding.RecyclerViewItemBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class ScannedTagsAdapter extends RecyclerView.Adapter<ScannedTagsAdapter.TagsViewHolder> {

    static class TagsViewHolder extends RecyclerView.ViewHolder {

        //ImageView stockType;
        TextView count;
        TextView tagRfid;
        TextView tagNlis;
        TextView scanTime;

        private TagsViewHolder(RecyclerViewItemBinding tagBinding) {
            super(tagBinding.getRoot());
            count = tagBinding.scanCount;
            tagRfid = tagBinding.tagRfid;
            tagNlis = tagBinding.tagNlis;
            scanTime = tagBinding.scanTime;
            //stockType = tagBinding.stockType;
            // if (stockType == ScannedTag.CATTLE)  holder.line1.setText(String.format("MOO", item.description));
        }
    }

    private static final String TAG = "TagsAdapter";
    private final LayoutInflater mInflator;
    private List<ScannedTag> data = new ArrayList<>();
    private boolean emptyList = true;

    ScannedTagsAdapter(Context context) { mInflator = LayoutInflater.from(context); }

    @Override
    public ScannedTagsAdapter.TagsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerViewItemBinding tagBinding = RecyclerViewItemBinding.inflate(mInflator, parent, false);
        return new TagsViewHolder(tagBinding);
    }

    @Override
    public void onBindViewHolder(TagsViewHolder holder, int position) {
        if (data != null) {
            ScannedTag tag = data.get(position);
            holder.tagRfid.setText(tag.getRfid());
            holder.scanTime.setText(tag.getTimestamp().toString());
            //holder.count.setText(String.valueOf(tag.getCount()));
        } else {
            holder.tagRfid.setText("No Tags Scanned");
        }
    }

    void setTags(List<ScannedTag> tags) {
        data = tags;
        notifyDataSetChanged();
    }

    public List<ScannedTag> getAllTags() {
        return data;
    }

    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        else return 0;
    }

    //To copy RFIDs to clipboard
    List<String> getAllTagRfids() {
        List<String> rfids = new ArrayList<>();
        for (ScannedTag t : data) {
            Log.d(TAG, "RFID get: "+t.getRfid());
            rfids.add(t.getRfid());
        }
        Log.d(TAG,"All Tags: "+rfids);
        return rfids;
    }

    //Pre LiveData & Room DB
    /*
    ScannedTagsAdapter(List<ScannedTag> tags) {
        //for (ScannedTag t : tags) data.add(t);
        data.addAll(tags);
    }

    @NonNull
    @Override
    public ScannedTagsAdapter.TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerViewItemBinding tagBinding = RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TagsViewHolder(tagBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        final ScannedTag tag = data.get(position);
        holder.tagRfid.setText(tag.getTagRfid());
        holder.scanTime.setText(tag.getTimestamp());
        holder.count.setText(String.valueOf(tag.getCount()));
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

    boolean alreadyScanned(String rfid, String ts) {
        Iterator<ScannedTag> it = data.iterator();
        while (it.hasNext()) {
            ScannedTag t = it.next();
            if (t.getRfid().equals(rfid)) {
                t.incrementCount();
                t.setTimestamp(ts);
                it.remove();
                addTag(t);
                return true;
            }
        }
        return false;
    }

    void clearTags() {
        data.clear();
        notifyDataSetChanged();
    }
    */
}
