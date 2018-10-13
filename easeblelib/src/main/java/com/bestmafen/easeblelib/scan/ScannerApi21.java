package com.bestmafen.easeblelib.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.bestmafen.easeblelib.entity.EaseDevice;
import com.blankj.utilcode.util.LogUtils;

import java.util.List;

/**
 * This class is used to implement {@link AbsScanner} when API>=21.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class ScannerApi21 extends AbsScanner {
    private static final String TAG = "ScannerApi18";

    private ScanCallback mScanCallback;
    private ScanSettings.Builder mScanSettingsBuilder = new ScanSettings.Builder();

    @Override
    public synchronized void scan(final boolean scan) {
        if (isScanning == scan) return;

        if (mEaseScanCallback == null) {
            throw new RuntimeException("EaseScanCallback can not be null");
        }

        if (mScanOption == null) {
            mScanOption = new ScanOption();
        }

        BluetoothLeScanner scanner = sBluetoothAdapter.getBluetoothLeScanner();
        if (scan) {
            if (scanner == null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEaseScanCallback.onBluetoothDisabled();
                    }
                });
                return;
            }

            if (mScanCallback == null) {
                mScanCallback = new MyScanCallback();
            }

            mDevices.clear();
            switch (mScanOption.mScanMode) {
                case ScanOption.LOW_POWER:
                    mScanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                    break;
                case ScanOption.BALANCED:
                    mScanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
                    break;
                case ScanOption.LOW_LATENCY:
                    mScanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                    break;
            }
            scanner.startScan(null, mScanSettingsBuilder.build(), mScanCallback);
            mHandler.postDelayed(mStopScanRunnable, mScanOption.mPeriod);
        } else {
            if (scanner != null) {
                scanner.stopScan(mScanCallback);
            }
            mHandler.removeCallbacks(mStopScanRunnable);
        }

        LogUtils.vTag(TAG, "ScannerApi21 " + hashCode() + " -> scan " + scan);
        isScanning = scan;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mEaseScanCallback.onScan(scan);
            }
        });
    }

    class MyScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (!isScanning) return;

            if (result == null) return;

            final BluetoothDevice device = result.getDevice();
            if (device == null) return;

            final String name = device.getName();
            final String address = device.getAddress();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) return;

            LogUtils.vTag(TAG, "ScannerApi21 onScanResult -> name=" + name + ", address=" + address);
            final int rssi = result.getRssi();
            final byte[] scanRecord = result.getScanRecord() == null ?
                    new byte[0] : result.getScanRecord().getBytes();

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (mScanOption.mNames.size() > 0 && !mScanOption.mNames.contains(name))
                        return;

                    if (mScanOption.mAddresses.size() > 0 && !mScanOption.mAddresses.contains(address))
                        return;

                    if (rssi < mScanOption.mMinRssi) return;

                    EaseDevice easeDevice = new EaseDevice(device, rssi, scanRecord);
                    if (mDevices.contains(easeDevice)) {
                        mDevices.set(mDevices.indexOf(easeDevice), easeDevice);
                    } else {
                        mDevices.add(easeDevice);
                    }
                    mEaseScanCallback.onDeviceFound(easeDevice);
                    LogUtils.dTag(TAG, "ScannerApi21 onScanResult -> " + easeDevice.toString());
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {

        }
    }
}
