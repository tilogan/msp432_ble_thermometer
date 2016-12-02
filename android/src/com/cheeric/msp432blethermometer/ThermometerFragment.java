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

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.cheeric.msp432blethermometer.gatt.CharacteristicChangeListener;
import com.cheeric.msp432blethermometer.gatt.GattManager;
import com.cheeric.msp432blethermometer.gatt.GattOperationBundle;
import com.cheeric.msp432blethermometer.gatt.operations.GattSetNotificationOperation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ThermometerFragment extends Fragment {
    private static final String BT_DEVICE = "bt_device";
    private final String TEMP_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private final String TEMP_CHARC_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private final String TEMP_CHARF_UUID = "0000ffe2-0000-1000-8000-00805f9b34fb";
    private final int maxNumOfReadings = 15;

    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic ledChar;
    private boolean flag;
    private List<Entry> entries;
    private LineChart chart;
    private LineDataSet dataSet;
    private LineData lineData;
    private int curIndex;
    private boolean isThermometer;
    private Handler handler;
    private Runnable runnable;
    private ProgressDialog dialog;

    private float avg_temperature;
    private float max_temperature;
    private float min_temperature;

    // TODO: Rename and change types of parameters
    private BluetoothDevice mBtdevice;
    private GattManager mGattManager;


//    private OnFragmentInteractionListener mListener;

    public ThermometerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param btdevice Parameter 1.
     * @return A new instance of fragment ThermometerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ThermometerFragment newInstance(BluetoothDevice btdevice) {
        ThermometerFragment fragment = new ThermometerFragment();
        Bundle args = new Bundle();
        args.putParcelable(BT_DEVICE, btdevice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("ThermometerFragment", "onCreate()");
        super.onCreate(savedInstanceState);

        setRetainInstance(true);


        if (savedInstanceState!= null) {

        }
        else {
            if (getArguments() != null)
                mBtdevice = getArguments().getParcelable(BT_DEVICE);

            mGattManager = new GattManager(getActivity());

            isThermometer = false;
            connectToDevice(mBtdevice);
            dialog = ProgressDialog.show(getActivity(), "", "Connecting...", true);

            handler = new android.os.Handler();
            runnable = new Runnable() {
                public void run() {
                    Log.i("tag", "5000 milliseconds timeout");
                    dialog.dismiss();
                    if (!isThermometer) {
                        returnToDeviceSelection();
                    }
                }
            };
            handler.postDelayed(runnable, 5000);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ThermometerFragment", "onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_thermometer, container, false);
    }

    @Override
    public void onAttach(Context context) {
        Log.d("ThermometerFragment", "onAttach()");
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnBluetoothScanFragmentListener");
//        }
    }

    @Override
    public void onDestroy() {
        Log.d("ThermometerFragment", "onDestroy()");
        super.onDestroy();
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
        }
    }

    @Override
    public void onDetach() {
        Log.d("ThermometerFragment", "onDetach()");
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onResume() {
        Log.d("ThermometerFragment", "onResume()");
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("MSP432 BLE Thermometer");

        initializeChart();

        final Switch notification_switch = (Switch)getActivity().findViewById(R.id.notification_switch);
        final Switch unit_switch = (Switch)getActivity().findViewById(R.id.unit_switch);

        notification_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GattOperationBundle bundle = new GattOperationBundle();
                if (isChecked) {
                    initializeData();

                    if (unit_switch.isChecked()) {
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARC_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                false
                        ));
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARF_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                true
                        ));
                    } else {
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARF_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                false
                        ));
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARC_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                true
                        ));
                    }
                }
                else {
                    bundle.addOperation(new GattSetNotificationOperation(
                            mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                            UUID.fromString(TEMP_CHARC_UUID),
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                            false
                    ));
                    bundle.addOperation(new GattSetNotificationOperation(
                            mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                            UUID.fromString(TEMP_CHARF_UUID),
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                            false
                    ));
                }
                mGattManager.queue(bundle);
            }
        });

        unit_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GattOperationBundle bundle = new GattOperationBundle();
                if (notification_switch.isChecked()) {
                    if (isChecked) {
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARC_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                false
                        ));
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARF_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                true
                        ));
                    } else {
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARF_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                false
                        ));
                        bundle.addOperation(new GattSetNotificationOperation(
                                mBtdevice, UUID.fromString(TEMP_SERVICE_UUID),
                                UUID.fromString(TEMP_CHARC_UUID),
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                                true
                        ));
                    }
                }
                mGattManager.queue(bundle);
                initializeData();
            }
        });
    }

    @Override
    public void onPause() {
        Log.d("ThermometerFragment", "onPause()");
        super.onPause();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];
        for (int i = 0; i < len; i += 2) {
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(getActivity(), false, gattCallback);
            mGattManager.setGatt(mGatt);


        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d("GattManager", "Connection State Changed!!!");
            super.onConnectionStateChange(gatt, status, newState);
            if (status == 133) {
                Log.e("GattManager", "Got the status 133 bug, closing gatt");
                gatt.close();
//                mGatts.remove(device.getAddress());
                dialog.dismiss();
                handler.removeCallbacks(runnable);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        returnToDeviceSelection();
                    }
                });
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("GattManager", "Gatt connected to device " + mBtdevice.getAddress());
//                mGatts.put(device.getAddress(), gatt);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("GattManager", "Disconnected from gatt server " + mBtdevice.getAddress() + ", newState: " + newState);
//                mGatts.remove(device.getAddress());
                mGattManager.setCurrentOperation(null);
                gatt.close();
                mGattManager.drive();
                dialog.dismiss();
                handler.removeCallbacks(runnable);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        returnToDeviceSelection();
                    }
                });
            }
        }
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
//            ((GattDescriptorReadOperation) mCurrentOperation).onRead(descriptor);
            mGattManager.setCurrentOperation(null);
            mGattManager.drive();
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            mGattManager.setCurrentOperation(null);
            mGattManager.drive();
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
//            ((GattCharacteristicReadOperation) mCurrentOperation).onRead(characteristic);
            mGattManager.setCurrentOperation(null);
            mGattManager.drive();
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d("GattManager", "services discovered, status: " + status);

            List<BluetoothGattService> services = mGatt.getServices();
            for (BluetoothGattService s : services) {
                if (s.getUuid().toString().equals(TEMP_SERVICE_UUID)) {
                    dialog.dismiss();
                    isThermometer = true;
                    handler.removeCallbacks(runnable);
                }
            }

            if(!isThermometer){
                Log.d("ThermometerFragment", "Thermometer Service not found");
                dialog.dismiss();
                handler.removeCallbacks(runnable);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        returnToDeviceSelection();
                    }
                });
            }

            mGattManager.addCharacteristicChangeListener(
                    UUID.fromString(TEMP_CHARC_UUID),
                    new CharacteristicChangeListener() {
                        @Override
                        public void onCharacteristicChanged(String deviceAddress, BluetoothGattCharacteristic characteristic) {
                            float temp = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            appendDataToChart(temp);
                        }
                    }
            );
            mGattManager.addCharacteristicChangeListener(
                    UUID.fromString(TEMP_CHARF_UUID),
                    new CharacteristicChangeListener() {
                        @Override
                        public void onCharacteristicChanged(String deviceAddress, BluetoothGattCharacteristic characteristic) {
                            float temp = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                            appendDataToChart(temp);
                        }
                    }
            );



            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((Switch)getActivity().findViewById(R.id.notification_switch)).setChecked(true);
                }
            });
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("GattManager", "Characteristic " + characteristic.getUuid() + "written to on device " + mBtdevice.getAddress());
            mGattManager.setCurrentOperation(null);
            mGattManager.drive();
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("GattManager", "Characteristic " + characteristic.getUuid() + "was changed, device: " + mBtdevice.getAddress());
            if (mGattManager.getCharacteristicChangeListeners().containsKey(characteristic.getUuid())) {
                for (CharacteristicChangeListener listener : mGattManager.getCharacteristicChangeListeners().get(characteristic.getUuid())) {
                    listener.onCharacteristicChanged(mBtdevice.getAddress(), characteristic);
                }
            }
        }
    };

    private void returnToDeviceSelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Selected device does not contain the expected BLE services/characteristics for the thermometer device. Make sure correct firmware is programmed and the appropriate device is being selected.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initializeChart() {
        chart = (LineChart) getActivity().findViewById(R.id.chart);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);

        if (((Switch)getActivity().findViewById(R.id.unit_switch)).isChecked()) {
            chart.getAxisLeft().setAxisMaximum(100);
            chart.getAxisLeft().setAxisMinimum(30);
        }
        else {
            chart.getAxisLeft().setAxisMaximum(45);
            chart.getAxisLeft().setAxisMinimum(-5);
        }

        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawLabels(false);

        chart.getLegend().setEnabled(false);
        chart.getDescription().setText("Temperature Data from MSP432");
        chart.setTouchEnabled(false);
        chart.setData(lineData);
    }

    private void initializeData() {
        curIndex = 0;
        entries = new ArrayList<Entry>();
        dataSet = new LineDataSet(entries, "Temperature");
        lineData = new LineData(dataSet);

        dataSet.setDrawValues(false);
        dataSet.setColor(Color.parseColor("Black"));
        dataSet.setCircleColor(Color.parseColor("Black"));
        dataSet.setDrawCircleHole(false);

        avg_temperature = 0;
        max_temperature = Float.MIN_VALUE;
        min_temperature = Float.MAX_VALUE;

        initializeChart();
    }

    private void appendDataToChart(final float temperature) {
        final String unit;
        if (((Switch)getActivity().findViewById(R.id.unit_switch)).isChecked())
            unit = " \u2109";
        else
            unit = " \u2103";

        if (curIndex < maxNumOfReadings)
            curIndex++;
        else {
            for (Entry entry : dataSet.getValues())
                entry.setX(entry.getX()-1);
            dataSet.removeFirst();
        }
        dataSet.addEntry(new Entry(curIndex-1, temperature));

        avg_temperature = 0;

        for (Entry entry : dataSet.getValues())
            avg_temperature += entry.getY();
        avg_temperature /= dataSet.getValues().size();

        if (temperature > max_temperature)
            max_temperature = temperature;
        if (temperature < min_temperature)
            min_temperature = temperature;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)getActivity().findViewById(R.id.temperature)).setText(
                        String.format("%.1f", temperature) + unit);
                lineData.notifyDataChanged();
                dataSet.setDrawValues(true);
                chart.notifyDataSetChanged();
                chart.invalidate();

                ((TextView)getActivity().findViewById(R.id.avg_temperature)).setText(
                        String.format("%.1f", avg_temperature) + unit);
                ((TextView)getActivity().findViewById(R.id.max_temperature)).setText(
                        String.format("%.1f", max_temperature) + unit);
                ((TextView)getActivity().findViewById(R.id.min_temperature)).setText(
                        String.format("%.1f", min_temperature) + unit);
            }
        });

    }
}
