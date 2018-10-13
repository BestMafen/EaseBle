package com.bestmafen.easeblelib.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.Looper;

import com.bestmafen.easeblelib.entity.EaseDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an abstract class which is used to replace
 * {@link BluetoothAdapter#startLeScan(BluetoothAdapter.LeScanCallback)} and
 * {@link android.bluetooth.le.BluetoothLeScanner#startScan(List, ScanSettings, ScanCallback)}.There are different
 * implementations on different API levels,see {@link ScannerApi21} and {@link ScannerApi18}.
 */
public abstract class AbsScanner {
    static BluetoothAdapter sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Handler mHandler = new Handler(Looper.getMainLooper());

    ScanOption       mScanOption;
    EaseScanCallback mEaseScanCallback;

    /**
     * Scanning results
     */
    List<EaseDevice> mDevices = new ArrayList<>();

    /**
     * Whether is scanning.
     */
    boolean isScanning = false;

    /**
     * The runnable to stop scanning.
     */
    Runnable mStopScanRunnable = new Runnable() {

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

    public boolean isScanning() {
        return isScanning;
    }

    /**
     * Stop scanning and set the {@link EaseScanCallback} to null to avoid memory leaking.
     */
    public void exit() {
        scan(false);
        mEaseScanCallback = null;
    }

    public AbsScanner setScanOption(ScanOption option) {
        mScanOption = option;
        return this;
    }

    public AbsScanner setEaseScanCallback(EaseScanCallback easeScanCallback) {
        mEaseScanCallback = easeScanCallback;
        return this;
    }
}
