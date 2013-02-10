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
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.services.SentinelLocationService;
import com.sentinel.utils.Time;
import com.sentinel.utils.Utils;

public class SentinelOnBreakActivity extends Activity {
    private static final int NOTIFICATION_ID;

    public static boolean isJunit = false;

    static {
        NOTIFICATION_ID = 1;
    }

    private static Chronometer countdownTimer;
    private static Button btnClockIn;
    private static TextView tvRemaining;
    private static NotificationManager mNotificationManager;
    private static SentinelSharedPreferences mSentinelSharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.breakactivity);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        tvRemaining = (TextView) findViewById(R.id.tvRemaining);
        btnClockIn = (Button) findViewById(R.id.btnClockIn);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSentinelSharedPreferences = new SentinelSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        setUIElementProperties(Color.LTGRAY, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

        setBreak();

        if (mSentinelSharedPreferences.clockedOut()) {
            resumeBreakActivity();
        } else if (isUserAllowedToTakeABreak() && mSentinelSharedPreferences.clockedIn()) {
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
        mNotificationManager.cancel(NOTIFICATION_ID);
        mSentinelSharedPreferences.setClockedIn();

        Intent sentinelIntent = new Intent(this, Sentinel.class);
        sentinelIntent.putExtra(Sentinel.RESUME_SESSION, true);
        sentinelIntent.putExtra(Sentinel.RESUME_MESSAGE, message);

        startActivity(sentinelIntent);
    }

    private void performClockOut() {
        mSentinelSharedPreferences.setClockedOut();
        mSentinelSharedPreferences.setBreakTakenDateTime(System.currentTimeMillis());

        stopLocationService();

        setCowndownTimer();
    }

    private void stopLocationService() {
        stopService(new Intent(this, SentinelLocationService.class));
    }

    private void resumeBreakActivity() {
        long lngBreak = mSentinelSharedPreferences.getBreakLength() - (System.currentTimeMillis() - mSentinelSharedPreferences.getBreakStartDateTime());

        if (lngBreak <= 0) {
            setBreakOverDisplay();
            setBreakOverNotification();
        }
    }

    private void setCowndownTimer() {
        setUIElementProperties(Color.LTGRAY, View.VISIBLE, View.VISIBLE, View.INVISIBLE);

        long breakLength;
        if (!isJunit) {
            breakLength = mSentinelSharedPreferences.getBreakLength();
        } else {
            breakLength = 1000;
        }

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
        mNotificationManager.notify(NOTIFICATION_ID, breakOverNotification);
    }

    private static void setBreak() {
        long sessionBeginAndTimeNowDifference = getSessionBeginAndTimeNowDifference();

        if (drivingFourHoursThirtyMinutes(sessionBeginAndTimeNowDifference))
            mSentinelSharedPreferences.setBreakLength(Time.FORTY_FIVE_MINUTES);
        else
            mSentinelSharedPreferences.setBreakLength(0);
    }

    private static long getSessionBeginAndTimeNowDifference() {
        long timeSessionBegan = mSentinelSharedPreferences.getSessionBeginDateTime();
        long timeNow = System.currentTimeMillis();
        return timeNow - timeSessionBegan;
    }

    private static boolean isUserAllowedToTakeABreak() {
        return 0 < mSentinelSharedPreferences.getBreakLength();
    }

    private static boolean drivingFourHoursThirtyMinutes(final long sessionBeginAndTimeNowDifference) {
        return 0 == mSentinelSharedPreferences.getBreakTakenTime() &&
                sessionBeginAndTimeNowDifference >= Time.FOUR_HOURS_TWENTY &&
                sessionBeginAndTimeNowDifference <= Time.FOUR_HOURS_THIRTY;
    }
}