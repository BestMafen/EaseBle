package com.bestmafen.easeble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bestmafen.easeblelib.entity.EaseDevice;
import com.bestmafen.easeblelib.scan.AbsScanner;
import com.bestmafen.easeblelib.scan.EaseScanCallback;
import com.bestmafen.easeblelib.scan.ScanOption;
import com.bestmafen.easeblelib.scan.ScannerFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private BaseAdapter mAdapter;
    private List<EaseDevice> mDevices = new ArrayList<>();

    private Button mBtScan;

    private AbsScanner mScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.lv);
        listView.setAdapter(mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mDevices.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = View.inflate(MainActivity.this, R.layout.item_device, null);
                    holder = new ViewHolder(convertView);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.mTvName.setText(mDevices.get(position).getDevice().getName());
                holder.mTvAddress.setText(mDevices.get(position).getDevice().getAddress());
                holder.mTvRssi.setText(String.valueOf(mDevices.get(position).getRssi()));
                return convertView;
            }
        });
        mBtScan = findViewById(R.id.bt_scan);
        mBtScan.setOnClickListener(this);

        ScanOption option = new ScanOption().withMinRssi(-72).withPeriod(5000).withScanMode(ScanOption.BALANCED);
        mScanner = ScannerFactory.getDefaultScanner()
                .setScanOption(option)
                .setEaseScanCallback(new EaseScanCallback() {

                    @Override
                    public void onBluetoothDisabled() {
                        Toast.makeText(MainActivity.this, R.string.bluetooth_is_disabled, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onScan(boolean scan) {
                        mBtScan.setText(scan ? R.string.scanning : R.string.start_scan);
                        if (scan) {
                            mDevices.clear();
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onDeviceFound(EaseDevice device) {
                        mDevices.add(device);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        mScanner.exit();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_scan:
                mScanner.scan(!mScanner.isScanning());
                break;
        }
    }

    class ViewHolder {
        TextView mTvName, mTvAddress, mTvRssi;

        ViewHolder(View view) {
            mTvName = view.findViewById(R.id.tv_name);
            mTvAddress = view.findViewById(R.id.tv_address);
            mTvRssi = view.findViewById(R.id.tv_rssi);
            view.setTag(this);
        }
    }
}
