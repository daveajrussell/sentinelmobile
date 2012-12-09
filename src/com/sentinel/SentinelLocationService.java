package com.sentinel;

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
import org.json.JSONException;
import org.json.JSONStringer;

import java.io.*;
import java.util.Calendar;
import java.util.TimeZone;

public class SentinelLocationService extends Service {

    private static final String FILE_NAME;
    private static final int TIME;
    private static final int DISTANCE;

    static {
        FILE_NAME = "geodata.tmp";
        TIME = 5000;
        DISTANCE = 5;
    }

    private Notification.Builder oLocationServiceNotificationBuilder;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        LocationManager oLocationServiceLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        oLocationServiceLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationServiceLocationListener);

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
        String strBufferedGeoDataJSON = readJSONStringFromBuffer();

        if (deviceIsConnected()) {

            if (strBufferedGeoDataJSON != null) {
                sendGISToLocationService(strBufferedGeoDataJSON + strGeoDataJSON);
            } else {
                sendGISToLocationService(strGeoDataJSON);
            }
        } else {
            writeJSONStringToBuffer(strBufferedGeoDataJSON + strGeoDataJSON);
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

    public String readJSONStringFromBuffer() {
        try {
            File oBufferedJSONFile = new File(FILE_NAME);
            InputStream oInputStream = new BufferedInputStream(new FileInputStream(oBufferedJSONFile));

            BufferedReader oReader = new BufferedReader(new InputStreamReader(oInputStream));

            StringBuilder oBufferedJSONString = new StringBuilder();
            String strLine;

            while ((strLine = oReader.readLine()) != null) {
                oBufferedJSONString.append(strLine);
            }

            oReader.close();
            oBufferedJSONFile.delete();

            return oBufferedJSONString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeJSONStringToBuffer(String strJSON) {
        try {
            FileOutputStream oOutputStream = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            oOutputStream.write(strJSON.getBytes());
            oOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String convertGeoDataObjectToJSONString(GIS oGis) {

        Gson oGson = new Gson();

        JSONStringer geoLocation;
        String strGeoObjectJSON = null;

        try {
            geoLocation = new JSONStringer()
                    .object()
                    .key("m_lngTimeStamp").value(oGis.getDateTimeStamp())
                    .key("m_dLatitude").value(oGis.getLatitude())
                    .key("m_dLongitude").value(oGis.getLongitude())
                    .key("m_intOrientation").value(oGis.getOrientation())
                    .key("m_dSpeed").value(oGis.getSpeed())
                    .endObject();

            strGeoObjectJSON = oGson.toJson(geoLocation.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strGeoObjectJSON;
    }

    private boolean deviceIsConnected() {
        ConnectivityManager oConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo oNetInfo = oConnectivityManager.getActiveNetworkInfo();

        return oNetInfo != null && oNetInfo.isConnectedOrConnecting();
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
