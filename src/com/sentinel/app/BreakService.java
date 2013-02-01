/*package com.sentinel.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.sentinel.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BreakService extends Service {
    private static final int NOTIFICATION_ID = 2;

    public static String BREAK_LENGTH = "BREAK_LENGTH";
    public static String NEXT_BREAK_LENGTH = "NEXT_BREAK_LENGTH";
    public static String NEXT_BREAK = "NEXT_BREAK";

    private static long lngBreakLength;
    private static long lngNextBreakLength;
    private static long lngNextBreak;

    private Calendar calendar;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationCompatBuilder;
    private SimpleDateFormat simpleDateFormat;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        lngBreakLength = intent.getLongExtra(BREAK_LENGTH, 0);
        lngNextBreakLength = intent.getLongExtra(NEXT_BREAK_LENGTH, 0);
        lngNextBreak = intent.getLongExtra(NEXT_BREAK, 0);

        calendar = Calendar.getInstance();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        countdownTimerRunner.run();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopSelf();
        stopForeground(true);
    }

    private Runnable countdownTimerRunner = new Runnable() {
        @Override
        public void run() {
            startCountdownTimer();
        }
    };

    private void startCountdownTimer() {
        notificationCompatBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher);

        new CountDownTimer(lngBreakLength, 1000) {
            public void onTick(long millisUntilFinished) {
                calendar.setTimeInMillis(millisUntilFinished);
                String timer = simpleDateFormat.format(calendar.getTime());
                notificationCompatBuilder.setContentText("Remaining Break: " + timer);
                notificationManager.notify(NOTIFICATION_ID, notificationCompatBuilder.build());
            }

            public void onFinish() {
                setFinishedNotification();
            }
        }.start();
    }

    private void setFinishedNotification() {
        Intent sentinelIntent = new Intent(getApplicationContext(), Sentinel.class);
        sentinelIntent.putExtra(Sentinel.CLOCK_IN_MESSAGE, getMessageForDialog());

        notificationCompatBuilder
                .setContentText("00:00:00")
                .setContentText("Break Finished")
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                //.setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, sentinelIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        Notification notification = notificationCompatBuilder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(NOTIFICATION_ID, notification);
        stopSelf();
    }

    private String getMessageForDialog() {
        if (lngNextBreak != 0 && lngNextBreakLength != 0) {
            return "You took a break of " + millisToMinutes(lngBreakLength) + " minutes. You must take another break in " + millisToHours(lngNextBreak) + " hours, for " + millisToMinutes(lngNextBreakLength) + " minutes.";
        } else {
            return "You took a break of " + millisToMinutes(lngBreakLength) + " minutes. You are not entitled to any further breaks for the remainder of your shift";
        }
    }

    private long millisToMinutes(long lngVal) {
        return ((lngVal / 1000) / 60);
    }

    private long millisToHours(long lngVal) {
        return (((lngVal / 1000) / 60) / 60);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}*/
