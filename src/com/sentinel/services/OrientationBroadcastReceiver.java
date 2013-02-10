package com.sentinel.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.sentinel.connection.ConnectionManager;
import com.sentinel.utils.JsonBuilder;
import com.sentinel.utils.ServiceHelper;
import com.sentinel.utils.TrackingHelper;

public class OrientationBroadcastReceiver extends BroadcastReceiver {

    public static final String ORIENTATION;
    public static final int ORIENTATION_NOTIFICATION_ID;

    static {
        ORIENTATION = "ORIENTATION";
        ORIENTATION_NOTIFICATION_ID = 3;
    }

    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder orientationNotificationBuilder = new NotificationCompat.Builder(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        orientationNotificationBuilder
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle("Sentinel")
                .setContentText("Caution")
                .setSubText("Device is not oriented correctly.");
        notificationManager.notify(ORIENTATION_NOTIFICATION_ID, orientationNotificationBuilder.build());

        Location location = TrackingHelper.lastKnownLocation(context);

        if (ConnectionManager.deviceIsConnected(context)) {
            String orientationNotification = JsonBuilder.geospatialDataJson(context, location);
            ServiceHelper.sendOrientationNotification(orientationNotification);
        }

        String message = intent.getStringExtra(ORIENTATION);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
