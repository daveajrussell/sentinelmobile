package com.sentinel.authentication;

import android.os.AsyncTask;
import com.sentinel.utils.ServiceHelper;

/**
 * David Russell
 * 27/01/13
 */
public class LogoutAsyncTask extends AsyncTask<String, Integer, String> {
    private static final String METHOD_NAME;
    private static final String URL;

    static {
        METHOD_NAME = "/Logout";
        URL = "http://webservices.daveajrussell.com/Services/AuthenticationService.svc";
    }

    @Override
    protected String doInBackground(String... strings) {
        if (strings[0] != null) {
            String userCredentialsJson = strings[0];
            return ServiceHelper.doPost(null, METHOD_NAME, URL, userCredentialsJson, false);
        } else
            return "";
    }
}