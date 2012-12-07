package com.sentinel;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONStringer;

import java.io.*;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * David Russell
 * 05/12/12
 * Service to send regular location updates to the web services
 * */
public class SentinelLocationService extends Service {

    private static final String TAG;
    private static final String FILE_NAME;
    private static final int TIME;
    private static final int DISTANCE;

    static {
        TAG = "LOCATION_UPDATE_SERVICE";
        FILE_NAME = "geodata.tmp";
        TIME = 5000;
        DISTANCE = 5;
    }

    private LocationManager oLocationManager;
    private GIS oGIS;
    private Gson oGson;
    private NotificationManager oNotificationManager;
    private Notification.Builder oNotifyBuilder;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        oLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        oLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationListener);

        startSentinelLocationForegroundService();

        return Service.START_STICKY;
    }

    /**
     * David Russell
     * 05/12/12
     * Function to bring the location service to the foreground.
     */
    private void startSentinelLocationForegroundService() {
        int NOTIFICATION_ID = 1;

        Intent intent = new Intent(this, Sentinel.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);

        oNotifyBuilder = new Notification.Builder(this)
                .setContentText("Location Service Running")
                .setSmallIcon(R.drawable.ic_launcher);

        startForeground(NOTIFICATION_ID, oNotifyBuilder.build());
    }

    /**
     * David Russell
     * 05/12/2012
     * Private function called by location listener to handle the location changed event
     *
     * @param oLocation
     * Location object parameter. Contains all GPS information
     */
    public void handleLocationChanged(Location oLocation) {

        // create an object with the location data
        oGIS = new GIS();
        oGIS.setDateTimeStamp(Calendar.getInstance(TimeZone.getTimeZone("gmt+1")).getTimeInMillis());
        oGIS.setLongitude(oLocation.getLongitude());
        oGIS.setLatitude(oLocation.getLatitude());
        oGIS.setOrientation(getResources().getConfiguration().orientation);
        oGIS.setSpeed(oLocation.getSpeed());

        oNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        oNotifyBuilder = new Notification.Builder(this)
                .setContentText("Location updated: " + oGIS.getLatitude() + " " + oGIS.getLongitude())
                .setSmallIcon(R.drawable.ic_launcher);

        oNotificationManager.notify(1, oNotifyBuilder.build());

        String strJSON = GISToJSONString(oGIS);
        String strBufferedJSON = readJSONStringFromBuffer();

        // call function to check for connectivity
        if (isConnected()) {
            // check for unsent data
            if (strBufferedJSON != null) {
                // call function to consume webservice with buffered data
                sendGISToLocationService(strBufferedJSON + strJSON);
            } else {
                // call function to consume webservice
                sendGISToLocationService(strJSON);
            }
        } else {
            // if there is no connectivity, write to a buffer
            writeJSONStringToBuffer(strBufferedJSON + strJSON);
        }
    }

    /**
     * David Russell
     * 05/12/12
     * Consumes a web service, sending data to a location service
     *
     * @param strJSON - JSON representation of a GIS object, encapsulating geographical data
     */
    private void sendGISToLocationService(String strJSON) {
        new LocationServiceAsyncTask().execute(strJSON);
    }

    /**
     * David Russell
     * 05/12/12
     * Private function that reads a JSON string from a private application file
     *
     * @return
     * If the file is found, the buffered JSON string
     */
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

    /**
     * David Russell
     * 05/12/12
     * Writes a JSON string to a private file for use later
     *
     * @param strJSON
     * The JSON to write to the buffer
     */
    public void writeJSONStringToBuffer(String strJSON) {
        try {
            FileOutputStream oOutputStream = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            oOutputStream.write(strJSON.getBytes());
            oOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * David Russell
     * 05/12/2012
     * Private function to check for connectivity.
     *
     * @return
     * Boolean value indicating if the device has connection
     */
    private boolean isConnected() {
        ConnectivityManager oConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo oNetInfo = oConnectivityManager.getActiveNetworkInfo();

        if (oNetInfo != null && oNetInfo.isConnectedOrConnecting())
            return true;
        else
            return false;
    }

    /**
     * David Russell
     * 05/12/12
     * private function that stringifies a GIS object
     *
     * @param oGis
     * Geographical data encapsulated in an object
     * @return
     * The JSON string for the GIS object
     */
    public String GISToJSONString(GIS oGis) {

        // Create a Gson object
        oGson = new Gson();

        // Create a json string
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

            // wrap the json string
            strGeoObjectJSON = oGson.toJson(geoLocation.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strGeoObjectJSON;
    }

    LocationListener oLocationListener = new LocationListener() {
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
