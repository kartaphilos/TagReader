package com.plingtech.tagreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plingtech.tagreader.databinding.RecyclerViewItemBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


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
            //tagNlis = tagBinding.tagNlis;
            scanTime = tagBinding.scanTime;
            //stockType = tagBinding.stockType;
            // if (stockType == ScannedTag.CATTLE)  holder.line1.setText(String.format("MOO", item.description));
        }
    }

    private static final String TAG = "TagsAdapter";
    private final LayoutInflater mInflator;
    private List<TagView> tags = new ArrayList<>();

    ScannedTagsAdapter(Context context) { mInflator = LayoutInflater.from(context); }

    @Override
    public ScannedTagsAdapter.TagsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerViewItemBinding tagBinding = RecyclerViewItemBinding.inflate(mInflator, parent, false);
        return new TagsViewHolder(tagBinding);
    }

    @Override
    public void onBindViewHolder(TagsViewHolder holder, int position) {
        if (tags != null) {
            TagView tag = tags.get(position);
            holder.count.setText(String.valueOf(tag.getScanCount()));
            holder.tagRfid.setText(tag.getRfid());
            SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            holder.scanTime.setText(hms.format(tag.getTimeScanned()));
        } else {
            holder.tagRfid.setText("No Tags Scanned");
        }
    }

    void setTags(List<TagView> t) {
        tags = t;
        notifyDataSetChanged();
    }

    public List<TagView> getTagsToView() {
        return tags;
    }

    @Override
    public int getItemCount() {
        if (tags != null)
            return tags.size();
        else return 0;
    }

}
