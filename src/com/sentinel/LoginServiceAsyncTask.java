package com.sentinel;

import android.os.AsyncTask;

public class LoginServiceAsyncTask extends AsyncTask<String, Integer, String> {

    private static final String METHOD_NAME;
    private static final String URL;

    static {
        METHOD_NAME = "/Authenticate";
        URL = "http://webservices.daveajrussell.com/LoginService.svc";
    }

    @Override
    protected String doInBackground(String... strings) {
        return null;
    }
}