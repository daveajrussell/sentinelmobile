package com.sentinel.tracking;

import android.content.Context;
import android.os.AsyncTask;
import com.sentinel.helper.ServiceHelper;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;

public class SpeedingNotificationAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    private Context mContext;
    private String processResult;
    private String geoDataJson;

    static {
        METHOD_NAME = "/PostSpeedingNotification";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    public SpeedingNotificationAsyncTask(Context context) {
        mContext = context;
        mSentinelDatabase = new SentinelBuffferedGeospatialDataDB(mContext);
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