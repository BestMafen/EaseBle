package com.bestmafen.easeble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.bestmafen.easeblelib.entity.EaseDevice;
import com.bestmafen.easeblelib.scan.AbsScanner;
import com.bestmafen.easeblelib.scan.EaseScanCallback;
import com.bestmafen.easeblelib.scan.ScannerFactory;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtScan;

    private AbsScanner mScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtScan = findViewById(R.id.bt_scan);
        mBtScan.setOnClickListener(this);

        mScanner = ScannerFactory.getDefaultScanner();
        mScanner.setEaseScanCallback(new EaseScanCallback() {

            @Override
            public void onBluetoothDisabled() {
                ToastUtils.showLong(R.string.bluetooth_is_disabled);
            }

            @Override
            public void onScan(boolean scan) {
                mBtScan.setText(scan ? R.string.scanning : R.string.start_scan);
            }

            @Override
            public void onDeviceFound(EaseDevice device) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_scan:
                if (PermissionUtils.isGranted(PermissionConstants.LOCATION)) {
                    mScanner.scan(!mScanner.isScanning());
                } else {
                    PermissionUtils.permission(PermissionConstants.LOCATION).callback(new PermissionUtils.SimpleCallback() {
                        @Override
                        public void onGranted() {
                            mScanner.scan(!mScanner.isScanning());
                        }

                        @Override
                        public void onDenied() {

                        }
                    }).request();
                }
                break;
        }
    }
}
