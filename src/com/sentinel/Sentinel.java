package com.sentinel;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.sentinel.preferences.SentinelSharedPreferences;

public class Sentinel extends MapActivity {

    private MapController oMapController;
    private Criteria oCriteria;

    private static final int TIME;
    private static final int DISTANCE;

    static {
        TIME = 5000;
        DISTANCE = 5;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(this);
        String strTest = oSentinelSharedPreferences.getUserPreferences();

        MapView oMapView = (MapView) findViewById(R.id.mapview);

        oMapController = oMapView.getController();

        oMapView.setBuiltInZoomControls(true);

        oMapController.setZoom(17);

        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        startService(intent);

        startLocationUpdates();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void startLocationUpdates() {
        LocationManager oLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        oLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationListener);

        // Set Criteria
        setCriteria();

        // Set Provider
        String strProvider = oLocationManager.getBestProvider(oCriteria, true);

        // Get the last known location using the provider
        Location oLocation = oLocationManager.getLastKnownLocation(strProvider);

        // Update the location on the map with the last known location
        updateLocation(oLocation);

        // Setup requesting location updates every 5000 milliseconds or 5 metres
        oLocationManager.requestLocationUpdates(strProvider, TIME, DISTANCE, oLocationListener);
    }

    private void updateLocation(Location oLocation) {
        if(oLocation != null) {
            Double dblLatitude = oLocation.getLatitude()*1E6;
            Double dblLongitude = oLocation.getLongitude()*1E6;
            GeoPoint oPoint = new GeoPoint(dblLatitude.intValue(), dblLongitude.intValue());
            oMapController.animateTo(oPoint);
        }
    }

    private void setCriteria() {
        oCriteria = new Criteria();
        oCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        oCriteria.setPowerRequirement(Criteria.POWER_LOW);
        oCriteria.setAltitudeRequired(false);
        oCriteria.setBearingRequired(false);
        oCriteria.setSpeedRequired(false);
        oCriteria.setCostAllowed(true);
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
}
