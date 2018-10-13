package com.bestmafen.easeblelib.entity;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bestmafen.easeblelib.util.EaseUtils;

/**
 * The class wraps a {@link BluetoothDevice}.
 */
public class EaseDevice implements Parcelable {
    private BluetoothDevice mDevice;
    private int             mRssi;
    private byte[]          mScanRecord;

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null) return false;

        return o instanceof EaseDevice && mDevice.equals(((EaseDevice) o).mDevice);
    }

    @Override
    public int hashCode() {
        return mDevice.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDevice, flags);
        dest.writeInt(this.mRssi);
        dest.writeByteArray(this.mScanRecord);
    }

    public EaseDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
        this.mDevice = device;
        this.mRssi = rssi;
        this.mScanRecord = scanRecord;
    }

    protected EaseDevice(Parcel in) {
        this.mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.mRssi = in.readInt();
        this.mScanRecord = in.createByteArray();
    }

    public static final Creator<EaseDevice> CREATOR = new Creator<EaseDevice>() {

        @Override
        public EaseDevice createFromParcel(Parcel source) {
            return new EaseDevice(source);
        }

        @Override
        public EaseDevice[] newArray(int size) {
            return new EaseDevice[size];
        }
    };

    @Override
    public String toString() {
        return "EaseDevice{" +
                "deviceName='" + mDevice.getName() + '\'' +
                ", deviceAddress'=" + mDevice.getAddress() + '\'' +
                ", mRssi=" + mRssi +
                ", mScanRecord=" + EaseUtils.byteArray2HexString(mScanRecord) +
                '}';
    }
}
