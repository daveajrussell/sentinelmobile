package com.sentinel.tracking;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import com.sentinel.helper.ServiceHelper;
import com.sentinel.helper.TrackingHelper;
import com.sentinel.helper.Utils;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;


public class SentinelLocationService extends Service {
    private static final int TIME;
    private static final int DISTANCE;
    private static final int LOCATION_NOTIFICATION_ID;

    public static boolean isJUnit = false;

    static {
        TIME = 20000;
        DISTANCE = 10;
        LOCATION_NOTIFICATION_ID = 1;
    }

    private static Location currentLocation;

    private SentinelBuffferedGeospatialDataDB oSentinelDB;

    private final class SentinelLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
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
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        removeUpdates();

        sentinelLocationListener = new SentinelLocationListener();
        sentinelLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sentinelLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, sentinelLocationListener);

        locationServiceNotificationBuilder = new NotificationCompat.Builder(this);
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
        locationServiceNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentText("Sentinel Running")
                .setSubText("Determining Location...")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent);
    }

    private void notifyLocationUpdate(Location location) {
        locationServiceNotificationBuilder.setSubText("Update: " + location.getLatitude() + " " + location.getLongitude()).build();
        notificationManager.notify(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }

    public void handleLocationChanged(final Location location) {
        currentLocation = location;
        notifyLocationUpdate(location);

        GeospatialInformation oGeospatialInformation = TrackingHelper.buildGeospatialInformationObject(this, location);
        oSentinelDB.addGeospatialData(oGeospatialInformation);
        String strGeospatialInformationJson;

        strGeospatialInformationJson = oSentinelDB.getBufferedGeospatialDataJsonString();

        if (ConnectionManager.deviceIsConnected(getApplicationContext())) {
            if (oSentinelDB.getBufferedGeospatialDataCount() >= 2) {
                ServiceHelper.sendBufferedGeospatialDataToLocationService(this, strGeospatialInformationJson);
            } else {
                ServiceHelper.sendGISToLocationService(this, strGeospatialInformationJson);
            }
        }

        oSentinelDB.closeSentinelDatabase();
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
