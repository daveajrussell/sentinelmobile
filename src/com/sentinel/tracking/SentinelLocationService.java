package com.sentinel.tracking;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.sentinel.R;
import com.sentinel.connection.ConnectionManager;
import com.sentinel.helper.TrackingHelper;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;


public class SentinelLocationService extends Service {
    private static final int TIME;
    private static final int DISTANCE;
    private static final int NOTIFICATION_ID;

    static {
        TIME = 60000;
        DISTANCE = 100;
        NOTIFICATION_ID = 1;
    }

    private NotificationCompat.Builder locationServiceNotificationBuilder;
    private ConnectionManager oSentinelConnectionManager;
    private SentinelBuffferedGeospatialDataDB oSentinelDB;

    private final class SentinelLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            handleLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            handleOnStatusChanged(s, i);
        }

        @Override
        public void onProviderEnabled(String s) {
            handleOnProviderEnabled(s);
        }

        @Override
        public void onProviderDisabled(String s) {
            handleOnProviderDisabled(s);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        LocationListener sentinelLocationListener = new SentinelLocationListener();
        LocationManager sentinelLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sentinelLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, sentinelLocationListener);

        oSentinelConnectionManager = new ConnectionManager(this);
        oSentinelDB = new SentinelBuffferedGeospatialDataDB(this);

        startSentinelLocationForegroundService();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopSentinelLocationForegroundService();
    }

    private void startSentinelLocationForegroundService() {
        locationServiceNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentText("Location Service Running")
                .setSmallIcon(R.drawable.ic_launcher);

        startForeground(NOTIFICATION_ID, locationServiceNotificationBuilder.build());
    }

    private void stopSentinelLocationForegroundService() {
        stopForeground(true);
    }

    public void handleLocationChanged(Location oCurrentLocationData) {
        GeospatialInformation oGeospatialInformation = TrackingHelper.buildGeospatialInformationObject(this, oCurrentLocationData);
        oSentinelDB.addGeospatialData(oGeospatialInformation);
        String strGeospatialInformationJson;

        NotificationManager oLocationServiceNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        locationServiceNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentText("Location updated: " + oGeospatialInformation.getLatitude() + " " + oGeospatialInformation.getLongitude())
                .setSmallIcon(R.drawable.ic_launcher);

        oLocationServiceNotificationManager.notify(NOTIFICATION_ID, locationServiceNotificationBuilder.build());

        strGeospatialInformationJson = oSentinelDB.getBufferedGeospatialDataJsonString();

        if (oSentinelConnectionManager.deviceIsConnected()) {
            if (oSentinelDB.getBufferedGeospatialDataCount() >= 2) {
                sendBufferedGeospatialDataToLocationService(strGeospatialInformationJson);
            } else {
                sendGISToLocationService(strGeospatialInformationJson);
            }
        }

        oSentinelDB.closeSentinelDatabase();
    }

    public void handleOnStatusChanged(String s, int i) {
        locationServiceNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentText("Status Changed: " + s + " " + i)
                .setSmallIcon(R.drawable.ic_launcher);
    }

    public void handleOnProviderEnabled(String s) {
        locationServiceNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentText("Provider Enabled: " + s)
                .setSmallIcon(R.drawable.ic_launcher);
    }

    public void handleOnProviderDisabled(String s) {
        locationServiceNotificationBuilder = new NotificationCompat.Builder(this)
                .setContentText("Provider Disabled: " + s)
                .setSmallIcon(R.drawable.ic_launcher);
    }

    private void sendGISToLocationService(String strGeospatialJson) {
        new LocationServiceAsyncTask(getApplicationContext()).execute(strGeospatialJson);
    }

    private void sendBufferedGeospatialDataToLocationService(String strGeospatialJsonSet) {
        new BufferedGeospatialDataAsyncTask(getApplicationContext()).execute(strGeospatialJsonSet);
    }
}
