package com.sentinel;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class LoginServiceAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    private Context oContext;
    private String strJSONReturnValue;

    static {
        METHOD_NAME = "/Authenticate";
        URL = "http://webservices.daveajrussell.com/Services/LoginService.svc";
    }

    public LoginServiceAsyncTask(Context context) {
        oContext = context;
    }

    @Override
    protected String doInBackground(String... strings) {

        if(!strings[0].isEmpty()) {
            String strLoginCredentialsJSON = strings[0];

            try {
                Gson oGson = new Gson();
                HttpClient oLoginServiceHttpClient = new DefaultHttpClient();
                HttpPost oLoginServiceHttpPost = new HttpPost(URL + METHOD_NAME);
                oLoginServiceHttpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

                Log.i("SENTINEL_INFO", "Passing: " + strLoginCredentialsJSON + " to web service");

                StringEntity oStringEntity = new StringEntity(strLoginCredentialsJSON);
                oLoginServiceHttpPost.setEntity(oStringEntity);

                HttpResponse oLoginServiceResponse = oLoginServiceHttpClient.execute(oLoginServiceHttpPost);

                Log.i("SentinelWebService", "Response Status: " + oLoginServiceResponse.getStatusLine());

                HttpEntity oLoginServiceResponseEntity = oLoginServiceResponse.getEntity();

                InputStream oLoginServiceResponseStream = oLoginServiceResponseEntity.getContent();
                Reader oLoginServiceReader = new InputStreamReader(oLoginServiceResponseStream);
                strJSONReturnValue = oGson.fromJson(oLoginServiceReader, String.class);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        Intent intent = new Intent(LoginServiceBroadcastReceiver.CREDENTIALS_AUTHENTICATION);
        intent.putExtra(LoginServiceBroadcastReceiver.USER_IDENTIFICATION, strJSONReturnValue);
        oContext.sendBroadcast(intent);
    }
}