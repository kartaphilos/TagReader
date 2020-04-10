package com.plingtech.tagreader;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tbruyelle.rxpermissions2.RxPermissions;

public class TagsListFragment extends Fragment {

    private static final String TAG = "TagListFrag";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tags_list, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(TagsListFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        /* Log.i(TAG, "starting permissions");
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);
        rxPermissions
                .request(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        Log.i(TAG, "Permission accepted ");
                    } else { // Permission denied
                        Log.i(TAG, "Permission denied ");
                    }
                });

         */

    }
}
