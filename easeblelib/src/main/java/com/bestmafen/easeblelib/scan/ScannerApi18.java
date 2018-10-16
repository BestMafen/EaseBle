package com.bestmafen.easeblelib.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.bestmafen.easeblelib.entity.EaseDevice;
import com.blankj.utilcode.util.LogUtils;

/**
 * This class is used to implement {@link AbsScanner} when API<21.
 */
class ScannerApi18 extends AbsScanner {
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    @Override
    public synchronized void scan(final boolean scan) {
        if (isScanning == scan) return;

        if (mEaseScanCallback == null) {
            throw new RuntimeException("EaseScanCallback can not be null");
        }

        if (mScanOption == null) {
            mScanOption = new ScanOption();
        }

        if (scan) {
            if (!sBluetoothAdapter.isEnabled()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEaseScanCallback.onBluetoothDisabled();
                    }
                });
                return;
            }

            if (mLeScanCallback == null) {
                mLeScanCallback = new MyLeScanCallBack();
            }
            mDevices.clear();
            sBluetoothAdapter.startLeScan(mLeScanCallback);
            mHandler.postDelayed(mStopScanningRunnable, mScanOption.mPeriod);
        } else {
            sBluetoothAdapter.stopLeScan(mLeScanCallback);
            mHandler.removeCallbacks(mStopScanningRunnable);
        }

        LogUtils.vTag(TAG, "ScannerApi18 " + hashCode() + " -> scan " + scan);
        isScanning = scan;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEaseScanCallback.onScan(scan);
            }
        });
    }

    class MyLeScanCallBack implements BluetoothAdapter.LeScanCallback {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (!isScanning) return;

            if (device == null) return;

            String name = device.getName();
            String address = device.getAddress();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) return;

            LogUtils.vTag(TAG, "ScannerApi18 onLeScan -> name=" + name + ", address=" + address + ", rssi=" + rssi);
            whenDeviceScanned(new EaseDevice(device, rssi, scanRecord));
        }
    }
}
