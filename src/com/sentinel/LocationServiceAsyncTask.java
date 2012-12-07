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
 * InternetIO is not meant to be performed on the main thread
 * This AsyncTask is launched whenever there is a need to perform a location update
 * and send data to a web service
 */
public class LocationServiceAsyncTask extends AsyncTask<String, Integer, String> {

    /**
     * David Russell
     * 05/12/12
     * Setup static variables that are initialised once and used throughout the class
     */
    private static final String METHOD_NAME;
    private static final String URL;

    static {
        METHOD_NAME = "/PostGISData";
        URL = "http://webservices.daveajrussell.com/LocationService.svc";
    }

    /**
     * David Russell
     * 05/12/12
     * doInBackground method runs an AsyncTask in the background on the application
     * @param strings
     * Array of parameters passed to the AsyncTask when it is invoked
     * @return
     * empty string, nothing is required to be returned as the calling context is a Service
     */
    @Override
    protected String doInBackground(String... strings) {

        // Check that there is a value in the first index
        if(!strings[0].isEmpty()) {
            String strJSON = strings[0];

            try {
                // Create an HttpClient object to execute the request
                HttpClient oHttpClient = new DefaultHttpClient();
                // Create an HttpPost object to encapsulate the WS method and entity
                HttpPost oPost = new HttpPost(URL + METHOD_NAME);
                // Set the header's content type to json
                oPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

                // Make a log of exactly what is being passed to the web service
                Log.i("SENTINEL_INFO", "Passing: " + strJSON + " to web service");

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