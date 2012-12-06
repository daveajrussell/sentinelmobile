package com.sentinel;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

/**
 * David Russell
 * 05/12/12
 */
public class LocationServiceAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    static {
        METHOD_NAME = "/PostGISData";
        URL = "http://webservices.daveajrussell.com/LocationService.svc";
    }

    @Override
    protected String doInBackground(String... strings) {

        if(!strings[0].isEmpty()) {
            String strJSON = strings[0];

            try {
                // Create an HttpClient object to execute the request
                HttpClient oHttpClient = new DefaultHttpClient();
                // Create an HttpPost object to encapsulate the WS method and entity
                HttpPost oPost = new HttpPost(URL + METHOD_NAME);
                // Set the header's content type to json
                oPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

                Log.i("SENTINTEL_INFO", "Passing: " + strJSON + " to webservice");

                // Create a string entity for this post
                StringEntity oStringEntity = new StringEntity(strJSON);
                oPost.setEntity(oStringEntity);

                // Execute the HttpPost
                HttpResponse oHttpResponse = oHttpClient.execute(oPost);

                // Log the http response
                Log.i("SentinelWebService", "Response Status: " + oHttpResponse.getStatusLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}