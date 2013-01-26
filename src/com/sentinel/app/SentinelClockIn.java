package com.sentinel.app;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import com.sentinel.R;
import com.sentinel.preferences.SentinelSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * David Russell
 * 26/01/13
 */
public class SentinelClockIn extends Activity
{
    Chronometer countdownTimer;
    Calendar calendar;
    Button btnClockIn;
    NotificationManager notificationManager;
    SentinelSharedPreferences oSentinelSharedPreferences;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clockin);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        btnClockIn = (Button) findViewById(R.id.btnClockIn);
        calendar = Calendar.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        oSentinelSharedPreferences = new SentinelSharedPreferences(this);

        createCountdownTimer();
    }

    private void createCountdownTimer()
    {
        long lngSessionBegin = oSentinelSharedPreferences.getSessionBeginDateTime();
        long lngNow = calendar.getTimeInMillis();
        long lngDiff = lngSessionBegin - lngNow;
        long lngBreak = calculateBreak(lngDiff);

        btnClockIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent sentinelIntent = new Intent(getApplicationContext(), Sentinel.class);
                startActivity(sentinelIntent);
            }
        });

        setCowndownTimer(lngBreak);
    }

    private long calculateBreak(long lngDiff)
    {
        // driver has been driving for 2 hours (+- 5 minutes)
        if (lngDiff >= 6900000 || lngDiff <= 7500000)
        {
            // break for 15 minutes, set flat that a break of 30 must be taken 2.5 hours later
            return 900000;
        }
        // driver has been driving for 4.5 hours (+- 5 minutes)
        else
            if (lngDiff >= 15900000 || lngDiff <= 16500000)
            {
                // break for 45 minutes
                return 2700000;
            }
            // driver has been driving for 4.75 hours (including 15 minute break +- 5 minutes)
            else
                if (lngDiff >= 16800000 || lngDiff <= 17400000)
                {
                    // break for 30 minutes
                    return 1800000;
                }
                else
                    return 0;
    }

    private void setCowndownTimer(long lngBreak)
    {
        new CountDownTimer(lngBreak, 1000)
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
                btnClockIn.setVisibility(View.VISIBLE);
                countdownTimer.setText("00:00:00");

                Notification oBreakOverNotification = new Notification.Builder(getApplicationContext())
                        .setContentText("Break Finished")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                        .build();
                notificationManager.notify(1, oBreakOverNotification);
            }
        }.start();
    }

}