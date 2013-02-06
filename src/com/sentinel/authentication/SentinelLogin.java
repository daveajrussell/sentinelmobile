package com.sentinel.authentication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.sentinel.app.R;
import com.sentinel.app.Sentinel;
import com.sentinel.app.SentinelNearingLegalDrivingTimeActivity;
import com.sentinel.connection.ConnectionManager;
import com.sentinel.helper.JsonHelper;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.helper.ServiceHelper;
import com.sentinel.models.Credentials;
import com.sentinel.preferences.SentinelSharedPreferences;

public class SentinelLogin extends Activity {

    public static final String CANCEL_ALARM;

    static {
        CANCEL_ALARM = "CANCEL_ALARM";
    }

    private static Credentials oUserCredentials;
    private static String strCredentialsJSONString;
    private static Button btnLogin;
    private static EditText txtUsername;
    private static EditText txtPassword;
    private static ProgressBar pbAsyncProgress;
    private SentinelSharedPreferences sentinelSharedPreferences;
    private LoginServiceAsyncTask loginServiceAsyncTask;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Intent intent = getIntent();
        if (intent.getBooleanExtra(CANCEL_ALARM, false)) {
            setAlarm(false);
        }

        btnLogin = (Button) findViewById(R.id.btn_login);
        txtUsername = (EditText) findViewById(R.id.txt_username);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        pbAsyncProgress = (ProgressBar) findViewById(R.id.pbAsyncProgress);

        if (!ConnectionManager.deviceIsConnected(this)) {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            btnLogin.setEnabled(false);
        }

        sentinelSharedPreferences = new SentinelSharedPreferences(this);

        /* DEBUG */
        txtUsername.setText("DR_DRIVER");
        txtPassword.setText("password");
        /* DEBUG */

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((txtUsername.getText().length() > 0) && (txtPassword.getText().length() > 0)) {

                    oUserCredentials = new Credentials(txtUsername.getText().toString(), txtPassword.getText().toString());

                    setUIElementsEnabled(false);

                    strCredentialsJSONString = JsonHelper.getUserCredentialsJsonFromCredentials(oUserCredentials);
                }

                loginServiceAsyncTask = new LoginServiceAsyncTask(getApplicationContext());
                loginServiceAsyncTask.execute(strCredentialsJSONString);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sentinelSharedPreferences.clockedIn()) {
            startActivity(new Intent(getApplicationContext(), Sentinel.class));
        }

        if (loginServiceAsyncTask != null) {
            AsyncTask.Status loginTaskStatus = loginServiceAsyncTask.getStatus();

            if (loginTaskStatus == AsyncTask.Status.RUNNING || loginTaskStatus == AsyncTask.Status.PENDING) {
                setUIElementsEnabled(false);
            }
        }
    }

    private static void setUIElementsEnabled(boolean enabled) {
        pbAsyncProgress.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        btnLogin.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
    }

    private void startSentinelActivity() {
        sentinelSharedPreferences.setClockedIn();
        Intent sentinelIntent = new Intent(this, Sentinel.class);
        sentinelIntent.putExtra(Sentinel.NEW_SESSION, true);
        startActivity(sentinelIntent);
    }

    private void setAlarm(boolean setOrCancel) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, SentinelNearingLegalDrivingTimeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (setOrCancel) {
            long lngEndDrivingAlarm = sentinelSharedPreferences.getDrivingEndAlarm();
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lngEndDrivingAlarm, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
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
                Toast.makeText(context, "Authentication Successful", Toast.LENGTH_LONG).show();
                sentinelSharedPreferences.setDrivingEndAlarm(33900000);
                sentinelSharedPreferences.setSessionBeginDateTime(System.currentTimeMillis());

                setAlarm(true);
                startSentinelActivity();
            } else {
                Toast.makeText(context, "Authentication Unsuccessful", Toast.LENGTH_LONG).show();
                setUIElementsEnabled(true);
            }
        }
    }
}