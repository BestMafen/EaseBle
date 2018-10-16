package com.bestmafen.easeblelib.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;

import com.bestmafen.easeblelib.entity.EaseDevice;
import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract class which is used to replace
 * {@link BluetoothAdapter#startLeScan(BluetoothAdapter.LeScanCallback)} and
 * {@link android.bluetooth.le.BluetoothLeScanner#startScan(List, ScanSettings, ScanCallback)}.There are different
 * implementations on different API levels,see {@link ScannerApi21} and {@link ScannerApi18}.
 */
public abstract class AbsScanner {
    static final String TAG = "AbsScanner";

    static BluetoothAdapter sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Handler to dispatch callback event on the main thread.
     */
    Handler mHandler = new Handler(Looper.getMainLooper());

    ScanOption       mScanOption;
    EaseScanCallback mEaseScanCallback;

    List<EaseDevice> mDevices = new ArrayList<>();

    boolean isScanning = false;

    /**
     * The runnable to stop scanning.
     */
    Runnable mStopScanningRunnable = new Runnable() {

        @Override
        public void run() {
            scan(false);
        }
    };

    /**
     * Start to scan or stop scanning.
     *
     * @param scan start to scan if true,stop scanning if false.
     */
    public abstract void scan(boolean scan);

    public AbsScanner setScanOption(ScanOption option) {
        mScanOption = option;
        return this;
    }

    public AbsScanner setEaseScanCallback(EaseScanCallback easeScanCallback) {
        mEaseScanCallback = easeScanCallback;
        return this;
    }

    public boolean isScanning() {
        return sBluetoothAdapter.isEnabled() && isScanning;
    }

    /**
     * Stop scanning and set the {@link EaseScanCallback} to null to avoid memory leaking.
     */
    public void exit() {
        scan(false);
        mEaseScanCallback = null;
    }

    void whenDeviceScanned(final EaseDevice easeDevice) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (!mScanOption.mNames.isEmpty() && !mScanOption.mNames.contains(easeDevice.getDevice().getName()))
                    return;

                if (!mScanOption.mAddresses.isEmpty() && !mScanOption.mAddresses.contains(easeDevice.getDevice().getAddress()))
                    return;

                if (easeDevice.getRssi() < mScanOption.mMinRssi) return;

                if (mDevices.contains(easeDevice)) return;

                mDevices.add(easeDevice);
                mEaseScanCallback.onDeviceFound(easeDevice);
            }
        });
    }
}
