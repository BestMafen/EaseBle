package com.bestmafen.easeblelib.scan;

import android.os.Build;

/**
 * This class is used to create a {@link AbsScanner} object at different API levels.
 */
public class ScannerFactory {

    private ScannerFactory() {

    }

    public static synchronized AbsScanner getDefaultScanner() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static AbsScanner instance = newScanner();
    }

    public static AbsScanner newScanner() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new ScannerApi18();
        } else {
            return new ScannerApi21();
        }
    }
}
