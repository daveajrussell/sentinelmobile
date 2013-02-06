package com.sentinel.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * David Russell
 * 11/12/12
 */
public abstract class ConnectionManager {
    public static boolean deviceIsConnected(Context context) {
        ConnectivityManager oConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo oNetInfo = oConnectivityManager.getActiveNetworkInfo();

        return oNetInfo != null && oNetInfo.isConnectedOrConnecting();
    }
}
