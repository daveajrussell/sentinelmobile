package com.sentinel.tracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import com.google.gson.Gson;
import com.sentinel.R;
import com.sentinel.connection.ConnectionManager;
import com.sentinel.models.GIS;
import org.json.JSONException;
import org.json.JSONStringer;

import java.io.*;
import java.util.Calendar;
import java.util.TimeZone;

public class SentinelLocationService extends Service {

    private static final int TIME;
    private static final int DISTANCE;

    static {
        TIME = 5000;
        DISTANCE = 5;
    }

    private Notification.Builder oLocationServiceNotificationBuilder;
    private ConnectionManager oSentinelConnectionManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        LocationManager oLocationServiceLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        oLocationServiceLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationServiceLocationListener);
        oSentinelConnectionManager = new ConnectionManager(this);

        startSentinelLocationForegroundService();

        return Service.START_STICKY;
    }

    private void startSentinelLocationForegroundService() {
        int NOTIFICATION_ID = 1;

        oLocationServiceNotificationBuilder = new Notification.Builder(this)
                .setContentText("Location Service Running")
                .setSmallIcon(R.drawable.ic_launcher);

        startForeground(NOTIFICATION_ID, oLocationServiceNotificationBuilder.build());
    }

    public void handleLocationChanged(Location oCurrentLocationData) {

        GIS oGeoData = buildGeoDataObject(oCurrentLocationData);

        NotificationManager oLocationServiceNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        oLocationServiceNotificationBuilder = new Notification.Builder(this)
                .setContentText("Location updated: " + oGeoData.getLatitude() + " " + oGeoData.getLongitude())
                .setSmallIcon(R.drawable.ic_launcher);

        oLocationServiceNotificationManager.notify(1, oLocationServiceNotificationBuilder.build());

        String strGeoDataJSON = convertGeoDataObjectToJSONString(oGeoData);
        String strBufferedGeoDataJSON = GISDataBuffer.readJSONStringFromBuffer();

        if (oSentinelConnectionManager.deviceIsConnected()) {

            if (strBufferedGeoDataJSON != null) {
                sendGISToLocationService(strBufferedGeoDataJSON + strGeoDataJSON);
            } else {
                sendGISToLocationService(strGeoDataJSON);
            }
        } else {
            GISDataBuffer.writeJSONStringToBuffer(this, strBufferedGeoDataJSON + strGeoDataJSON);
        }
    }

    private GIS buildGeoDataObject(Location oCurrentLocationData) {
        return new GIS(
            Calendar.getInstance(TimeZone.getTimeZone("gmt+1")).getTimeInMillis(),
            oCurrentLocationData.getLongitude(),
            oCurrentLocationData.getLatitude(),
            getResources().getConfiguration().orientation,
            oCurrentLocationData.getSpeed()
        );
    }

    private void sendGISToLocationService(String strJSON) {
        new LocationServiceAsyncTask().execute(strJSON);
    }

    public String convertGeoDataObjectToJSONString(GIS oGis) {

        Gson oGson = new Gson();

        JSONStringer geoLocation;
        String strGeoObjectJSON = null;

        try {
            geoLocation = new JSONStringer()
                    .object()
                    .key("lngTimeStamp").value(oGis.getDateTimeStamp())
                    .key("dblLatitude").value(oGis.getLatitude())
                    .key("dblLongitude").value(oGis.getLongitude())
                    .key("intOrientation").value(oGis.getOrientation())
                    .key("dblSpeed").value(oGis.getSpeed())
                    .endObject();

            strGeoObjectJSON = oGson.toJson(geoLocation.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strGeoObjectJSON;
    }

    LocationListener oLocationServiceLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            handleLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

}
