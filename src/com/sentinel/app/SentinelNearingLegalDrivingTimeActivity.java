package com.sentinel.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import com.google.gson.Gson;
import com.sentinel.R;
import com.sentinel.authentication.ClockOutAsyncTask;
import com.sentinel.helper.ServiceHelper;
import com.sentinel.helper.TrackingHelper;
import com.sentinel.models.GeospatialInformation;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * David Russell
 * 27/01/13
 */
public class SentinelNearingLegalDrivingTimeActivity extends Activity
{
    private NotificationManager notificationManager;
    private GeospatialInformation geospatialInformation;
    private Gson oGson = new Gson();
    private Calendar calendar;
    private Chronometer countdownTimer;
    private Button btnClockOut;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legaldrivingtimeactivity);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        btnClockOut = (Button) findViewById(R.id.btnClockOut);

        calendar = Calendar.getInstance();
        geospatialInformation = TrackingHelper.getLastKnownGeospatialInformation(this);

        final String strLastKnowLocationJson = getUserLocationJsonString();


        btnClockOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new ClockOutAsyncTask(getApplicationContext()).execute(strLastKnowLocationJson);
            }
        });

        setCowndownTimer(30000);

        new NearingLegalDrivingTimeAsyncTask().execute(strLastKnowLocationJson);
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
                        .setContentText("Shift Ending in 5 Minutes")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                        .build();
                notificationManager.notify(1, oBreakOverNotification);

                AlertDialog.Builder oResultDialog = new AlertDialog.Builder(getApplicationContext());
                oResultDialog.setTitle("Shift Ending");
                oResultDialog.setMessage("Your shift is scheduled to end in 5 minutes.");
                oResultDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        // do nothing
                    }
                });
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
                                    .key("iSessionID").value(geospatialInformation.getSessionID())
                                    .key("oUserIdentification").value(geospatialInformation.getUserIndentification())
                                    .key("lTimeStamp").value(geospatialInformation.getDateTimeStamp())
                                    .key("dLatitude").value(geospatialInformation.getLatitude())
                                    .key("dLongitude").value(geospatialInformation.getLongitude())
                                    .key("dSpeed").value(geospatialInformation.getSpeed())
                                    .key("iOrientation").value(geospatialInformation.getOrientation())
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
            String userLocationJson;

            if (!strings[0].isEmpty())
            {
                userLocationJson = strings[0];

                ServiceHelper.doPost(METHOD_NAME, URL, userLocationJson);
            }

            return "";
        }
    }
}