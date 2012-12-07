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

public class Sentinel extends MapActivity {

    private MapView oMapView;
    private MapController oMapController;
    private LocationManager oLocationManager;
    private Criteria oCriteria;
    private String strProvider;

    private static final int TIME;
    private static final int DISTANCE;

    static {
        TIME = 5000;
        DISTANCE = 5;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set MapView object reference
        oMapView = (MapView)findViewById(R.id.mapview);

        // Get a reference to the MapView's MapController object
        oMapController = oMapView.getController();

        // Set MapView settings
        oMapView.setBuiltInZoomControls(true);

        // Set MapController settings
        oMapController.setZoom(17);

        // Start the SentinelLocationService Service
        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        startService(intent);

        // Start listening to location updates
        startLocationUpdates();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * David Russell
     * 06/12/12
     * Function to start the location updates
     * Initially, an attempt to determine the users last location is made
     * Subsequent updates to the map are made via the LocationListener
     */
    private void startLocationUpdates() {
        oLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        oLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationListener);

        // Set Criteria
        setCriteria();

        // Set Provider
        strProvider = oLocationManager.getBestProvider(oCriteria, true);

        // Get the last known location using the provider
        Location oLocation = oLocationManager.getLastKnownLocation(strProvider);

        // Update the location on the map with the last known location
        updateLocation(oLocation);

        // Setup requesting location updates every 5000 milliseconds or 5 metres
        oLocationManager.requestLocationUpdates(strProvider, TIME, DISTANCE, oLocationListener);
    }

    /**
     * David Russell
     * 06/12/12
     * Function to update the location on the map.
     * Takes the latitude and longitude from the Location object passed
     * Creates a GeoPoint object that is used as a reference point to animate the map to
     * @param oLocation
     * The Location object that is created by the event listener
     */
    private void updateLocation(Location oLocation) {
        if(oLocation != null) {
            Double dblLatitude = oLocation.getLatitude()*1E6;
            Double dblLongitude = oLocation.getLongitude()*1E6;
            GeoPoint oPoint = new GeoPoint(dblLatitude.intValue(), dblLongitude.intValue());
            oMapController.animateTo(oPoint);
        }
    }

    /**
     * David Russell
     * 06/12/12
     * Function that sets the Criteria object with settings
     * Here it is specified that the accuracy needs to be FINE
     * The power requirement is LOW
     * There is no need to take Altitude, Bearing or Speed measurements
     */
    private void setCriteria() {
        oCriteria = new Criteria();
        oCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        oCriteria.setPowerRequirement(Criteria.POWER_LOW);
        oCriteria.setAltitudeRequired(false);
        oCriteria.setBearingRequired(false);
        oCriteria.setSpeedRequired(false);
        oCriteria.setCostAllowed(true);
    }

    /**
     * David Russell
     * 06/12/12
     * Setup a LocationListener to handle location changed events
     */
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
