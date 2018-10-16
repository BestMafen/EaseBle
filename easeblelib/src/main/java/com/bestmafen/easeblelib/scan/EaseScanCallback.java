package com.bestmafen.easeblelib.scan;

import com.bestmafen.easeblelib.entity.EaseDevice;

import java.util.List;

/**
 * Interface used to process scan results,all the methods are called on the main thread.
 */
public interface EaseScanCallback {

    /**
     * This method will be called when a scan starts with Bluetooth adapter being disabled.
     */
    void onBluetoothDisabled();

    /**
     * This method will be called when a scan starts or stops.
     *
     * @param scan start scan if true,stop scanning if false
     */
    void onScan(boolean scan);

    /**
     * This method will be called when find a matched device.
     *
     * @param device a matched device
     */
    void onDeviceFound(EaseDevice device);
}
