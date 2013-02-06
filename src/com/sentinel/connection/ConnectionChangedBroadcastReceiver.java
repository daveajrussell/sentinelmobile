package com.sentinel.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.sentinel.helper.ServiceHelper;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;
//import android.net.NetworkInfo;

/**
 * David Russell
 * 11/12/12
 */
public class ConnectionChangedBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        ConnectivityManager oConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo oNetInfo = oConnectivityManager.getActiveNetworkInfo();

        if (null != oNetInfo && oNetInfo.isConnectedOrConnecting()) {
            SentinelBuffferedGeospatialDataDB oSentinelDB = new SentinelBuffferedGeospatialDataDB(context);

            if (0 != oSentinelDB.getBufferedGeospatialDataCount()) {
                String strGeospatialInformationJson;

                strGeospatialInformationJson = oSentinelDB.getBufferedGeospatialDataJsonString();

                if (oSentinelDB.getBufferedGeospatialDataCount() >= 2) {
                    ServiceHelper.sendBufferedGeospatialDataToLocationService(context, strGeospatialInformationJson);
                } else {
                    ServiceHelper.sendGISToLocationService(context, strGeospatialInformationJson);
                }
                oSentinelDB.closeSentinelDatabase();
            }
        }
    }
}
