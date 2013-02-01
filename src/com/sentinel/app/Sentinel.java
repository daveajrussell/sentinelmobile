package com.sentinel.app;

import android.app.AlertDialog;
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
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.sentinel.R;
import com.sentinel.asset.GeotagDeliveryZXingActvity;
import com.sentinel.authentication.LogoutAsyncTask;
import com.sentinel.authentication.SentinelLogin;
import com.sentinel.helper.JsonHelper;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.tracking.SentinelLocationService;

public class Sentinel extends MapActivity {

    public static final String NEW_SESSION = "NEW_SESSION";
    public static final String RESUME_SESSION = "RESUME_SESSION";
    public static final String CLOCK_IN_MESSAGE = "CLOCK_IN_MESSAGE";
    private static final int TIME;
    private static final int DISTANCE;

    static {
        TIME = 60000;
        DISTANCE = 100;
    }

    LocationListener oLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
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
    private MapController oMapController;
    private Criteria oCriteria;
    private MapView oMapView;
    private SentinelSharedPreferences sentinelSharedPreferences;
    private JsonHelper jsonHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sentinelSharedPreferences = new SentinelSharedPreferences(this);
        oMapView = (MapView) findViewById(R.id.mapview);
        oMapController = oMapView.getController();
        oMapView.setBuiltInZoomControls(true);
        oMapController.setZoom(10);
        jsonHelper = new JsonHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (0 == sentinelSharedPreferences.getSessionID() &&
                sentinelSharedPreferences.getUserIdentification().isEmpty()) {
            startActivity(new Intent(this, SentinelLogin.class));
            stopLocationService();
        } else if (sentinelSharedPreferences.clockedOut()) {
            startActivity(new Intent(this, SentinelOnBreakActivity.class));
        } else {
            launchSentinel();
        }
    }

    public void launchSentinel() {
        Intent intent = getIntent();

        if (null != intent.getStringExtra(CLOCK_IN_MESSAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage(intent.getStringExtra(CLOCK_IN_MESSAGE))
                    .setPositiveButton("Clock In", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startLocationService();
                        }
                    }).show();
        }

        if (intent.getBooleanExtra(NEW_SESSION, false)) {
            startLocationService();
        }

        if (intent.getBooleanExtra(RESUME_SESSION, false)) {
            startLocationService();
        }

        startLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();

        if (sentinelSharedPreferences.clockedIn()) {
            menuInflater.inflate(R.menu.sentinel_menu, menu);
            return true;
        } else if (sentinelSharedPreferences.clockedOut()) {
            return false;
        } else {
            return false;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.scan_qr_action:
                Intent zxingIntent = new Intent(this, GeotagDeliveryZXingActvity.class);
                startActivity(zxingIntent);
                break;
            case R.id.clock_out:
                clockOut();
                break;
            case R.id.logout_action:
                performLogout();
                break;
            default:
                break;
        }
        return true;
    }

    private void performLogout() {
        stopLocationService();

        String userCredentialsJson = jsonHelper.getUserCredentialsJsonFromSharedPreferences();
        new LogoutAsyncTask(this).execute(userCredentialsJson);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private void clockOut() {
        //sentinelSharedPreferences.setBreakTakenDateTime(Calendar.getInstance().getTimeInMillis());
        //sentinelSharedPreferences.setClockedOut();

        /*Intent service = new Intent(this, BreakService.class);
        service.putExtra(BreakService.BREAK_LENGTH, lngBreakLength);
        service.putExtra(BreakService.NEXT_BREAK_LENGTH, lngNextBreakLength);
        service.putExtra(BreakService.NEXT_BREAK, lngNextBreak);
        startService(service); */
        stopLocationService();

        Intent breakIntent = new Intent(this, SentinelOnBreakActivity.class);
        startActivity(breakIntent);
    }

    private void startLocationUpdates() {
        LocationManager oLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        oLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationListener);

        setGeoSpatialCriteria();

        String strProvider = oLocationManager.getBestProvider(oCriteria, true);

        Location oLocation = oLocationManager.getLastKnownLocation(strProvider);

        updateLocation(oLocation);

        oLocationManager.requestLocationUpdates(strProvider, TIME, DISTANCE, oLocationListener);
    }

    private void startLocationService() {
        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        startService(intent);
    }

    private void stopLocationService() {
        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        stopService(intent);
    }

    private void updateLocation(Location oLocation) {
        if (oLocation != null) {
            Double dblLatitude = oLocation.getLatitude() * 1E6;
            Double dblLongitude = oLocation.getLongitude() * 1E6;
            GeoPoint oPoint = new GeoPoint(dblLatitude.intValue(), dblLongitude.intValue());
            oMapController.animateTo(oPoint);
        }
    }

    private void setGeoSpatialCriteria() {
        oCriteria = new Criteria();
        oCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        oCriteria.setPowerRequirement(Criteria.POWER_LOW);
        oCriteria.setAltitudeRequired(false);
        oCriteria.setBearingRequired(false);
        oCriteria.setSpeedRequired(false);
        oCriteria.setCostAllowed(true);
    }
}

