package com.sentinel.tracking;

import android.os.AsyncTask;
import com.sentinel.utils.ServiceHelper;

public class OrientationNotificationAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    private String processResult;
    private String geoDataJson;

    static {
        METHOD_NAME = "/PostOrientationNotification";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    @Override
    protected String doInBackground(String... strings) {
        if (!strings[0].isEmpty()) {
            geoDataJson = strings[0];
            processResult = ServiceHelper.doPost(null, METHOD_NAME, URL, geoDataJson, false);
        }
        return processResult;
    }
}