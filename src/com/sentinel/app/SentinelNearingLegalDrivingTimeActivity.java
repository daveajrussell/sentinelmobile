package com.sentinel.app;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import com.google.gson.Gson;
import com.sentinel.R;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.tracking.TrackingHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * David Russell
 * 27/01/13
 */
public class SentinelNearingLegalDrivingTimeActivity extends Activity
{
    private GeospatialInformation oLastKnownLocation;
    private Gson oGson = new Gson();
    private Calendar calendar;
    private Chronometer countdownTimer;
    private Button btnClockOut;
    NotificationManager notificationManager;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legaldrivingtimeactivity);

        calendar = Calendar.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        countdownTimer = (Chronometer)findViewById(R.id.countdownTimer);
        btnClockOut = (Button)findViewById(R.id.btnClockOut);
        oLastKnownLocation = TrackingHelper.getLastKnownGeospatialInformation(this);

        String strLastKnowLocationJson = getUserLocationJsonString();
        new NearingLegalDrivingTimeAsyncTask().execute(strLastKnowLocationJson);

        btnClockOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });

        setCowndownTimer(30000);
    }

    private void setCowndownTimer(long lngCount)
    {
        new CountDownTimer(lngCount, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
                calendar.setTimeInMillis(millisUntilFinished);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                String timer = simpleDateFormat.format(calendar.getTime());
                countdownTimer.setText(timer);
            }

            public void onFinish()
            {
                countdownTimer.setText("00:00:00");

                Notification oBreakOverNotification = new Notification.Builder(getApplicationContext())
                        .setContentText("Break Finished")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                        .build();
                notificationManager.notify(1, oBreakOverNotification);

                // alert dialog?
            }
        }.start();
    }

    private String getUserLocationJsonString()
    {
        String strUserLocationJson = "";
        try
        {
            strUserLocationJson =
                    oGson.toJson(
                            new JSONStringer()
                                    .object()
                                    .key("iSessionID").value(oLastKnownLocation.getSessionID())
                                    .key("oUserIdentification").value(oLastKnownLocation.getUserIndentification())
                                    .key("lTimeStamp").value(oLastKnownLocation.getDateTimeStamp())
                                    .key("dLatitude").value(oLastKnownLocation.getLatitude())
                                    .key("dLongitude").value(oLastKnownLocation.getLongitude())
                                    .key("dSpeed").value(oLastKnownLocation.getSpeed())
                                    .key("iOrientation").value(oLastKnownLocation.getOrientation())
                                    .endObject().toString());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return strUserLocationJson;
    }

    private class NearingLegalDrivingTimeAsyncTask extends AsyncTask<String, Integer, String>
    {
        private final String METHOD_NAME = "/NotifyNearingLegalDrivingTime";
        private final String URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";

        @Override
        protected String doInBackground(String... strings)
        {
            String strResult = "";
            String strUserLocationJson;

            if (!strings[0].isEmpty())
            {
                strUserLocationJson = strings[0];

                try
                {
                    HttpClient oLocationServiceHttpClient = new DefaultHttpClient();
                    HttpPost oLocationServiceHttpPost = new HttpPost(URL + METHOD_NAME);
                    oLocationServiceHttpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

                    Log.i("SENTINEL_INFO", "Passing: " + strUserLocationJson + " to web service");

                    StringEntity oStringEntity = new StringEntity(strUserLocationJson);
                    oLocationServiceHttpPost.setEntity(oStringEntity);

                    HttpResponse oLocationServiceResponseCode = oLocationServiceHttpClient.execute(oLocationServiceHttpPost);

                    Log.i("SentinelWebService", "Response Status: " + oLocationServiceResponseCode.getStatusLine());

                    int iStatus = oLocationServiceResponseCode.getStatusLine().getStatusCode();

                    switch (iStatus)
                    {
                        case ResponseStatusHelper.OK:
                            strResult = ResponseStatusHelper.OK_RESULT;
                            break;
                        default:
                            strResult = ResponseStatusHelper.OTHER_ERROR_RESULT;
                            break;
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            return strResult;
        }
    }
}