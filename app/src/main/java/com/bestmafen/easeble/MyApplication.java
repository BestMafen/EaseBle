package com.bestmafen.easeble;

import android.app.Application;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

/**
 * Created by Administrator on 2018/9/17/017.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initUtils();
    }

    private void initUtils() {
        Utils.init(this);
        LogUtils.Config config = LogUtils.getConfig();
        config.setLogSwitch(true);
        config.setGlobalTag("EaseBle");
    }
}
