package com.plingtech.tagreader;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.plingtech.tagreader.databinding.FragmentTagsListBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TagsListFragment extends Fragment {

    private static final String TAG = "TagListFrag";
    private FragmentTagsListBinding binding;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    public ScannedTagsAdapter adapter;
    public TagsListFragment tagsFrag;
    public MainActivity ma;

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
        binding.buttonSecond.setOnClickListener(click -> {
            NavHostFragment.findNavController(TagsListFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
        });
        Log.d(TAG, "binding.fab");
        binding.fabCopy.setOnClickListener(fabv -> {
                            copyTagRfidToClipboard();
                            Snackbar.make(fabv,
                                       "Scanned Tags copied to clipboard", Snackbar.LENGTH_LONG)
                                      .setAction("Action", null).show();
                            }
        );

        /*
        fabv -> Snackbar.make(fabv,
                "Scanned Tags copied to clipboard", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

         */


        Log.d(TAG, "Start BLE scan & connect");
        //TODO: Make observable and subscribe to result for device &/or connection
        ma.bt.scanBleDevices(tagsFrag);

        Log.d(TAG, "recyclerView binding");
        recyclerView = binding.tagList;
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        List<ScannedTag> input = new ArrayList<>();
        ScannedTag notags = new ScannedTag(getString(R.string.no_tags_msg), "", "00:00",0);
        Log.d(TAG,"notag: "+notags.toString());
        input.add(notags);
        Log.d(TAG, "setting adaptor");
        adapter = new ScannedTagsAdapter(input);
        recyclerView.setAdapter(adapter);
    }

    public void tagItemDataBuild (String rfid) {
        String nlis = "";

        ScannedTag t = new ScannedTag(rfid,
                                    nlis,
                                    addScannedTime(),
                                    decodeStockType(nlis)
        );
        adapter.addScanResult(t);

    }

    private String addScannedTime() {  // Add scan time to tag object
        Locale l = Locale.getDefault();
        Log.i(TAG, "Locale: "+l );
        Date ts = Calendar.getInstance().getTime();
        Log.i(TAG, "Timestamp: "+ts );
        SimpleDateFormat dmy = new SimpleDateFormat("dd-MM-yyyy", l);
        Log.i(TAG, "Date: "+dmy.format(ts) );
        SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss", l);
        Log.i(TAG, "Time: "+hms.format(ts) );
        return hms.format(ts);
    }

    private int decodeStockType(String nlis) {
        //Do NLIS lookup and decode nlis to stocktype, colour, ....
        return 0;
    }

    private void copyTagRfidToClipboard() {
        List<String> rfids = adapter.getAllTagRfids();
        ClipData cd;
        String rfidCopy = TextUtils.join("\n", rfids);
        cd = ClipData.newPlainText("text",rfidCopy);
        ma.cm.setPrimaryClip(cd);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //ma.tags = adapter.data;
        binding = null;
    }

   //TODO Recreate list when Fragement is recreated but still in memory.

}
