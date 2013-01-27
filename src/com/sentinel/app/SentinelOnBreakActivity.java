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
public class SentinelOnBreakActivity extends Activity
{
    public static final String BREAK_LENGTH = "BREAK_LENGTH";

    private Chronometer countdownTimer;
    private Calendar calendar;
    private Button btnClockIn;
    private NotificationManager notificationManager;
    private SentinelSharedPreferences oSentinelSharedPreferences;
    private long breakLength;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clockin);

        Intent intent = getIntent();
        breakLength = intent.getLongExtra(BREAK_LENGTH, 0);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        btnClockIn = (Button) findViewById(R.id.btnClockIn);
        calendar = Calendar.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        oSentinelSharedPreferences = new SentinelSharedPreferences(this);

        createCountdownTimer();
    }

    private void updateAlarms()
    {

    }

    private void createCountdownTimer()
    {
        btnClockIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent sentinelIntent = new Intent(getApplicationContext(), Sentinel.class);
                startActivity(sentinelIntent);
            }
        });

        oSentinelSharedPreferences.setBreakTakenDateTime(System.currentTimeMillis());
        setCowndownTimer(breakLength);
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