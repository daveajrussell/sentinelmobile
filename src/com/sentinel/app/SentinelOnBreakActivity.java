package com.sentinel.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
    public static final String NEXT_BREAK_LENGTH = "NEXT_BREAK_LENGTH";
    public static final String NEXT_BREAK = "NEXT_BREAK";

    private Chronometer countdownTimer;
    private Calendar calendar;
    private Button btnClockIn;
    private NotificationManager notificationManager;
    private SentinelSharedPreferences oSentinelSharedPreferences;
    private SimpleDateFormat simpleDateFormat;
    private AlertDialog.Builder alertDialog;

    private static long lngBreakLength;
    private static long lngNextBreakLength;
    private static long lngNextBreak;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clockin);

        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        Intent intent = getIntent();
        lngBreakLength = intent.getLongExtra(BREAK_LENGTH, 0);
        lngNextBreakLength = intent.getLongExtra(NEXT_BREAK_LENGTH, 0);
        lngNextBreak = intent.getLongExtra(NEXT_BREAK, 0);

        countdownTimer = (Chronometer) findViewById(R.id.countdownTimer);
        btnClockIn = (Button) findViewById(R.id.btnClockIn);
        calendar = Calendar.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        oSentinelSharedPreferences = new SentinelSharedPreferences(this);

        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Break Finished")
                .setMessage(getMessageForDialog())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                });

        createCountdownTimer();
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
        setCowndownTimer();
    }


    private void setCowndownTimer()
    {
        new CountDownTimer(lngBreakLength, 1000)
        {
            public void onTick(long millisUntilFinished)
            {
                calendar.setTimeInMillis(millisUntilFinished);
                String timer = simpleDateFormat.format(calendar.getTime());
                countdownTimer.setText(timer);
            }

            public void onFinish()
            {
                btnClockIn.setVisibility(View.VISIBLE);
                countdownTimer.setText("00:00:00");
                countdownTimer.setTextColor(Color.RED);
                alertDialog.show();

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

    private String getMessageForDialog()
    {
        if (lngNextBreak != 0 && lngNextBreakLength != 0)
        {
            return "You took a break of " + millisToMinutes(lngBreakLength) + " minutes. You must take another break in " + millisToHours(lngNextBreak) + " hours, for " + millisToMinutes(lngNextBreakLength) + " minutes.";
        } else
        {
            return "You took a break of " + millisToMinutes(lngBreakLength) + " minutes. You are not entitled to any further breaks for the remainder of your shift";
        }
    }

    private long millisToMinutes(long lngVal)
    {
        return ((lngVal / 1000) / 60);
    }

    private long millisToHours(long lngVal)
    {
        return (((lngVal / 1000) / 60) / 60);
    }

}