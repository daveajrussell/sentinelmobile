package com.sentinel.tracking;

import android.content.Context;
import android.os.AsyncTask;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;
import com.sentinel.utils.HttpResponseCode;
import com.sentinel.utils.ServiceHelper;

public class HistoricalGeospatialDataAsyncTask extends AsyncTask<String, Integer, String> {
    private static final String METHOD_NAME;
    private static final String URL;

    private static String strProcessResult;
    private static String geoDataJson;

    private SentinelBuffferedGeospatialDataDB oSentinelDB;

    static {
        METHOD_NAME = "/PostBufferedHistorialData";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    public HistoricalGeospatialDataAsyncTask(Context context) {
        oSentinelDB = new SentinelBuffferedGeospatialDataDB(context);
    }

    @Override
    protected String doInBackground(String... strings) {
        if (!strings[0].isEmpty()) {
            geoDataJson = strings[0];
            strProcessResult = ServiceHelper.doPost(null, METHOD_NAME, URL, geoDataJson, false);
        }
        return strProcessResult;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == HttpResponseCode.OK_RESULT) {
            oSentinelDB.deleteGeospatialData();
            oSentinelDB.closeSentinelDatabase();
        }
    }
}
