package com.sentinel.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sentinel.asset.GeotagDeliveryZXingActvity;
import com.sentinel.authentication.LogoutAsyncTask;
import com.sentinel.authentication.SentinelLogin;
import com.sentinel.helper.CriteriaBuilder;
import com.sentinel.helper.JsonHelper;
import com.sentinel.helper.Utils;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.tracking.SentinelLocationService;

import java.util.Calendar;

public class Sentinel extends Activity {

    public static final String NEW_SESSION;
    public static final String RESUME_SESSION;
    public static final String RESUME_MESSAGE;
    private static final int TIME;
    private static final int DISTANCE;

    static {
        NEW_SESSION = "NEW_SESSION";
        RESUME_SESSION = "RESUME_SESSION";
        RESUME_MESSAGE = "RESUME_MESSAGE";
        TIME = 60000;
        DISTANCE = 100;
    }

    private static Location lastLocation;

    private static Intent locationServicesIntent;
    private GoogleMap googleMap;
    private SentinelSharedPreferences sentinelSharedPreferences;
    private LocationListener sentinelLocationListener;
    private LocationManager sentinelLocationManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (result == ConnectionResult.SUCCESS) {
            setContentView(R.layout.main);

            FragmentManager fragmentManager = getFragmentManager();
            MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
            googleMap = mapFragment.getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);

            sentinelSharedPreferences = new SentinelSharedPreferences(this);
            locationServicesIntent = new Intent(this, SentinelLocationService.class);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("This device was found to be incompatible with Google Play Services." +
                            "Please check your version of Google Play.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    protected void onResume() {
        super.onResume();

        if ((0 == sentinelSharedPreferences.getSessionID()) && (sentinelSharedPreferences.getUserIdentification().isEmpty())) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SentinelLogin.class));
            stopLocationService();
        } else if (sentinelSharedPreferences.clockedOut()) {
            startActivity(new Intent(this, SentinelOnBreakActivity.class));
        } else {
            launchSentinel();
        }
    }

    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

        int orientaton = getApplicationContext().getResources().getConfiguration().orientation;
        Toast.makeText(getApplicationContext(), String.format("%d", orientaton), Toast.LENGTH_SHORT).show();
    }

    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    public void launchSentinel() {
        Intent intent = getIntent();

        if (intent.getBooleanExtra(NEW_SESSION, false)) {
            startLocationService();
            intent.removeExtra(NEW_SESSION);
        }

        if (intent.getBooleanExtra(RESUME_SESSION, false)) {
            startLocationService();
            intent.removeExtra(RESUME_SESSION);
        }

        if (intent.hasExtra(RESUME_MESSAGE)) {
            Toast.makeText(this, intent.getStringExtra(RESUME_MESSAGE), Toast.LENGTH_LONG).show();
            intent.removeExtra(RESUME_MESSAGE);
        }

        startLocationUpdates();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();

        if (sentinelSharedPreferences.clockedIn()) {
            menuInflater.inflate(R.menu.sentinel_menu, menu);
            return true;
        } else if (sentinelSharedPreferences.clockedOut()) {
            return false;
        }
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.scan_qr_action:
                Intent zxingIntent = new Intent(this, GeotagDeliveryZXingActvity.class);
                startActivity(zxingIntent);
                break;
            case R.id.clock_out_action:
                clockOut();
                break;
            case R.id.logout_action:
                performLogout();
                break;
        }

        return true;
    }

    protected void performLogout() {

        long lngShiftEndTimeDifference = sentinelSharedPreferences.getDrivingEndAlarm() - System.currentTimeMillis();
        String time = Utils.getFormattedHrsMinsSecsTimeString(lngShiftEndTimeDifference);
        String message = String.format("You still have %1$s of your shift remaining. Are you sure you wish to logout?", time);

        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(message)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopLocationService();
                        stopLocationUpdates();

                        String userCredentialsJson = JsonHelper.getUserCredentialsJsonFromSharedPreferences(sentinelSharedPreferences);

                        sentinelSharedPreferences.clearSharedPreferences();

                        Intent loginIntent = new Intent(getApplicationContext(), SentinelLogin.class);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);

                        new LogoutAsyncTask().execute(userCredentialsJson);
                    }
                }).show();
    }

    protected void clockOut() {
        stopLocationService();
        stopLocationUpdates();

        Intent breakIntent = new Intent(this, SentinelOnBreakActivity.class);
        startActivity(breakIntent);
    }

    protected void startLocationUpdates() {
        sentinelLocationListener = new sentinelLocationListener();
        sentinelLocationManager = ((LocationManager) getSystemService(LOCATION_SERVICE));

        Criteria criteria = getGeoSpatialCriteria();

        String provider = sentinelLocationManager.getBestProvider(criteria, true);
        sentinelLocationManager.requestLocationUpdates(provider, TIME, DISTANCE, sentinelLocationListener);

        updateLocation(getLastLocation());
    }

    private static Criteria getGeoSpatialCriteria() {
        new CriteriaBuilder()
                .setAccuracy(Criteria.ACCURACY_FINE)
                .setPowerRequirement(Criteria.POWER_HIGH)
                .setAltitudeRequired(false)
                .setBearingRequired(false)
                .setSpeedRequired(false)
                .setCostAllowed(true);
        return CriteriaBuilder.build();
    }

    protected void stopLocationUpdates() {
        if ((null != sentinelLocationManager) && (null != sentinelLocationListener)) {
            sentinelLocationManager.removeUpdates(sentinelLocationListener);
            sentinelLocationManager = null;
            sentinelLocationListener = null;
        }
    }

    protected void startLocationService() {
        if (null != locationServicesIntent)
            startService(locationServicesIntent);
    }

    protected void stopLocationService() {
        if (null != locationServicesIntent)
            stopService(locationServicesIntent);
    }

    protected void updateLocation(Location location) {
        if (null != location) {
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.clear();
            googleMap.addMarker(new MarkerOptions()
                    .position(latlng)
                    .title("Current Location")
                    .snippet("Time: " + Calendar.getInstance().getTime())
                    .icon(BitmapDescriptorFactory.defaultMarker(210)));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 13));
            setLastLocation(location);
        }
    }

    protected Location getLastLocation() {
        if (null != lastLocation)
            return lastLocation;
        else
            return null;
    }

    protected void setLastLocation(Location location) {
        if (null != location)
            lastLocation = location;
    }

    private final class sentinelLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            if (Utils.checkUpdateIsMoreAccurate(lastLocation, location, TIME)) {
                updateLocation(location);
            }
        }

        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        public void onProviderEnabled(String s) {
        }

        public void onProviderDisabled(String s) {
        }
    }
}