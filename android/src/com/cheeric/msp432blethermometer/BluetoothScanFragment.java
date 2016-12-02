/*
 * Copyright (c) 2016 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.cheeric.msp432blethermometer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnBluetoothScanFragmentListener} interface
 * to handle interaction events.
 * Use the {@link BluetoothScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BluetoothScanFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String EXAMPLE_NAME = "exampleName";

    // TODO: Rename and change types of parameters
    private String exampleName;

    private OnBluetoothScanFragmentListener mListener;


    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private Runnable mRunnable;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothDevice btDevice;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private BluetoothDeviceListAdapter mBluetoothDeviceAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
//    private ProgressDialog dialog;


    public BluetoothScanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param example Parameter 1.
     * @return A new instance of fragment BluetoothScanFragment.
     */
    public static BluetoothScanFragment newInstance(String example) {
        BluetoothScanFragment fragment = new BluetoothScanFragment();
        Bundle args = new Bundle();
        args.putString(EXAMPLE_NAME, example);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mHandler.removeCallbacks(mRunnable);
        mSwipeRefreshLayout.setRefreshing(false);
        mLEScanner.stopScan(mScanCallback);
        btDevice = ((BluetoothDevice) (l.getItemAtPosition(position)));
        String clicked = btDevice.getName();
        if (clicked == null)
            clicked = "Unknown Device";
        Log.d("Selected ", clicked);

        // Switch to example fragment
//        if (clicked.equals(getArguments().getString(EXAMPLE_NAME)) && mListener != null) {
        if (mListener != null) {
//            mGatt = btDevice.connectGatt(getActivity(), false, gattCallback);
//            dialog = ProgressDialog.show(getActivity(), "", "Loading. Please wait...", true);
//            dialog.setCancelable(true);
            mListener.onBluetoothDeviceSelected(btDevice);
        }
    }

//    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            Log.d("BluetoothScanFragment", "Connection State Changed!!!");
//            super.onConnectionStateChange(gatt, status, newState);
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                gatt.discoverServices();
//            }
//        }
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            dialog.dismiss();
//            super.onServicesDiscovered(gatt, status);
//            Log.d("BluetoothScanFragment", "services discovered, status: " + status);
//            List<BluetoothGattService> services = mGatt.getServices();
//            for (BluetoothGattService s : services) {
//                if (s.getUuid().toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb"))
//                    mListener.onBluetoothDeviceSelected(btDevice);
//            }
//        }
//    };

            @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBluetoothScanFragmentListener) {
            mListener = (OnBluetoothScanFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBluetoothScanFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exampleName = getArguments().getString(EXAMPLE_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth_scan, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("BluetoothScanFragment", "onActivityCreated()");

        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mSwipeRefreshLayout = (SwipeRefreshLayout)getActivity().findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        startScan();
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Select BLE Device...");
        mHandler.removeCallbacks(mRunnable);
        mSwipeRefreshLayout.setRefreshing(false);
        if (mLEScanner != null)
            mLEScanner.stopScan(mScanCallback);
        Log.d("BluetoothScanFragment", "onResume()");
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
        startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("BluetoothScanFragment", "onPause()");

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }

        if (mSwipeRefreshLayout!=null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public void onDestroy() {
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }

        super.onDestroy();
        Log.d("BluetoothScanFragment", "onDestroy()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void startScan() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }

            mBluetoothDeviceAdapter = new BluetoothDeviceListAdapter(getActivity(), R.layout.ble_device_list_row);
            getListView().setAdapter(mBluetoothDeviceAdapter);

            scanLeDevice(true);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
//                    if (Build.VERSION.SDK_INT < 21) {
//                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    } else {
                    mLEScanner.stopScan(mScanCallback);
//                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            };
            mHandler.postDelayed(mRunnable, SCAN_PERIOD);
//            if (Build.VERSION.SDK_INT < 21) {
//                mBluetoothAdapter.startLeScan(mLeScanCallback);
//            } else {
            mLEScanner.startScan(filters, settings, mScanCallback);
//            }
        } else {
//            if (Build.VERSION.SDK_INT < 21) {
//                mBluetoothAdapter.stopLeScan(mLeScanCallback);
//            } else {
            mSwipeRefreshLayout.setRefreshing(false);
            mLEScanner.stopScan(mScanCallback);
//            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.i("callbackType", String.valueOf(callbackType));
//            Log.d("result", result.toString());
            final BluetoothDevice btDevice = result.getDevice();
            final int rssi = result.getRssi();

            mBluetoothDeviceAdapter.addDevice(btDevice, rssi);
            mBluetoothDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
//                Log.d("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnBluetoothScanFragmentListener {
        // TODO: Update argument type and name
        void onBluetoothDeviceSelected(BluetoothDevice btdevice);
    }
}
