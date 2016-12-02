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

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class ThermometerActivity extends AppCompatActivity
        implements BluetoothScanFragment.OnBluetoothScanFragmentListener {

    private static final String BT_FRAGMENT_TAG = "BT_FRAGMENT_TAG";
    private static final String THERMOMETER_TAG = "THERMOMETER_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermometer);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.thermometer_toolbar);
        myToolbar.setBackgroundColor(Color.parseColor("#CC0000"));
        myToolbar.setTitleTextColor(Color.WHITE);
        myToolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("MSP432 BLE Thermometer");


        if (savedInstanceState == null) {
            // Create a new Fragment to be placed in the activity layout
            BluetoothScanFragment bluetoothScanFragment = BluetoothScanFragment.newInstance("MSP432 Thermometer");

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, bluetoothScanFragment, BT_FRAGMENT_TAG).commit();
        }
        else {
            ThermometerFragment thermometerFragment = (ThermometerFragment) getSupportFragmentManager().findFragmentByTag(THERMOMETER_TAG);
        }
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        ThermometerFragment myFragment = (ThermometerFragment)getSupportFragmentManager().findFragmentByTag(THERMOMETER_TAG);
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//        myFragment.sendMessage(message);
    }


    public void onBluetoothDeviceSelected(BluetoothDevice btdevice) {
        Log.d("ProjectZeroActivity", "Device selected: " + btdevice.getName());
        if (findViewById(R.id.fragment_container) != null) {
            // Create a new Fragment to be placed in the activity layout
            ThermometerFragment thermometerFragment = ThermometerFragment.newInstance(btdevice);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, thermometerFragment, THERMOMETER_TAG);
            transaction.addToBackStack(null);

            transaction.commit();
        }
    }
}
