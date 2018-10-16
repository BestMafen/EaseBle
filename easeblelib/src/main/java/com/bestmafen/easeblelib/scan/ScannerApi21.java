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
            mHandler.postDelayed(mStopScanningRunnable, mScanOption.mPeriod);
        } else {
            if (scanner != null) {
                scanner.stopScan(mScanCallback);
            }
            mHandler.removeCallbacks(mStopScanningRunnable);
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

            BluetoothDevice device = result.getDevice();
            if (device == null) return;

            String name = device.getName();
            String address = device.getAddress();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address)) return;

            LogUtils.vTag(TAG, "ScannerApi21 onScanResult -> name=" + name + ", address=" + address + ", rssi=" + result.getRssi());
            byte[] scanRecord = null;
            if (result.getScanRecord() != null) {
                scanRecord = result.getScanRecord().getBytes();
            }
            whenDeviceScanned(new EaseDevice(device, result.getRssi(), scanRecord));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {

        }
    }
}
