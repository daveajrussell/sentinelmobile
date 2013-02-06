package com.sentinel.app;

import android.app.*;
import android.content.DialogInterface;
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

    static {
        NOTIFICATION_ID = 1;
    }

    private static Chronometer countdownTimer;
    private static Button btnClockIn;
    private static TextView tvRemaining;
    private static NotificationManager notificationManager;
    private static SentinelSharedPreferences oSentinelSharedPreferences;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clockin);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        tvRemaining = (TextView) findViewById(R.id.tvRemaining);
        btnClockIn = (Button) findViewById(R.id.btnClockIn);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        oSentinelSharedPreferences = new SentinelSharedPreferences(this);
    }

    protected void onResume() {
        setUIElementProperties(Color.BLACK, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE);

        setBreak();

        if (oSentinelSharedPreferences.clockedOut())
            resumeBreakActivity();
        else if (userIsAllowedToTakeBreak()) {
            if (oSentinelSharedPreferences.clockedIn())
                performClockOut();
        } else {

            //String time = Utils.getFormattedMinsSecsTimeString();
            //String message = String.format("You still have %1$s of your shift remaining. Are you sure you wish to logout?", time);

            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("You may not begin your recorded break yet.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            performClockIn();
                        }
                    }).show();
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

    private void performClockOut() {
        oSentinelSharedPreferences.setClockedOut();
        oSentinelSharedPreferences.setBreakTakenDateTime(System.currentTimeMillis());

        stopLocationService();

        setCowndownTimer();
    }

    private void stopLocationService() {
        stopService(new Intent(this, SentinelLocationService.class));
    }

    private void resumeBreakActivity() {
        long lngBreak = oSentinelSharedPreferences.getBreakLength() - (System.currentTimeMillis() - oSentinelSharedPreferences.getBreakStartDateTime());

        if (lngBreak <= 0) {
            setBreakOverDisplay();
            setBreakOverNotification();
        }
    }

    private void setCowndownTimer() {
        setUIElementProperties(Color.BLACK, View.VISIBLE, View.VISIBLE, View.INVISIBLE);

        long lngBreak = oSentinelSharedPreferences.getBreakLength();

        CountDownTimer breakTimer = new CountDownTimer(lngBreak, 1000) {
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
                performClockIn();
            }
        });
    }

    private void performClockIn() {
        notificationManager.cancel(NOTIFICATION_ID);
        oSentinelSharedPreferences.setClockedIn();
        Intent sentinelIntent = new Intent(this, Sentinel.class);
        sentinelIntent.putExtra("RESUME_SESSION", true);
        startActivity(sentinelIntent);
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
        long sessionDelta = getSessionDelta();

        if ((sessionDelta >= 6900000) && (sessionDelta <= 7500000)) {
            oSentinelSharedPreferences.setBreakLength(900000);
        } else if ((sessionDelta >= 15900000) && (sessionDelta <= 16500000)) {
            oSentinelSharedPreferences.setBreakLength(2700000);
        } else if ((sessionDelta >= 16800000) && (sessionDelta <= 17400000)) {
            oSentinelSharedPreferences.setBreakLength(1800000);
        } else {
            oSentinelSharedPreferences.setBreakLength(0);
        }
    }

    private static long getSessionDelta() {
        long lngSessionBegin = oSentinelSharedPreferences.getSessionBeginDateTime();
        long lngNow = System.currentTimeMillis();
        return lngNow - lngSessionBegin;
    }

    private static boolean userIsAllowedToTakeBreak() {
        return 0 < oSentinelSharedPreferences.getBreakLength();
    }
}