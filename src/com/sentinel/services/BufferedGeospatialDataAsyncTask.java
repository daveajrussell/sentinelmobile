package com.sentinel.services;

import android.content.Context;
import android.os.AsyncTask;
import com.sentinel.sql.SentinelDB;
import com.sentinel.utils.HttpResponseCode;
import com.sentinel.utils.ServiceHelper;

public class BufferedGeospatialDataAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    private static String mProcessResult;
    private static String mDataJson;

    private SentinelDB mSentinelDB;

    static {
        METHOD_NAME = "/PostBufferedGeospatialDataSet";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    public BufferedGeospatialDataAsyncTask(Context context) {
        mSentinelDB = new SentinelDB(context);
    }

    @Override
    protected String doInBackground(String... strings) {
        if (!strings[0].isEmpty()) {
            mDataJson = strings[0];
            mProcessResult = ServiceHelper.doPost(null, METHOD_NAME, URL, mDataJson, false);
        }
        return mProcessResult;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result.equals(HttpResponseCode.OK_RESULT)) {
            mSentinelDB.deleteGeospatialData();
            mSentinelDB.closeSentinelDatabase();
        }
    }
}