package com.sentinel;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public class LoginServiceAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    static {
        METHOD_NAME = "/Authenticate";
        URL = "http://webservices.daveajrussell.com/LoginService.svc";
    }

    @Override
    protected String doInBackground(String... strings) {

        if(!strings[0].isEmpty()) {
            String strLoginCredentialsJSON = strings[0];

            try {
                HttpClient oLocationServiceHttpClient = new DefaultHttpClient();
                HttpPost oLocationServiceHttpPost = new HttpPost(URL + METHOD_NAME);
                oLocationServiceHttpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

                Log.i("SENTINEL_INFO", "Passing: " + strLoginCredentialsJSON + " to web service");

                StringEntity oStringEntity = new StringEntity(strLoginCredentialsJSON);
                oLocationServiceHttpPost.setEntity(oStringEntity);

                HttpResponse oLocationServiceResponseCode = oLocationServiceHttpClient.execute(oLocationServiceHttpPost);

                Log.i("SentinelWebService", "Response Status: " + oLocationServiceResponseCode.getStatusLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}