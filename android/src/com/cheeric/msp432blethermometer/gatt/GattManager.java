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
package com.cheeric.msp432blethermometer.gatt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.cheeric.msp432blethermometer.gatt.operations.GattOperation;

public class GattManager {
    private Context mContext;
    private ConcurrentLinkedQueue<GattOperation> mQueue;
    private ConcurrentHashMap<String, BluetoothGatt> mGatts;
    private BluetoothGatt mGatt;
    private GattOperation mCurrentOperation;
    private HashMap<UUID, ArrayList<CharacteristicChangeListener>> mCharacteristicChangeListeners;
    private AsyncTask<Void, Void, Void> mCurrentOperationTimeout;
    public GattManager(Context context) {
        mContext = context;
        mQueue = new ConcurrentLinkedQueue<>();
        mGatts = new ConcurrentHashMap<>();
        mCurrentOperation = null;
        mCharacteristicChangeListeners = new HashMap<>();
    }
    public synchronized void cancelCurrentOperationBundle() {
        Log.v("GattManager", "Cancelling current operation. Queue size before: " + mQueue.size());
        if(mCurrentOperation != null && mCurrentOperation.getBundle() != null) {
            for(GattOperation op : mCurrentOperation.getBundle().getOperations()) {
                mQueue.remove(op);
            }
        }
        Log.v("GattManager", "Queue size after: " + mQueue.size());
        mCurrentOperation = null;
        drive();
    }
    public synchronized void queue(GattOperation gattOperation) {
        mQueue.add(gattOperation);
        Log.v("GattManager", "Queueing Gatt operation, size will now become: " + mQueue.size());
        drive();
    }
    public synchronized void drive() {
        if(mCurrentOperation != null) {
            Log.e("GattManager", "tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }
        if( mQueue.size() == 0) {
            Log.v("GattManager", "Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            return;
        }
        final GattOperation operation = mQueue.poll();
        Log.v("GattManager", "Driving Gatt queue, size will now become: " + mQueue.size());
        setCurrentOperation(operation);
        if(mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }
        mCurrentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    Log.v("GattManager", "Starting to do a background timeout");
                    wait(operation.getTimoutInMillis());
                } catch (InterruptedException e) {
                    Log.v("GattManager", "was interrupted out of the timeout");
                }
                if(isCancelled()) {
                    Log.v("GattManager", "The timeout was cancelled, so we do nothing.");
                    return null;
                }
                Log.v("GattManager", "Timeout ran to completion, time to cancel the entire operation bundle. Abort, abort!");
                cancelCurrentOperationBundle();
                return null;
            }
            @Override
            protected synchronized void onCancelled() {
                super.onCancelled();
                notify();
            }
        }.execute();
        final BluetoothDevice device = operation.getDevice();
        execute(mGatt, operation);
    }
    private void execute(BluetoothGatt gatt, GattOperation operation) {
        if(operation != mCurrentOperation) {
            return;
        }
        operation.execute(gatt);
        if(!operation.hasAvailableCompletionCallback()) {
            setCurrentOperation(null);
            drive();
        }
    }
    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }
    public BluetoothGatt getGatt(BluetoothDevice device) {
        return mGatts.get(device);
    }
    public void addCharacteristicChangeListener(UUID characteristicUuid, CharacteristicChangeListener characteristicChangeListener) {
        if(!mCharacteristicChangeListeners.containsKey(characteristicUuid)) {
            mCharacteristicChangeListeners.put(characteristicUuid, new ArrayList<CharacteristicChangeListener>());
        }
        mCharacteristicChangeListeners.get(characteristicUuid).add(characteristicChangeListener);
    }
    public void queue(GattOperationBundle bundle) {
        for(GattOperation operation : bundle.getOperations()) {
            queue(operation);
        }
    }
    public class ConnectionStateChangedBundle {
        public final int mNewState;
        public final String mAddress;
        public ConnectionStateChangedBundle(String address, int newState) {
            mAddress = address;
            mNewState = newState;
        }
    }

    public HashMap<UUID, ArrayList<CharacteristicChangeListener>> getCharacteristicChangeListeners() {
        return mCharacteristicChangeListeners;
    }

    public void setGatt(BluetoothGatt gatt) {
        mGatt = gatt;
    }
}