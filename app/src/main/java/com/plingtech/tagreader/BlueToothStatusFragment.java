package com.plingtech.tagreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.plingtech.tagreader.databinding.FragmentBlueToothStatusBinding;
import com.polidea.rxandroidble2.RxBleConnection;

import io.reactivex.disposables.Disposable;

public class BlueToothStatusFragment extends DialogFragment {
    public MainActivity ma;
    private FragmentBlueToothStatusBinding binding;

    public BlueToothStatusFragment() {
        // Required empty public constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String TAG = "BT Status Dialog";
        ma = (MainActivity)getActivity();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        //inflater.inflate(R.layout.fragment_blue_tooth_status, null)

        binding = FragmentBlueToothStatusBinding.inflate(LayoutInflater.from(ma));
        builder.setView(binding.getRoot())
                .setMessage(R.string.bt_status)
                .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Close
                    }
                })
        ;

        // Set Text with BT device info & rssi
        if(ma.bt.getBleDevice() == null) {
            binding.btDeviceName.setText("No Device Detected");
        }
        else {
            binding.btDeviceName.setText(ma.bt.getBleDeviceName());
            binding.btDeviceMac.setText(ma.bt.getBleDeviceMac());
            Disposable connDisposable = ma.bt.subscribeBtConnState().subscribe(c -> binding.btConnStatus.setText(c.toString()), throwable -> Log.d(TAG, "RSSI observable error: "+throwable));
        }

        if (ma.bt.getCurrentConnState() == RxBleConnection.RxBleConnectionState.CONNECTED) {
            Disposable rssidisposable = ma.bt.subscribeRssi().subscribe(i -> binding.btRssi.setText(i.toString()), throwable -> Log.d(TAG, "RSSI observable error: "+throwable));
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlueToothStatusFragment.
     */
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // TODO: Rename and change types and number of parameters
    public static BlueToothStatusFragment newInstance(String param1, String param2) {
        BlueToothStatusFragment fragment = new BlueToothStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blue_tooth_status, container, false);
    }
}
