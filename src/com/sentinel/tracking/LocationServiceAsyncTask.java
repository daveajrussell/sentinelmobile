package com.sentinel.tracking;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public class LocationServiceAsyncTask extends AsyncTask<String, Integer, String>
{

    private static final String METHOD_NAME;
    private static final String URL;
    private static String strProcessResult;
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
            String strGeoDataJSON = strings[0];

            try
            {
                HttpClient oLocationServiceHttpClient = new DefaultHttpClient();
                HttpPost oLocationServiceHttpPost = new HttpPost(URL + METHOD_NAME);
                oLocationServiceHttpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

                Log.i("SENTINEL_INFO", "Passing: " + strGeoDataJSON + " to web service");

                StringEntity oStringEntity = new StringEntity(strGeoDataJSON);
                oLocationServiceHttpPost.setEntity(oStringEntity);

                HttpResponse oLocationServiceResponseCode = oLocationServiceHttpClient.execute(oLocationServiceHttpPost);

                Log.i("SentinelWebService", "Response Status: " + oLocationServiceResponseCode.getStatusLine());

                int iStatus = oLocationServiceResponseCode.getStatusLine().getStatusCode();

                switch (iStatus)
                {
                    case ResponseStatusHelper.OK:
                        strProcessResult = ResponseStatusHelper.OK_RESULT;
                        break;
                    default:
                        strProcessResult = ResponseStatusHelper.OTHER_ERROR_RESULT;
                        break;
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
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