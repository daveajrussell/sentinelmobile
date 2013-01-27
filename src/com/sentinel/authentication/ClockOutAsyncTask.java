package com.sentinel.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.helper.ServiceHelper;

/**
 * David Russell
 * 27/01/13
 */
public class ClockOutAsyncTask extends AsyncTask<String, Integer, String>
{
    private Context context;
    private String userCredentialsJson;
    private static final String METHOD;
    private static final String URL;

    static
    {
        METHOD = "http://webservices.daveajrussell.com/Services/AuthenticationService.svc";
        URL = "/Logout";
    }

    public ClockOutAsyncTask(Context context)
    {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings)
    {
        if (strings[0] != null)
        {
            userCredentialsJson = strings[0];
            return ServiceHelper.doPost(METHOD, URL, userCredentialsJson);
        }
        else
            return "";
    }

    @Override
    protected void onPostExecute(String result)
    {
        if (result == ResponseStatusHelper.OK_RESULT)
        {
            Intent intent = new Intent(context, SentinelLogin.class);
            context.startActivity(intent);
        }
    }
}