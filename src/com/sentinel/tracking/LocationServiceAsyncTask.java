package com.sentinel.tracking;

import android.content.Context;
import android.os.AsyncTask;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.helper.ServiceHelper;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;

public class LocationServiceAsyncTask extends AsyncTask<String, Integer, String>
{

    private static final String METHOD_NAME;
    private static final String URL;
    private static String strProcessResult;
    private static String geoDataJson;
    private SentinelBuffferedGeospatialDataDB oSentinelDB;

    static
    {
        METHOD_NAME = "/PostGeospatialData";
        URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";
    }

    public LocationServiceAsyncTask(Context oContext)
    {
        oSentinelDB = new SentinelBuffferedGeospatialDataDB(oContext);
    }

    @Override
    protected String doInBackground(String... strings)
    {

        if (!strings[0].isEmpty())
        {
            geoDataJson = strings[0];
            strProcessResult = ServiceHelper.doPost(METHOD_NAME, URL, geoDataJson);
        }
        return strProcessResult;
    }

    @Override
    protected void onPostExecute(String result)
    {
        if (result == ResponseStatusHelper.OK_RESULT)
        {
            oSentinelDB.deleteGeospatialData();
        }
    }
}