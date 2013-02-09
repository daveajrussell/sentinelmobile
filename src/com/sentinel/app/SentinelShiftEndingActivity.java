package com.sentinel.app;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import com.sentinel.helper.*;
import com.sentinel.preferences.SentinelSharedPreferences;

public class SentinelShiftEndingActivity extends Activity {

    private static final int NOTIFICATION_ID;
    public static final String ALERT_SENT;
    public static final String SHIFT_ENDING;

    static {
        NOTIFICATION_ID = 1;
        ALERT_SENT = "ALERT_SENT";
        SHIFT_ENDING = "SHIFT_ENDING";
    }

    public static boolean isJunit = false;

    private static Chronometer countdownTimer;
    private static Button btnClockOut;
    private static TextView tvRemaining;
    private static NotificationManager notificationManager;
    private static SentinelSharedPreferences sentinelSharedPreferences;
    private static long shiftEnding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shiftendingactivity);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        tvRemaining = (TextView) findViewById(R.id.tvRemaining);
        btnClockOut = (Button) findViewById(R.id.btnClockOut);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sentinelSharedPreferences = new SentinelSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        setUIElementProperties(Color.BLACK, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

        if (sentinelSharedPreferences.shiftEnding()) {
            resumeShiftEndingActivity();
        } else {
            startShiftEndingActivity();
        }

        Intent intent = getIntent();

        if (!intent.getBooleanExtra(ALERT_SENT, true)) {
            Location location = TrackingHelper.lastKnownLocation(this);
            String geospatialJson = JsonBuilder.geospatialDataJson(this, location);
            new NearingLegalDrivingTimeAsyncTask().execute(geospatialJson);
            intent.removeExtra(ALERT_SENT);
        }

        if (intent.getBooleanExtra(SHIFT_ENDING, true)) {
            sentinelSharedPreferences.setShiftEnding(true);
            intent.removeExtra(SHIFT_ENDING);
        }

        setAlarm(false);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setAlarm(true);
    }

    private void startShiftEndingActivity() {
        Notification breakOverNotification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Sentinel")
                .setContentText("Your shift is ending")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setAutoCancel(true)
                .build();
        notificationManager.notify(NOTIFICATION_ID, breakOverNotification);

        setCowndownTimer();
    }

    private void setAlarm(boolean setOrCancel) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, SentinelShiftEndingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (setOrCancel) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, shiftEnding, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void resumeShiftEndingActivity() {
        setUIElementProperties(Color.BLACK, View.VISIBLE, View.VISIBLE, View.INVISIBLE);
        long shiftEnding = sentinelSharedPreferences.getDrivingEndAlarm() - System.currentTimeMillis();

        if (shiftEnding <= 0) {
            setShiftOverDisplay();
            setShiftOverNotification();
        }
    }

    private static void setUIElementProperties(int elementColor, int timerVisible, int textVisible, int buttonVisible) {
        countdownTimer.setVisibility(timerVisible);
        tvRemaining.setVisibility(textVisible);
        btnClockOut.setVisibility(buttonVisible);

        countdownTimer.setTextColor(elementColor);
        tvRemaining.setTextColor(elementColor);
    }

    private void setCowndownTimer() {
        setUIElementProperties(Color.BLACK, View.VISIBLE, View.VISIBLE, View.INVISIBLE);

        if (!isJunit) {
            shiftEnding = sentinelSharedPreferences.getDrivingEndAlarm() - System.currentTimeMillis();
        } else {
            shiftEnding = 1000;
        }

        CountDownTimer breakTimer = new CountDownTimer(shiftEnding, 1000) {
            public void onTick(long millisUntilFinished) {
                String timer = Utils.getFormattedMinsSecsTimeString(millisUntilFinished);
                countdownTimer.setText(timer);
            }

            public void onFinish() {
                setShiftOverDisplay();
                setShiftOverNotification();
            }
        };
        breakTimer.start();
    }

    private void setShiftOverDisplay() {
        countdownTimer.setText("00:00");
        setUIElementProperties(Color.RED, View.VISIBLE, View.VISIBLE, View.VISIBLE);

        sentinelSharedPreferences.setShiftEnded(true);

        btnClockOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationManager.cancel(NOTIFICATION_ID);
                AuthenticationHelper.performLogout(getApplicationContext());
            }
        });
    }

    private void setShiftOverNotification() {
        Intent intent = new Intent(this, Sentinel.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification breakOverNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Sentinel")
                .setContentText("Shift Finished")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setContentIntent(pIntent).build();

        breakOverNotification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, breakOverNotification);
    }

    private class NearingLegalDrivingTimeAsyncTask extends AsyncTask<String, Integer, String> {
        private final String METHOD_NAME = "/NotifyNearingLegalDrivingTime";
        private final String URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";

        @Override
        protected String doInBackground(String... strings) {
            String userLocationJson;

            if (!strings[0].isEmpty()) {
                userLocationJson = strings[0];

                ServiceHelper.doPost(METHOD_NAME, URL, userLocationJson);
            }

            return "";
        }
    }
}