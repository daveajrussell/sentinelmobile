package com.sentinel.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.helper.ServiceHelper;
import com.sentinel.tracking.SentinelLocationService;

/**
 * David Russell
 * 27/01/13
 */
public class LogoutAsyncTask extends AsyncTask<String, Integer, String>
{
    private static final String METHOD_NAME;
    private static final String URL;
    static
    {
        METHOD_NAME = "/Logout";
        URL = "http://webservices.daveajrussell.com/Services/AuthenticationService.svc";
    }
    private Context context;
    private String userCredentialsJson;

    public LogoutAsyncTask(Context context)
    {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings)
    {
        if (strings[0] != null)
        {
            userCredentialsJson = strings[0];
            return ServiceHelper.doPost(METHOD_NAME, URL, userCredentialsJson);
        } else
            return "";
    }

    @Override
    protected void onPostExecute(String result)
    {
        if (result == ResponseStatusHelper.OK_RESULT)
        {
            Intent locationServiceIntent = new Intent(context, SentinelLocationService.class);
            context.stopService(locationServiceIntent);

            Intent loginIntent = new Intent(context, SentinelLogin.class);
            loginIntent.putExtra(SentinelLogin.CANCEL_ALARM, true);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(loginIntent);
        }
    }
}