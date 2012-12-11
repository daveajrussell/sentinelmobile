package com.sentinel.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * David Russell
 * 11/12/12
 */
public class LocationServiceBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        String strJSON = readGISDataFromBuffer();
        if(!strJSON.isEmpty())
            new LocationServiceAsyncTask().execute(strJSON);
    }

    private String readGISDataFromBuffer() {
        return GISDataBuffer.readJSONStringFromBuffer();
    }
}
