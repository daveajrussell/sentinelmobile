package com.sentinel.tracking;

import android.os.AsyncTask;
import com.sentinel.helper.ServiceHelper;

public class SpeedingNotificationAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    private String processResult;
    private String geoDataJson;

    static {
        METHOD_NAME = "/PostSpeedingNotification";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    @Override
    protected String doInBackground(String... strings) {
        if (!strings[0].isEmpty()) {
            geoDataJson = strings[0];
            processResult = ServiceHelper.doPost(METHOD_NAME, URL, geoDataJson);
        }
        return processResult;
    }
}