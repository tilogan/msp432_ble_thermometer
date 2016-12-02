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

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;

public class BluetoothDeviceListAdapter extends BaseAdapter {
    private Activity context;
    private int layoutResourceId;
    private ArrayList<BluetoothDevice> device_list;
    private HashMap hm = new HashMap();

    public BluetoothDeviceListAdapter(Activity context, int layoutResourceId) {
        super();
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.device_list = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device, int rssi) {
        if (!device_list.contains(device)) {
//            Log.i("BleDeviceListAdapter", "Added " + device.getName());
            device_list.add(device);
        }
        hm.put(device, rssi);
    }

    @Override
    public Object getItem(int position) {
        return device_list.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return device_list.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = inflater.inflate(layoutResourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceRssi = (TextView) view.findViewById(R.id.ble_device_info);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.ble_device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = device_list.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
//            if (deviceName.equals("MSP432 Thermometer")) {
//                view.setBackgroundColor(Color.parseColor("#00BBCC"));
//            }
//            else {
//                view.setBackgroundColor(Color.TRANSPARENT);
//            }
        }
        else {
            viewHolder.deviceName.setText("Unknown Device");
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        viewHolder.deviceRssi.setText(hm.get(device).toString());
        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceRssi;
    }
}
