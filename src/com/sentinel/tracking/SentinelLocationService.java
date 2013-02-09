package com.sentinel.tracking;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import com.sentinel.app.R;
import com.sentinel.app.Sentinel;
import com.sentinel.connection.ConnectionManager;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;
import com.sentinel.utils.JsonBuilder;
import com.sentinel.utils.ServiceHelper;
import com.sentinel.utils.TrackingHelper;
import com.sentinel.utils.Utils;


public class SentinelLocationService extends Service {
    private static final int TIME;
    private static final int DISTANCE;
    private static final int LOCATION_NOTIFICATION_ID;
    private static final int SPEEDING_NOTIFICATION_ID;
    private static final double MAX_SPEED;

    public static boolean isJUnit = false;

    static {
        TIME = 20000;
        DISTANCE = 10;
        LOCATION_NOTIFICATION_ID = 1;
        SPEEDING_NOTIFICATION_ID = 2;
        MAX_SPEED = 134.2161774;
    }

    private static Location currentLocation;

    private SentinelBuffferedGeospatialDataDB oSentinelDB;

    private final class SentinelLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location.getSpeed() > MAX_SPEED) {
                handleExcessSpeed(location);
            }

            if (Utils.checkUpdateIsMoreAccurate(currentLocation, location, TIME)) {
                handleLocationChanged(location);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
            notifyProviderEnabled();
        }

        @Override
        public void onProviderDisabled(String s) {
            notifyProviderDisabled();
        }
    }

    private LocationListener sentinelLocationListener;

    private LocationManager sentinelLocationManager;
    private NotificationCompat.Builder locationServiceNotificationBuilder;
    private NotificationCompat.Builder speedingNotificationBuilder;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        removeUpdates();

        sentinelLocationListener = new SentinelLocationListener();
        sentinelLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sentinelLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, sentinelLocationListener);

        locationServiceNotificationBuilder = new NotificationCompat.Builder(this);
        speedingNotificationBuilder = new NotificationCompat.Builder(this);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        oSentinelDB = new SentinelBuffferedGeospatialDataDB(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        if (0 != oSentinelDB.getBufferedGeospatialDataCount()) {
            String historicalGeospatialJson = oSentinelDB.getBufferedGeospatialDataJsonString();
            ServiceHelper.sendHistoricalDataToLocationService(this, historicalGeospatialJson);
        }

        startSentinelLocationNotifications();
        startSentinelLocationForegroundService();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopSentinelLocationForegroundService();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Intent intent = new Intent(this, OrientationBroadcastReceiver.class);
            intent.putExtra(OrientationBroadcastReceiver.ORIENTATION, "Device is not oriented correctly.");
            sendBroadcast(intent);
        }
    }

    private void startSentinelLocationForegroundService() {
        if (!isJUnit) {
            startForeground(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
        }
    }

    private void stopSentinelLocationForegroundService() {
        removeUpdates();
        if (!isJUnit) {
            stopForeground(true);
        }
    }

    private void removeUpdates() {
        if (null != sentinelLocationListener && null != sentinelLocationManager) {
            sentinelLocationManager.removeUpdates(sentinelLocationListener);
            sentinelLocationManager = null;
            sentinelLocationListener = null;
        }
    }

    private void startSentinelLocationNotifications() {
        Intent intent = new Intent(this, Sentinel.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        locationServiceNotificationBuilder
                .setContentText("Sentinel Running")
                .setSubText("Determining Location...")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);
    }

    private void notifyLocationUpdate(Location location) {
        locationServiceNotificationBuilder.setSubText("Update: " + location.getLatitude() + " " + location.getLongitude()).build();
        notificationManager.notify(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }

    public void handleExcessSpeed(final Location location) {
        speedingNotificationBuilder
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle("Sentinel")
                .setContentText("Caution")
                .setSubText("Speed exceeded legal limits.")
                .setOngoing(true)
                .setAutoCancel(true);
        notificationManager.notify(SPEEDING_NOTIFICATION_ID, speedingNotificationBuilder.build());

        Intent intent = new Intent(this, Sentinel.class);
        intent.putExtra("TEST", "Speeding Broadcast");
        sendBroadcast(intent);

        if (ConnectionManager.deviceIsConnected(getApplicationContext())) {
            String speedingNotificationJson = JsonBuilder.geospatialDataJson(this, location);
            ServiceHelper.sendSpeedingNotification(speedingNotificationJson);
            handleLocationChanged(location);
        }
    }

    public void handleLocationChanged(final Location location) {
        currentLocation = location;
        notifyLocationUpdate(location);

        String strGeospatialInformationJson = addLocationToDatabaseAndReturnLocationJson();

        if (ConnectionManager.deviceIsConnected(getApplicationContext())) {
            if (oSentinelDB.getBufferedGeospatialDataCount() >= 2) {
                ServiceHelper.sendBufferedGeospatialDataToLocationService(this, strGeospatialInformationJson);
            } else {
                ServiceHelper.sendGISToLocationService(this, strGeospatialInformationJson);
            }
        }
        oSentinelDB.closeSentinelDatabase();
    }

    private String addLocationToDatabaseAndReturnLocationJson() {
        GeospatialInformation oGeospatialInformation = TrackingHelper.getGeospatialInformation(this);
        oSentinelDB.addGeospatialData(oGeospatialInformation);

        return oSentinelDB.getBufferedGeospatialDataJsonString();
    }

    private void notifyProviderDisabled() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        locationServiceNotificationBuilder
                .setContentIntent(pIntent)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSubText("You must enable GPS Services").build();
        notificationManager.notify(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }

    private void notifyProviderEnabled() {
        notificationManager.cancel(LOCATION_NOTIFICATION_ID);
        startSentinelLocationNotifications();
        notificationManager.notify(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }
}
