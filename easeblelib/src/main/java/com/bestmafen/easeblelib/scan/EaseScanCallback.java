package com.bestmafen.easeblelib.scan;

import com.bestmafen.easeblelib.entity.EaseDevice;

import java.util.List;

/**
 * Interface used to process scan result,all the methods called at main thread
 */
public interface EaseScanCallback {

    /**
     * This method will be invoked when you start a scan with Bluetooth adapter being disabled.
     */
    void onBluetoothDisabled();

    /**
     * This method will be invoked when a scan starts or stops.
     *
     * @param scan start scan if true,stop scanning if false
     */
    void onScan(boolean scan);

    /**
     * This method will be invoked when a expected mDevice has been found.
     *
     * @param device a expected mDevice
     */
    void onDeviceFound(EaseDevice device);
}
