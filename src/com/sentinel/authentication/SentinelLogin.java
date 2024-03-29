package com.sentinel.authentication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.sentinel.app.R;
import com.sentinel.app.Sentinel;
import com.sentinel.app.SentinelShiftEndingActivity;
import com.sentinel.models.Credentials;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.utils.HttpResponseCode;
import com.sentinel.utils.JsonBuilder;
import com.sentinel.utils.ServiceHelper;
import com.sentinel.utils.Time;

public class SentinelLogin extends Activity {

    public static final String CANCEL_ALARM;

    static {
        CANCEL_ALARM = "CANCEL_ALARM";
    }

    public static boolean isJunit = false;

    private static Credentials mUserCredentials;
    private static String mCredentialsJSONString;
    private static Button btnLogin;
    private static EditText txtUsername;
    private static EditText txtPassword;
    private static ProgressBar pbAsyncProgress;
    private SentinelSharedPreferences mSentinelSharedPreferences;
    private LoginServiceAsyncTask mLoginServiceAsyncTask;

    private class loginConnectionChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager oConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo oNetInfo = oConnectivityManager.getActiveNetworkInfo();
            if (null != oNetInfo && oNetInfo.isConnectedOrConnecting()) {
                setUINoConnectivity(false);
            } else {
                setUINoConnectivity(true);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        registerReceiver(new loginConnectionChangedReceiver(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        Intent intent = getIntent();
        if (intent.getBooleanExtra(CANCEL_ALARM, false)) {
            setAlarm(false);
            intent.removeExtra(CANCEL_ALARM);
        }

        btnLogin = (Button) findViewById(R.id.btn_login);
        txtUsername = (EditText) findViewById(R.id.txt_username);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        pbAsyncProgress = (ProgressBar) findViewById(R.id.pbAsyncProgress);

        btnLogin.setFocusableInTouchMode(true);
        btnLogin.requestFocusFromTouch();

        mSentinelSharedPreferences = new SentinelSharedPreferences(this);

        /* DEBUG */
        txtUsername.setText("DR_DRIVER");
        txtPassword.setText("password");
        /* DEBUG */

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((txtUsername.getText().length() > 0) && (txtPassword.getText().length() > 0)) {
                    setUIElementsEnabled(false);

                    mUserCredentials = new Credentials(txtUsername.getText().toString(), txtPassword.getText().toString());
                    mCredentialsJSONString = JsonBuilder.userCredentialsJson(mUserCredentials);

                    mLoginServiceAsyncTask = new LoginServiceAsyncTask(getApplicationContext());
                    mLoginServiceAsyncTask.execute(mCredentialsJSONString);
                } else if (txtPassword.getText().length() <= 0 && txtUsername.getText().length() > 0) {
                    Toast.makeText(getApplicationContext(), "You must enter a valid password", Toast.LENGTH_SHORT).show();
                } else if (txtUsername.getText().length() <= 0 && txtPassword.getText().length() > 0) {
                    Toast.makeText(getApplicationContext(), "You must enter a valid username", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "You must enter a valid username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSentinelSharedPreferences.clockedIn()) {
            startActivity(new Intent(getApplicationContext(), Sentinel.class));
        }

        if (mLoginServiceAsyncTask != null) {
            AsyncTask.Status loginTaskStatus = mLoginServiceAsyncTask.getStatus();

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

    private static void setUINoConnectivity(boolean noConnectivity) {
        if (noConnectivity) {
            setUIElementsEnabled(false);
            btnLogin.setText(R.string.no_connectivity_title);
        } else {
            setUIElementsEnabled(true);
            btnLogin.setText(R.string.clock_in_title);
        }
    }

    private void startSentinelActivity() {
        mSentinelSharedPreferences.setClockedIn();
        Intent sentinelIntent = new Intent(this, Sentinel.class);
        sentinelIntent.putExtra(Sentinel.NEW_SESSION, true);
        startActivity(sentinelIntent);
    }

    private void setAlarm(boolean setOrCancel) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, SentinelShiftEndingActivity.class);
        intent.putExtra(SentinelShiftEndingActivity.SHIFT_ENDING, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (setOrCancel) {
            long lngEndDrivingAlarm;
            lngEndDrivingAlarm = mSentinelSharedPreferences.getDrivingEndAlarm() - Time.FIVE_MINUTES;
            alarmManager.set(AlarmManager.RTC_WAKEUP, lngEndDrivingAlarm, pendingIntent);
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
                return ServiceHelper.doPost(context, METHOD_NAME, URL, loginCredentialsJson, true);
            } else
                return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(HttpResponseCode.OK_RESULT)) {
                Toast.makeText(context, "Authentication Successful", Toast.LENGTH_SHORT).show();
                if (!isJunit) {
                    mSentinelSharedPreferences.setDrivingEndAlarm(System.currentTimeMillis() + Time.NINE_HOURS);
                } else {
                    mSentinelSharedPreferences.setDrivingEndAlarm(System.currentTimeMillis() + 330000);
                }
                mSentinelSharedPreferences.setSessionBeginDateTime(System.currentTimeMillis());

                setAlarm(true);
                startSentinelActivity();
            } else {
                Toast.makeText(context, "Authentication Unsuccessful", Toast.LENGTH_SHORT).show();
                setUIElementsEnabled(true);
            }
        }
    }
}