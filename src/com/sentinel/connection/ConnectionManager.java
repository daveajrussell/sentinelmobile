package com.sentinel.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * David Russell
 * 11/12/12
 */
public class ConnectionManager {

    private Context m_oContext;

    public ConnectionManager(Context oContext) {
        this.m_oContext = oContext;
    }

    public boolean deviceIsConnected() {
        ConnectivityManager oConnectivityManager = (ConnectivityManager) m_oContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo oNetInfo = oConnectivityManager.getActiveNetworkInfo();

        return oNetInfo != null && oNetInfo.isConnectedOrConnecting();
    }

}
