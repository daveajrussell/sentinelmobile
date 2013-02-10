package com.sentinel.services;

import android.content.Context;
import android.os.AsyncTask;
import com.sentinel.sql.SentinelDB;
import com.sentinel.utils.HttpResponseCode;
import com.sentinel.utils.ServiceHelper;

public class LocationServiceAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;
    private static String mProcessResult;
    private static String mGeoDataJson;
    private SentinelDB mSentinelDB;

    static {
        METHOD_NAME = "/PostGeospatialData";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    public LocationServiceAsyncTask(Context oContext) {
        mSentinelDB = new SentinelDB(oContext);
    }

    @Override
    protected String doInBackground(String... strings) {

        if (!strings[0].isEmpty()) {
            mGeoDataJson = strings[0];
            mProcessResult = ServiceHelper.doPost(null, METHOD_NAME, URL, mGeoDataJson, false);
        }
        return mProcessResult;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result.equals(HttpResponseCode.OK_RESULT)) {
            mSentinelDB.deleteGeospatialData();
        }
    }
}