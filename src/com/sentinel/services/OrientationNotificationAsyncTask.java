package com.sentinel.services;

import android.os.AsyncTask;
import com.sentinel.utils.ServiceHelper;

public class OrientationNotificationAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    private String mProcessResult;
    private String mGeoDataJson;

    static {
        METHOD_NAME = "/PostOrientationNotification";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    @Override
    protected String doInBackground(String... strings) {
        if (!strings[0].isEmpty()) {
            mGeoDataJson = strings[0];
            mProcessResult = ServiceHelper.doPost(null, METHOD_NAME, URL, mGeoDataJson, false);
        }
        return mProcessResult;
    }
}