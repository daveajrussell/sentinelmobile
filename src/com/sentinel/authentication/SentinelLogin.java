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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sentinel.R;
import com.sentinel.app.Sentinel;
import com.sentinel.app.SentinelNearingLegalDrivingTimeActivity;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.models.Credentials;
import com.sentinel.models.User;
import com.sentinel.preferences.SentinelSharedPreferences;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONStringer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class SentinelLogin extends Activity
{
    private Credentials oUserCredentials;
    private String strCredentialsJSONString;

    private Button btnLogin;
    private EditText txtUsername;
    private EditText txtPassword;
    private ProgressBar pbAsyncProgress;
    private AlarmManager alarmManager;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        btnLogin = (Button) findViewById(R.id.btn_login);
        txtUsername = (EditText) findViewById(R.id.txt_username);
        txtPassword = (EditText) findViewById(R.id.txt_password);

        pbAsyncProgress = (ProgressBar) findViewById(R.id.pbAsyncProgress);

        /* DEBUG */
        txtUsername.setText("DR_ARCHITECT");
        txtPassword.setText("randomness");
        /* DEBUG */

        //long lngEndDrivingAlarm = oSentinelSharedPreferences.getDrivingEndAlarm();
        Intent intent = new Intent(SentinelLogin.this, SentinelNearingLegalDrivingTimeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000/*lngEndDrivingAlarm*/, pendingIntent);

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

                    strCredentialsJSONString = convertCredentialsObjectToJSONString(oUserCredentials);
                }

                new LoginServiceAsyncTask(SentinelLogin.this).execute(strCredentialsJSONString);
            }
        });

    }

    public String convertCredentialsObjectToJSONString(Credentials oCredentials)
    {
        Gson oGson = new Gson();
        JSONStringer oCredentialsStringer;
        String strCredentialsJSON = null;

        try
        {
            oCredentialsStringer = new JSONStringer()
                    .object()
                    .key("strUsername").value(oCredentials.getUsername())
                    .key("strPassword").value(oCredentials.getPassword())
                    .endObject();

            strCredentialsJSON = oGson.toJson(oCredentialsStringer.toString());

        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        return strCredentialsJSON;
    }

    private class LoginServiceAsyncTask extends AsyncTask<String, Integer, String>
    {

        private final String METHOD_NAME = "/Authenticate";
        private final String URL = "http://webservices.daveajrussell.com/Services/AuthenticationService.svc";

        private Context oContext;
        private String strJson;
        private String strProcessResult;
        private User oUser;
        private Gson oGson;
        private PendingIntent drivingTimePendingIntent;

        public LoginServiceAsyncTask(Context context)
        {
            oContext = context;
        }

        @Override
        protected String doInBackground(String... strings)
        {

            if (!strings[0].isEmpty())
            {
                String strLoginCredentialsJSON = strings[0];

                try
                {
                    HttpClient oLoginServiceHttpClient = new DefaultHttpClient();
                    HttpPost oLoginServiceHttpPost = new HttpPost(URL + METHOD_NAME);
                    oLoginServiceHttpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

                    Log.i("SENTINEL_INFO", "Passing: " + strLoginCredentialsJSON + " to web service");


                    StringEntity oStringEntity = new StringEntity(strLoginCredentialsJSON);
                    oLoginServiceHttpPost.setEntity(oStringEntity);

                    HttpResponse oLoginServiceResponse = oLoginServiceHttpClient.execute(oLoginServiceHttpPost);

                    int iStatus = oLoginServiceResponse.getStatusLine().getStatusCode();

                    Log.i("SentinelWebService", "Response Status: " + oLoginServiceResponse.getStatusLine());

                    switch (iStatus)
                    {
                        case ResponseStatusHelper.OK:
                            loginToSystem(oLoginServiceResponse);
                            strProcessResult = ResponseStatusHelper.OK_RESULT;
                            break;
                        case ResponseStatusHelper.BAD_REQUEST:
                            strProcessResult = ResponseStatusHelper.BAD_REQUEST_RESULT;
                            break;
                        case ResponseStatusHelper.UNAUTHORIZED:
                            strProcessResult = ResponseStatusHelper.UNAUTHORIZED_RESULT;
                            break;
                        case ResponseStatusHelper.NOT_FOUND:
                            strProcessResult = ResponseStatusHelper.NOT_FOUND_RESULT;
                            break;
                        case ResponseStatusHelper.INTERNAL_SERVER_ERROR:
                            strProcessResult = ResponseStatusHelper.INTERNAL_SERVER_ERROR_RESULT;
                            break;
                        default:
                            strProcessResult = ResponseStatusHelper.OTHER_ERROR_RESULT;
                            break;
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return strProcessResult;
        }

        @Override
        protected void onPostExecute(String result)
        {

            if (result == ResponseStatusHelper.OK_RESULT)
            {
                SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(oContext);
                String strUserIdentification = oUser.getUserIdentification();
                int iSessionID = oUser.getSessionID();

                oSentinelSharedPreferences.setUserPreferences(strUserIdentification, iSessionID);
                oSentinelSharedPreferences.setNextAlarm(6900000);
                oSentinelSharedPreferences.setDrivingEndAlarm(33900000);

                Intent sentinelIntent = new Intent(oContext, Sentinel.class);
                sentinelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                oContext.startActivity(sentinelIntent);

                /*long lngEndDrivingAlarm = oSentinelSharedPreferences.getDrivingEndAlarm();
                Intent intent = new Intent(this, SentinelClockIn.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(oContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + lngEndDrivingAlarm, pendingIntent);  */
            }
            else
            {
                AlertDialog.Builder oAuthenticationAlert = new AlertDialog.Builder(oContext);
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
                }
                else
                {
                    oAuthenticationAlert.setMessage("An error has occurred. Please try again later.");
                }

                oAuthenticationAlert.show();
            }
        }

        private void loginToSystem(HttpResponse oResponse)
        {
            try
            {
                oGson = new Gson();
                JsonParser parser = new JsonParser();
                JsonObject oUserJsonObject;

                HttpEntity oLoginServiceEntity = oResponse.getEntity();
                InputStream oLoginServiceResponseStream = oLoginServiceEntity.getContent();
                Reader oLoginServiceReader = new InputStreamReader(oLoginServiceResponseStream);

                strJson = oGson.fromJson(oLoginServiceReader, String.class);
                oUserJsonObject = parser.parse(strJson).getAsJsonObject();

                oUser = new User();
                oUser.setUserIdentification(oGson.fromJson(oUserJsonObject.get("UserKey"), String.class));
                oUser.setSessionID(oGson.fromJson(oUserJsonObject.get("SessionID"), int.class));
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
}