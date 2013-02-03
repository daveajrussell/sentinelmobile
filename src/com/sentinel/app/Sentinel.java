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
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sentinel.R;
import com.sentinel.asset.GeotagDeliveryZXingActvity;
import com.sentinel.authentication.LogoutAsyncTask;
import com.sentinel.authentication.SentinelLogin;
import com.sentinel.helper.CriteriaBuilder;
import com.sentinel.helper.JsonHelper;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.tracking.SentinelLocationService;

public class Sentinel extends Activity {

    public static final String NEW_SESSION;
    private static final int TIME;
    private static final int DISTANCE;
    static {
        NEW_SESSION = "NEW_SESSION";
        TIME = 60000;
        DISTANCE = 100;
    }
    private static Intent locationServicesIntent;
    private static GoogleMap googleMap;
    private static FragmentManager fragmentManager;
    private SentinelSharedPreferences sentinelSharedPreferences;
    private LocationListener sentinelLocationListener;
    private LocationManager sentinelLocationManager;

    private static Criteria getGeoSpatialCriteria() {
        new CriteriaBuilder()
                .setAccuracy(1)
                .setPowerRequirement(1)
                .setAltitudeRequired(false)
                .setBearingRequired(false)
                .setSpeedRequired(false)
                .setCostAllowed(true);
        return CriteriaBuilder.build();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        setContentView(R.layout.main);

        fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        googleMap = mapFragment.getMap();
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        sentinelSharedPreferences = new SentinelSharedPreferences(this);

        locationServicesIntent = new Intent(this, SentinelLocationService.class);
    }

    protected void onResume() {
        super.onResume();

        if ((0 == sentinelSharedPreferences.getSessionID()) && (sentinelSharedPreferences.getUserIdentification().isEmpty())) {
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
    }

    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    public void launchSentinel() {
        Intent intent = getIntent();

        if (null != intent.getStringExtra("CLOCK_IN_MESSAGE")) {
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage(intent.getStringExtra("CLOCK_IN_MESSAGE"))
                    .setPositiveButton("Clock In", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startLocationService();
                        }
                    }).show();
        }

        if (intent.getBooleanExtra("NEW_SESSION", false)) {
            startLocationService();
        }

        if (intent.getBooleanExtra("RESUME_SESSION", false)) {
            startLocationService();
        }

        startLocationUpdates();

        if (!sentinelLocationManager.isProviderEnabled("gps")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("You must enable GPS Services")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
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

    private void performLogout() {
        stopLocationService();
        stopLocationUpdates();

        String userCredentialsJson = JsonHelper.getUserCredentialsJsonFromSharedPreferences(sentinelSharedPreferences);

        this.sentinelSharedPreferences.clearSharedPreferences();

        Intent loginIntent = new Intent(this, SentinelLogin.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);

        new LogoutAsyncTask().execute(userCredentialsJson);
    }

    private void clockOut() {
        stopLocationService();
        stopLocationUpdates();

        Intent breakIntent = new Intent(this, SentinelOnBreakActivity.class);
        startActivity(breakIntent);
    }

    private void startLocationUpdates() {
        sentinelLocationListener = new SentinelLocationListener();
        sentinelLocationManager = ((LocationManager) getSystemService("location"));

        Criteria criteria = getGeoSpatialCriteria();

        String provider = sentinelLocationManager.getBestProvider(criteria, true);
        Location lastKnownLocation = sentinelLocationManager.getLastKnownLocation(provider);
        sentinelLocationManager.requestLocationUpdates(provider, TIME, DISTANCE, sentinelLocationListener);

        updateLocation(lastKnownLocation);
    }

    private void stopLocationUpdates() {
        if ((null != sentinelLocationManager) && (null != sentinelLocationListener)) {
            sentinelLocationManager.removeUpdates(sentinelLocationListener);
            sentinelLocationManager = null;
            sentinelLocationListener = null;
        }
    }

    private void startLocationService() {
        if (null != locationServicesIntent)
            startService(locationServicesIntent);
    }

    private void stopLocationService() {
        if (null != locationServicesIntent)
            stopService(locationServicesIntent);
    }

    private void updateLocation(Location location) {
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.addMarker(new MarkerOptions()
                .position(latlng)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(210.0F)));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10.0F));
    }

    private final class SentinelLocationListener implements LocationListener {
        private SentinelLocationListener() {
        }

        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        public void onProviderEnabled(String s) {
        }

        public void onProviderDisabled(String s) {
        }
    }
}