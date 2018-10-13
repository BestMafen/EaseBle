package com.bestmafen.easeblelib.scan;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ScanOption is passed to {@link AbsScanner} to define the parameters for the scan.
 */
public class ScanOption {
    public static final int LOW_POWER   = 0;
    public static final int BALANCED    = 1;
    public static final int LOW_LATENCY = 2;

    @IntDef({LOW_POWER, BALANCED, LOW_LATENCY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanMode {

    }

    /**
     * Names which are expected to be found.
     */
    Set<String> mNames = new HashSet<>();

    /**
     * Addresses which are expected to be found.
     */
    Set<String> mAddresses = new HashSet<>();

    /**
     * A device whose rssi is less than it will not be found.
     */
    int mMinRssi = -99;

    /**
     * Scan period,minimum value is 5000,unit:ms
     */
    long mPeriod = 16000;

    /**
     * scan mode,only effects at level 21 or higher, can be one of {@link ScanOption#LOW_POWER}
     * ,{@link ScanOption#BALANCED} or {@link ScanOption#LOW_POWER}.
     */
    int mScanMode = BALANCED;

    public ScanOption clearNames() {
        mNames.clear();
        return this;
    }

    public ScanOption withName(String name) {
        mNames.add(name);
        return this;
    }

    public ScanOption WithNames(List<String> names) {
        mNames.addAll(names);
        return this;
    }

    public ScanOption withNames(String[] names) {
        mNames.addAll(Arrays.asList(names));
        return this;
    }

    public ScanOption clearAddresses() {
        mAddresses.clear();
        return this;
    }

    public ScanOption withAddress(String address) {
        mAddresses.add(address);
        return this;
    }

    public ScanOption withAddresses(List<String> addresses) {
        mAddresses.addAll(addresses);
        return this;
    }

    public ScanOption withAddresses(String[] addresses) {
        mAddresses.addAll(Arrays.asList(addresses));
        return this;
    }

    public ScanOption withMinRssi(int rssi) {
        mMinRssi = rssi;
        return this;
    }

    public ScanOption withPeriod(long period) {
        mPeriod = period;
        if (mPeriod < 1000) {
            mPeriod = 5000;
        }
        return this;
    }

    public ScanOption withScanMode(@ScanMode int scanMode) {
        mScanMode = scanMode;
        return this;
    }
}
