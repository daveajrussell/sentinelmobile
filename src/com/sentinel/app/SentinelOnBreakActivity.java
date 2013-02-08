package com.sentinel.app;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import com.sentinel.helper.Utils;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.tracking.SentinelLocationService;

public class SentinelOnBreakActivity extends Activity {
    private static final int NOTIFICATION_ID;
    private static final long FORTY_FIVE_MINUTES;
    private static final long FOUR_HOURS_TWENTY;
    private static final long FOUR_HOURS_THIRTY;

    static {
        NOTIFICATION_ID = 1;
        FORTY_FIVE_MINUTES = 2700000;
        FOUR_HOURS_TWENTY = 15600000;
        FOUR_HOURS_THIRTY = 16200000;
    }

    private static Chronometer countdownTimer;
    private static Button btnClockIn;
    private static TextView tvRemaining;
    private static NotificationManager notificationManager;
    private static SentinelSharedPreferences sentinelSharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clockin);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        tvRemaining = (TextView) findViewById(R.id.tvRemaining);
        btnClockIn = (Button) findViewById(R.id.btnClockIn);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sentinelSharedPreferences = new SentinelSharedPreferences(this);
    }

    protected void onResume() {
        setUIElementProperties(Color.BLACK, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

        setBreak();

        if (sentinelSharedPreferences.clockedOut()) {
            resumeBreakActivity();
        } else if (isUserAllowedToTakeABreak() && sentinelSharedPreferences.clockedIn()) {
            performClockOut();
        } else {
            String message = "You may not begin your recorded break yet.";
            performClockIn(message);
        }

        super.onResume();
    }

    private static void setUIElementProperties(int elementColor, int timerVisible, int textVisible, int buttonVisible) {
        countdownTimer.setVisibility(timerVisible);
        tvRemaining.setVisibility(textVisible);
        btnClockIn.setVisibility(buttonVisible);

        countdownTimer.setTextColor(elementColor);
        tvRemaining.setTextColor(elementColor);
    }

    private void performClockIn(String message) {
        notificationManager.cancel(NOTIFICATION_ID);
        sentinelSharedPreferences.setClockedIn();

        Intent sentinelIntent = new Intent(this, Sentinel.class);
        sentinelIntent.putExtra(Sentinel.RESUME_SESSION, true);
        sentinelIntent.putExtra(Sentinel.RESUME_MESSAGE, message);

        startActivity(sentinelIntent);
    }

    private void performClockOut() {
        sentinelSharedPreferences.setClockedOut();
        sentinelSharedPreferences.setBreakTakenDateTime(System.currentTimeMillis());

        stopLocationService();

        setCowndownTimer();
    }

    private void stopLocationService() {
        stopService(new Intent(this, SentinelLocationService.class));
    }

    private void resumeBreakActivity() {
        long lngBreak = sentinelSharedPreferences.getBreakLength() - (System.currentTimeMillis() - sentinelSharedPreferences.getBreakStartDateTime());

        if (lngBreak <= 0) {
            setBreakOverDisplay();
            setBreakOverNotification();
        }
    }

    private void setCowndownTimer() {
        setUIElementProperties(Color.BLACK, View.VISIBLE, View.VISIBLE, View.INVISIBLE);

        long breakLength = sentinelSharedPreferences.getBreakLength();

        CountDownTimer breakTimer = new CountDownTimer(breakLength, 1000) {
            public void onTick(long millisUntilFinished) {
                String timer = Utils.getFormattedMinsSecsTimeString(millisUntilFinished);
                countdownTimer.setText(timer);
            }

            public void onFinish() {
                setBreakOverDisplay();
                setBreakOverNotification();
            }
        };
        breakTimer.start();
    }

    private void setBreakOverDisplay() {
        countdownTimer.setText("00:00");
        setUIElementProperties(Color.RED, View.VISIBLE, View.VISIBLE, View.VISIBLE);

        btnClockIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                performClockIn(null);
            }
        });
    }

    private void setBreakOverNotification() {
        Intent intent = new Intent(this, SentinelOnBreakActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification breakOverNotification = new NotificationCompat.Builder(this)
                .setContentText("Break Finished")
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setContentIntent(pIntent).build();

        breakOverNotification.flags = Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, breakOverNotification);
    }

    private static void setBreak() {
        long sessionBeginAndTimeNowDifference = getSessionBeginAndTimeNowDifference();

        if (drivingFourHoursThirtyMinutes(sessionBeginAndTimeNowDifference))
            sentinelSharedPreferences.setBreakLength(FORTY_FIVE_MINUTES);
        else
            sentinelSharedPreferences.setBreakLength(0);
    }

    private static long getSessionBeginAndTimeNowDifference() {
        long timeSessionBegan = sentinelSharedPreferences.getSessionBeginDateTime();
        long timeNow = System.currentTimeMillis();
        return timeNow - timeSessionBegan;
    }

    private static boolean isUserAllowedToTakeABreak() {
        return 0 < sentinelSharedPreferences.getBreakLength();
    }

    private static boolean drivingFourHoursThirtyMinutes(final long sessionBeginAndTimeNowDifference) {
        return 0 == sentinelSharedPreferences.getBreakTakenTime() &&
                sessionBeginAndTimeNowDifference >= FOUR_HOURS_TWENTY &&
                sessionBeginAndTimeNowDifference <= FOUR_HOURS_THIRTY;
    }
}