package com.sentinel;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.sentinel.R;

public class Sentinel extends MapActivity {

    private MapView oMapView;
    private MapController oMapController;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        oMapView = (MapView)findViewById(R.id.mapView);

        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        startService(intent);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
