package com.plingtech.tagreader;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.plingtech.tagreader.databinding.FragmentTagsListBinding;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TagsListFragment extends Fragment {

    private static final String TAG = "TagListFrag";
    private FragmentTagsListBinding binding;
    private RecyclerView.LayoutManager layoutManager;
    TagViewModel mTagViewModel;
    public ScannedTagsAdapter adapter;
    public TagsListFragment tagsFrag;
    public MainActivity ma;
    private MediaPlayer mp;
    private TextView totalCountView;
    private int totalDistinct = 0;


    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState) {
        binding = FragmentTagsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        ma = (MainActivity)getActivity();
        tagsFrag = this;
        Log.d(TAG, "onCreateView complete");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated started");
        /*
        binding.buttonSecond.setOnClickListener(click -> {
            NavHostFragment.findNavController(TagsListFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
        });
        */
        Log.d(TAG, "binding.fab");
        binding.fabCopy.setOnClickListener(fabv -> {
                    try {
                        copyTagRfidsToClipboard();
                        Snackbar.make(
                                fabv,getString(R.string.clipboard_copy),
                                Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
        });

        Log.d(TAG, "MediaPlayer create");
        mp = MediaPlayer.create(ma, R.raw.slow_sabre);
        Log.d(TAG, "recyclerView binding");
        RecyclerView recyclerView = binding.tagList;
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        Log.d(TAG, "Setting up recycler adaptor");
        //adapter = new ScannedTagsAdapter(input);
        adapter = new ScannedTagsAdapter(getContext());
        recyclerView.setAdapter(adapter);
        mTagViewModel = new ViewModelProvider(this).get(TagViewModel.class);
        mTagViewModel.getTagsToView().observe(getViewLifecycleOwner(), tags -> {
            adapter.setTags(tags);
        });

        Log.d(TAG, "Setting up totalCountView");
        totalCountView = binding.totalCount;
        //totalCountView.setText(getString(R.string.no_tags_msg));
        mTagViewModel.getTagCount().observe(getViewLifecycleOwner(), count -> {
            if (count.equals(0)) totalCountView.setText(getString(R.string.no_tags_msg));
            else totalCountView.setText("Unique Tags: "+count.toString());
        });

        //Log.d(TAG, "Start BLE scan & connect");
        //ma.bt.scanBleDevices(tagsFrag);
        Log.d(TAG, "Scan, connect, subscribe, repeat");
        ma.bt.connectTagReader2(tagsFrag);
    }

    void tagItemDataBuild(String rfid) {
        String nlis = "No NLIS info";
        Date ts = addScannedTime();

        /*
        if (!adapter.alreadyScanned(rfid, ts)) {
            adapter.addTag(new ScannedTag(1, rfid, nlis, ts, decodeStockType(nlis)));
            totalCountView.setText(String.valueOf(++totalDistinct));
        }
        */
        mTagViewModel.insertTag(new ScannedTag(ts, ts, rfid, nlis, decodeStockType(nlis)));
        mp.start();
    }

    private Date addScannedTime() {  // Add scan time to tag object
        Locale l = Locale.getDefault();
        Log.i(TAG, "Locale: "+l );
        Date ts = Calendar.getInstance().getTime();
        Log.i(TAG, "Timestamp: "+ts );
        SimpleDateFormat dmy = new SimpleDateFormat("dd-MM-yyyy", l);
        Log.i(TAG, "Date: "+dmy.format(ts) );
        SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss", l);
        Log.i(TAG, "Time: "+hms.format(ts) );
        //return hms.format(ts);
        return ts;
    }

    private int decodeStockType(String nlis) {
        //Do NLIS lookup and decode nlis to stocktype, colour, ....
        return 0;
    }

    private void copyTagRfidsToClipboard() throws ExecutionException, InterruptedException {
        List<String> rfids = mTagViewModel.getAllRfid();
        Log.d(TAG,"rfids: "+rfids);
        ClipData cd;
        String rfidCopy = TextUtils.join("\n", rfids);
        cd = ClipData.newPlainText("text",rfidCopy);
        ma.cm.setPrimaryClip(cd);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //ma.tags = adapter.data;
        mp.stop(); // Stop media player
        binding = null;
    }

   //TODO Recreate list when Fragment is recreated but still in memory.

}
