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
import com.sentinel.models.GeospatialInformation;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;


public class SentinelLocationService extends Service {
    private static final int TIME;
    private static final int DISTANCE;
    private static final int LOCATION_NOTIFICATION_ID;
    private static final int THIRTY_SECONDS;

    static {
        TIME = 10000;
        DISTANCE = 10;
        LOCATION_NOTIFICATION_ID = 1;
        THIRTY_SECONDS = 1000 * 60 / 2;
    }

    private static Location currentLocation;

    private SentinelBuffferedGeospatialDataDB oSentinelDB;

    private final class SentinelLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (updateIsMoreAccurate(currentLocation, location)) {
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private LocationListener sentinelLocationListener;
    private LocationManager sentinelLocationManager;
    private NotificationCompat.Builder locationServiceNotificationBuilder;
    private NotificationManager notificationManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        removeUpdates();

        sentinelLocationListener = new SentinelLocationListener();
        sentinelLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sentinelLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, sentinelLocationListener);

        locationServiceNotificationBuilder = new NotificationCompat.Builder(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        oSentinelDB = new SentinelBuffferedGeospatialDataDB(this);

        if (0 != oSentinelDB.getBufferedGeospatialDataCount()) {
            String historicalGeospatialJson = oSentinelDB.getBufferedGeospatialDataJsonString();
            ServiceHelper.sendHistoricalDataToLocationService(this, historicalGeospatialJson);
        }

        startSentinelLocationNotifications();
        getLastKnownLocation();
        startSentinelLocationForegroundService();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopSentinelLocationForegroundService();
        super.onDestroy();
    }

    private void getLastKnownLocation() {
        Location lastLocation = sentinelLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        handleLocationChanged(lastLocation);
    }

    private void startSentinelLocationForegroundService() {
        startForeground(LOCATION_NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }

    private void stopSentinelLocationForegroundService() {
        removeUpdates();
        stopForeground(true);
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

    private static boolean updateIsMoreAccurate(Location currentLocation, Location location) {
        if (currentLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentLocation.getTime();
        boolean isNewer = timeDelta > 0;

        if (timeDelta > THIRTY_SECONDS) {
            return true;
        } else if (timeDelta < -THIRTY_SECONDS) {
            return false;
        }


        int accuracyDelta = (int) (location.getAccuracy() - currentLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
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
