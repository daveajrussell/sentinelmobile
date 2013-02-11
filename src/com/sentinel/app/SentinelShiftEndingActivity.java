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
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.utils.*;

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
    private static NotificationManager mNotificationManager;
    private static SentinelSharedPreferences mSentinelSharedPreferences;
    private static AlarmManager mAlarmManager;
    private static long mShiftEnding;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shiftendingactivity);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        tvRemaining = (TextView) findViewById(R.id.tvRemaining);
        btnClockOut = (Button) findViewById(R.id.btnClockOut);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSentinelSharedPreferences = new SentinelSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        setAlarm(false);
        setUIElementProperties(Color.LTGRAY, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

        if (mSentinelSharedPreferences.shiftEnding()) {
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
            mSentinelSharedPreferences.setShiftEnding(true);
            intent.removeExtra(SHIFT_ENDING);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != mSentinelSharedPreferences && mSentinelSharedPreferences.shiftEnding()) {
            setAlarm(true);
        } else {
            setAlarm(false);
        }
    }

    private void startShiftEndingActivity() {
        Intent intent = new Intent(this, Sentinel.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification breakOverNotification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Sentinel")
                .setContentText("Your shift is ending")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, breakOverNotification);

        setCowndownTimer();
    }

    private void setAlarm(boolean setOrCancel) {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, SentinelShiftEndingActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (setOrCancel) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, mShiftEnding, pendingIntent);
        } else {
            mAlarmManager.cancel(pendingIntent);
        }
    }

    private void resumeShiftEndingActivity() {
        setUIElementProperties(Color.LTGRAY, View.VISIBLE, View.VISIBLE, View.INVISIBLE);
        long shiftEnding = mSentinelSharedPreferences.getDrivingEndAlarm() - System.currentTimeMillis();

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
        setUIElementProperties(Color.LTGRAY, View.VISIBLE, View.VISIBLE, View.INVISIBLE);

        if (!isJunit) {
            mShiftEnding = mSentinelSharedPreferences.getDrivingEndAlarm() - System.currentTimeMillis();
        } else {
            mShiftEnding = 10000;
        }

        CountDownTimer breakTimer = new CountDownTimer(mShiftEnding, 1000) {
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

        mSentinelSharedPreferences.setShiftEnding(false);
        mSentinelSharedPreferences.setShiftEnded(true);

        btnClockOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNotificationManager.cancel(NOTIFICATION_ID);
                setAlarm(false);
                AuthenticationHelper.performLogout(getApplicationContext());
            }
        });
    }

    private void setShiftOverNotification() {
        /*mNotificationManager.cancel(NOTIFICATION_ID);
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
        mNotificationManager.notify(NOTIFICATION_ID, breakOverNotification);*/
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private class NearingLegalDrivingTimeAsyncTask extends AsyncTask<String, Integer, String> {
        private final String METHOD_NAME = "/NotifyNearingLegalDrivingTime";
        private final String URL = "http://webservices.daveajrussell.com/Services/LocationService.svc";

        @Override
        protected String doInBackground(String... strings) {
            String userLocationJson;

            if (!strings[0].isEmpty()) {
                userLocationJson = strings[0];

                ServiceHelper.doPost(null, METHOD_NAME, URL, userLocationJson, false);
            }

            return "";
        }
    }
}