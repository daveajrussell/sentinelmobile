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

public class SentinelLogin extends Activity
{
    public static final String CANCEL_ALARM = "CANCEL_ALARM";
    private Credentials oUserCredentials;
    private String strCredentialsJSONString;
    private Button btnLogin;
    private EditText txtUsername;
    private EditText txtPassword;
    private ProgressBar pbAsyncProgress;
    private AlarmManager alarmManager;
    private JsonHelper jsonHelper;
    private SentinelSharedPreferences sentinelSharedPreferences;
    private PendingIntent pendingIntent;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Intent intent = getIntent();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        btnLogin = (Button) findViewById(R.id.btn_login);
        txtUsername = (EditText) findViewById(R.id.txt_username);
        txtPassword = (EditText) findViewById(R.id.txt_password);
        pbAsyncProgress = (ProgressBar) findViewById(R.id.pbAsyncProgress);
        sentinelSharedPreferences = new SentinelSharedPreferences(this);
        jsonHelper = new JsonHelper(this);

        if (intent.getBooleanExtra(CANCEL_ALARM, false))
            cancelAlarm();

        /* DEBUG */
        txtUsername.setText("DR_DRIVER");
        txtPassword.setText("password");
        /* DEBUG */

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if ((txtUsername.getText().length() > 0) && (txtPassword.getText().length() > 0))
                {

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

    private void cancelAlarm()
    {
        Intent intent = new Intent(this, SentinelNearingLegalDrivingTimeActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private class LoginServiceAsyncTask extends AsyncTask<String, Integer, String>
    {
        private final String METHOD_NAME = "/Authenticate";
        private final String URL = "http://webservices.daveajrussell.com/Services/AuthenticationService.svc";
        private Context context;
        private String loginCredentialsJson;

        public LoginServiceAsyncTask(Context context)
        {
            this.context = context;
        }

        private void setAlarm()
        {
            long lngEndDrivingAlarm = sentinelSharedPreferences.getDrivingEndAlarm();
            Intent intent = new Intent(getApplicationContext(), SentinelNearingLegalDrivingTimeActivity.class);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lngEndDrivingAlarm, pendingIntent);
        }

        @Override
        protected String doInBackground(String... strings)
        {
            if (!strings[0].isEmpty())
            {
                loginCredentialsJson = strings[0];
                return ServiceHelper.doPostAndLogin(getApplicationContext(), METHOD_NAME, URL, loginCredentialsJson);
            } else
                return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (result == ResponseStatusHelper.OK_RESULT)
            {
                sentinelSharedPreferences.setDrivingEndAlarm(33900000);
                sentinelSharedPreferences.setSessionBeginDateTime(System.currentTimeMillis());

                setAlarm();

                Intent sentinelIntent = new Intent(context, Sentinel.class);
                sentinelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(sentinelIntent);
            } else
            {
                AlertDialog.Builder oAuthenticationAlert = new AlertDialog.Builder(context);
                oAuthenticationAlert.setTitle("Authentication Failed");
                oAuthenticationAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        pbAsyncProgress.setVisibility(View.INVISIBLE);
                        btnLogin.setEnabled(true);
                        txtUsername.setEnabled(true);
                        txtPassword.setEnabled(true);
                    }
                });

                if (result == ResponseStatusHelper.UNAUTHORIZED_RESULT)
                {
                    oAuthenticationAlert.setMessage("Invalid Login Details.");
                } else
                {
                    oAuthenticationAlert.setMessage("An error has occurred. Please try again later.");
                }

                oAuthenticationAlert.show();
            }
        }
    }
}