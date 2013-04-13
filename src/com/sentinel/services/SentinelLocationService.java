package com.sentinel.services;

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
import com.sentinel.sql.SentinelDB;
import com.sentinel.utils.ServiceHelper;
import com.sentinel.utils.TrackingHelper;
import com.sentinel.utils.Utils;


public class SentinelLocationService extends Service implements LocationListener {
    private static final int TIME;
    private static final int DISTANCE;
    private static final int LOCATION_NOTIFICATION_ID;
    private static final int SPEEDING_NOTIFICATION_ID;
    private static final double MAX_SPEED;

    public static boolean isJUnit = false;
    private static boolean ignoreOrientation = false;

    static {
        TIME = 10000;
        DISTANCE = 0;
        LOCATION_NOTIFICATION_ID = 1;
        SPEEDING_NOTIFICATION_ID = 2;
        MAX_SPEED = 134.2161774;
    }

    private LocationManager mSentinelLocationManager;
    private static Location mCurrentLocation;
    private SentinelDB mSentinelDB;

    @Override
    public void onLocationChanged(Location location) {
        if (Utils.checkUpdateIsMoreAccurate(location, mCurrentLocation, TIME)) {
            handleLocationChanged(location);
        }

        if (location.getSpeed() > MAX_SPEED) {
            handleExcessSpeed();
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        if (s.equals(LocationManager.GPS_PROVIDER)) {// || s.equals(LocationManager.NETWORK_PROVIDER)) {
            mSentinelLocationManager.requestLocationUpdates(s, TIME, DISTANCE, this);
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        notifyProviderDisabled();
    }

    private NotificationCompat.Builder locationServiceNotificationBuilder;
    private NotificationCompat.Builder speedingNotificationBuilder;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        locationServiceNotificationBuilder = new NotificationCompat.Builder(this);
        speedingNotificationBuilder = new NotificationCompat.Builder(this);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mSentinelDB = new SentinelDB(this);

        mSentinelLocationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        if (0 != mSentinelDB.getRowCount()) {
            String historicalGeospatialJson = mSentinelDB.getBufferedGeospatialDataJsonString();

            if (1 == mSentinelDB.getRowCount()) {
                ServiceHelper.sendHistoricalDataToLocationService(this, historicalGeospatialJson.toString());
            } else if (1 < mSentinelDB.getRowCount()) {
                ServiceHelper.sendHistoricalDataSetToLocationService(this, historicalGeospatialJson.toString());
            }
        }

        startLocationUpdates();
        startSentinelLocationNotifications();
        startForegroundService();

        return super.onStartCommand(intent, flags, startID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();
        stopForegroundService();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (!ignoreOrientation) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                    newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                Intent intent = new Intent(this, OrientationBroadcastReceiver.class);
                intent.putExtra(OrientationBroadcastReceiver.ORIENTATION, newConfig.orientation);
                sendBroadcast(intent);
            }
        }
    }

    private void startLocationUpdates() {
        mSentinelLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, this);
        //mSentinelLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME, DISTANCE, this);
    }

    private void stopLocationUpdates() {
        mSentinelLocationManager.removeUpdates(this);
        mSentinelLocationManager = null;
    }

    private void startForegroundService() {
        if (!isJUnit) {
            startForeground(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
        }
    }

    private void stopForegroundService() {
        if (!isJUnit) {
            stopForeground(true);
        }
    }

    private void startSentinelLocationNotifications() {
        Intent intent = new Intent(this, Sentinel.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        locationServiceNotificationBuilder
                .setContentTitle("Sentinel")
                .setContentText("Tracking")
                .setSubText("Determining Location...")
                .setSmallIcon(R.drawable.ic_stat_example)
                .setContentIntent(pIntent);
    }

    private void notifyLocationUpdate(Location location) {
        locationServiceNotificationBuilder
                .setSubText("Location: " + location.getLatitude() + ", " + location.getLongitude())
                .setTicker("Update: " + location.getLatitude() + ", " + location.getLongitude());
        notificationManager.notify(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }

    public void handleExcessSpeed() {
        speedingNotificationBuilder
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle("Sentinel")
                .setContentText("Warning")
                .setSubText("Speed exceeded legal limits.")
                .setOngoing(true)
                .setAutoCancel(true);
        notificationManager.notify(SPEEDING_NOTIFICATION_ID, speedingNotificationBuilder.build());

        /*
        if (ConnectionManager.deviceIsConnected(getApplicationContext())) {
            String speedingNotificationJson = JsonBuilder.geospatialDataJson(this, location);
            ServiceHelper.sendGISToLocationService(this, speedingNotificationJson);
            handleLocationChanged(location);
        }*/
    }

    public void handleLocationChanged(final Location location) {
        mCurrentLocation = location;
        notifyLocationUpdate(location);

        String strGeospatialInformationJson = addLocationToDatabaseAndReturnLocationJson(location);

        if (ConnectionManager.deviceIsConnected(getApplicationContext())) {
            if (mSentinelDB.getRowCount() >= 2) {
                ServiceHelper.sendBufferedGeospatialDataToLocationService(this, strGeospatialInformationJson);
            } else {
                ServiceHelper.sendGISToLocationService(this, strGeospatialInformationJson);
            }
        }
        mSentinelDB.closeSentinelDatabase();
    }

    public static void ignoreOrientationChanges(final boolean ignore) {
        ignoreOrientation = ignore;
    }

    private String addLocationToDatabaseAndReturnLocationJson(Location location) {
        GeospatialInformation oGeospatialInformation = TrackingHelper.getGeospatialInformation(this, location);

        mSentinelDB.addGeospatialData(oGeospatialInformation);

        return mSentinelDB.getBufferedGeospatialDataJsonString();
    }

    private void notifyProviderDisabled() {
        final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        final PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        locationServiceNotificationBuilder
                .setContentIntent(pIntent)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle("Sentinel")
                .setContentText("Warning")
                .setSubText("You must enable GPS Services").build();
        notificationManager.notify(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }
}
