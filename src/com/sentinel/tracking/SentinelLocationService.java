package com.sentinel.tracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import com.sentinel.R;
import com.sentinel.connection.ConnectionManager;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.sql.SentinelBuffferedGeospatialDataDB;


public class SentinelLocationService extends Service
{

    private static final int TIME;
    private static final int DISTANCE;
    private static final int NOTIFICATION_ID;

    static
    {
        TIME = 5000;
        DISTANCE = 5;
        NOTIFICATION_ID = 1;
    }

    LocationListener oLocationServiceLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            handleLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle)
        {
        }

        @Override
        public void onProviderEnabled(String s)
        {
        }

        @Override
        public void onProviderDisabled(String s)
        {
        }
    };
    private Notification.Builder oLocationServiceNotificationBuilder;
    private ConnectionManager oSentinelConnectionManager;
    private SentinelBuffferedGeospatialDataDB oSentinelDB;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
        LocationManager oLocationServiceLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        oLocationServiceLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationServiceLocationListener);
        oSentinelConnectionManager = new ConnectionManager(this);
        oSentinelDB = new SentinelBuffferedGeospatialDataDB(this);

        startSentinelLocationForegroundService();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        stopSentinelLocationForegroundService();
    }

    private void startSentinelLocationForegroundService()
    {
        oLocationServiceNotificationBuilder = new Notification.Builder(this)
                .setContentText("Location Service Running")
                .setSmallIcon(R.drawable.ic_launcher);

        startForeground(NOTIFICATION_ID, oLocationServiceNotificationBuilder.build());
    }

    private void stopSentinelLocationForegroundService()
    {
        stopForeground(true);
    }

    public void handleLocationChanged(Location oCurrentLocationData)
    {
        GeospatialInformation oGeospatialInformation = TrackingHelper.buildGeospatialInformationObject(this, oCurrentLocationData);
        oSentinelDB.addGeospatialData(oGeospatialInformation);
        String strGeospatialInformationJson;

        NotificationManager oLocationServiceNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        oLocationServiceNotificationBuilder = new Notification.Builder(this)
                .setContentText("Location updated: " + oGeospatialInformation.getLatitude() + " " + oGeospatialInformation.getLongitude())
                .setSmallIcon(R.drawable.ic_launcher);

        oLocationServiceNotificationManager.notify(1, oLocationServiceNotificationBuilder.build());

        strGeospatialInformationJson = oSentinelDB.getBufferedGeospatialDataJsonString();

        if (oSentinelConnectionManager.deviceIsConnected())
        {
            if (oSentinelDB.getBufferedGeospatialDataCount() >= 2)
            {
                sendBufferedGeospatialDataToLocationService(strGeospatialInformationJson);
            }
            else
            {
                sendGISToLocationService(strGeospatialInformationJson);
            }
        }

        oSentinelDB.closeSentinelDatabase();
    }

    private void sendGISToLocationService(String strGeospatialJson)
    {
        new LocationServiceAsyncTask(getApplicationContext()).execute(strGeospatialJson);
    }

    private void sendBufferedGeospatialDataToLocationService(String strGeospatialJsonSet)
    {
        new BufferedGeospatialDataAsyncTask(getApplicationContext()).execute(strGeospatialJsonSet);
    }
}
