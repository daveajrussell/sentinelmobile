package com.sentinel.authentication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.sentinel.R;
import com.sentinel.app.Sentinel;
import com.sentinel.app.SentinelNearingLegalDrivingTimeActivity;
import com.sentinel.helper.JsonHelper;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.helper.ServiceHelper;
import com.sentinel.models.Credentials;
import com.sentinel.preferences.SentinelSharedPreferences;

public class SentinelLogin extends Activity {

    private static Credentials oUserCredentials;
    private static String strCredentialsJSONString;
    private static Button btnLogin;
    private static EditText txtUsername;
    private static EditText txtPassword;
    private static ProgressBar pbAsyncProgress;
    private static AlarmManager alarmManager;
    private static JsonHelper jsonHelper;
    private static SentinelSharedPreferences sentinelSharedPreferences;
    private static PendingIntent pendingIntent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        sentinelSharedPreferences = new SentinelSharedPreferences(this);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        btnLogin = (Button) findViewById(R.id.btn_login);
        txtUsername = (EditText) findViewById(R.id.txt_username);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        pbAsyncProgress = (ProgressBar) findViewById(R.id.pbAsyncProgress);
        sentinelSharedPreferences = new SentinelSharedPreferences(this);
        jsonHelper = new JsonHelper(this);

        /* DEBUG */
        txtUsername.setText("DR_DRIVER");
        txtPassword.setText("password");
        /* DEBUG */

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((txtUsername.getText().length() > 0) && (txtPassword.getText().length() > 0)) {

                    oUserCredentials = new Credentials(txtUsername.getText().toString(), txtPassword.getText().toString());

                    pbAsyncProgress.setVisibility(View.VISIBLE);
                    btnLogin.setEnabled(false);
                    txtUsername.setEnabled(false);
                    txtPassword.setEnabled(false);

                    strCredentialsJSONString = jsonHelper.getUserCredentialsJsonFromCredentials(oUserCredentials);
                }

                new LoginServiceAsyncTask(SentinelLogin.this).execute(strCredentialsJSONString);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sentinelSharedPreferences.clockedIn()) {
            startActivity(new Intent(getApplicationContext(), Sentinel.class));
        }
    }

    private class LoginServiceAsyncTask extends AsyncTask<String, Integer, String> {
        private final String METHOD_NAME = "/Authenticate";
        private final String URL = "http://webservices.daveajrussell.com/Services/AuthenticationService.svc";
        private Context context;
        private String loginCredentialsJson;

        public LoginServiceAsyncTask(Context context) {
            this.context = context;
        }

        private void setAlarm() {
            long lngEndDrivingAlarm = sentinelSharedPreferences.getDrivingEndAlarm();
            Intent intent = new Intent(context, SentinelNearingLegalDrivingTimeActivity.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lngEndDrivingAlarm, pendingIntent);
        }

        private void startSentinelActivity() {
            sentinelSharedPreferences.setClockedIn();
            Intent sentinelIntent = new Intent(context, Sentinel.class);
            sentinelIntent.putExtra(Sentinel.NEW_SESSION, true);
            startActivity(sentinelIntent);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (!strings[0].isEmpty()) {
                loginCredentialsJson = strings[0];
                return ServiceHelper.doPostAndLogin(context, METHOD_NAME, URL, loginCredentialsJson);
            } else
                return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(ResponseStatusHelper.OK_RESULT)) {
                sentinelSharedPreferences.setDrivingEndAlarm(33900000);
                sentinelSharedPreferences.setSessionBeginDateTime(System.currentTimeMillis());

                setAlarm();
                startSentinelActivity();
            } else {
                showFailureDialog(result);
            }
        }

        private void showFailureDialog(final String result) {
            new AlertDialog.Builder(context)
                    .setTitle("Authentication Failed")
                    .setMessage(result.equals(ResponseStatusHelper.NOT_FOUND_RESULT) ?
                            "Invalid Login Details"
                            :
                            "An error has occured. Please try again later")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pbAsyncProgress.setVisibility(View.INVISIBLE);
                            btnLogin.setEnabled(true);
                            txtUsername.setEnabled(true);
                            txtPassword.setEnabled(true);
                        }
                    }).show();
        }
    }
}