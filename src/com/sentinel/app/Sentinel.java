package com.sentinel.app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
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
import com.sentinel.helper.JsonHelper;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.tracking.SentinelLocationService;

import java.util.Calendar;

public class Sentinel extends MapActivity
{
    public static final String START_SERVICE = "START_SERVICE";

    private static final int TIME;
    private static final int DISTANCE;

    static
    {
        TIME = 60000;
        DISTANCE = 100;
    }

    LocationListener oLocationListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location location)
        {
            updateLocation(location);
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
    private MapController oMapController;
    private Criteria oCriteria;
    private MapView oMapView;
    private SentinelSharedPreferences oSentinelSharedPreferences;
    private JsonHelper jsonHelper;

    private static long lngBreakLength;
    private static long lngNextBreakLength;
    private static long lngNextBreak;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = getIntent();
        if(intent.getBooleanExtra(START_SERVICE, true))
        {
            startLocationService();
        }


        oMapView = (MapView) findViewById(R.id.mapview);
        oMapController = oMapView.getController();
        oMapView.setBuiltInZoomControls(true);
        oMapController.setZoom(15);

        oSentinelSharedPreferences = new SentinelSharedPreferences(this);
        jsonHelper = new JsonHelper(this);

        startLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.sentinel_menu, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);

        switch (item.getItemId())
        {
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

    private void performLogout()
    {
        stopLocationService();

        String userCredentialsJson = jsonHelper.getUserCredentialsJsonFromSharedPreferences();
        new LogoutAsyncTask(this).execute(userCredentialsJson);
    }

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    private void clockOut()
    {
        long lngSessionBegin = oSentinelSharedPreferences.getSessionBeginDateTime();
        long lngNow = System.currentTimeMillis();
        long lngDiff = lngNow - lngSessionBegin;
        calculateBreak(lngDiff);

        if (lngBreakLength > 0)
        {
            stopLocationService();

            SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(this);
            oSentinelSharedPreferences.setBreakTakenDateTime(Calendar.getInstance().getTimeInMillis());

            Intent oClockInIntent = new Intent(Sentinel.this, SentinelOnBreakActivity.class);
            oClockInIntent.putExtra(SentinelOnBreakActivity.BREAK_LENGTH, lngBreakLength);
            oClockInIntent.putExtra(SentinelOnBreakActivity.NEXT_BREAK_LENGTH, lngNextBreakLength);
            oClockInIntent.putExtra(SentinelOnBreakActivity.NEXT_BREAK, lngNextBreak);
            startActivity(oClockInIntent);
        } else
        {
            new AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage("You may not begin your recorded break yet.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            // do nothing
                        }
                    }).show();

        }
    }

    private void calculateBreak(long lngDiff)
    {
        // driver has been driving for 2 hours (+- 5 minutes) break for 15 minutes, break of 30 must be taken 2.5 (-5 minutes) hours later.
        if (lngDiff >= 6900000 && lngDiff <= 7500000)
        {
            lngBreakLength = 900000;
            lngNextBreakLength = 1800000;
            lngNextBreak = 8700000;
        }
        // driver has been driving for 4.5 hours (+- 5 minutes) and must break for 45 minutes. No other break given.
        else
            if (lngDiff >= 15900000 && lngDiff <= 16500000)
            {
                lngBreakLength = 2700000;
                lngNextBreakLength = 0;
                lngNextBreak = 0;
            }
            // driver has been driving for 4.75 hours (including 15 minute break +- 5 minutes) and must break for 30 minutes. Not other break given.
            else
                if (lngDiff >= 16800000 && lngDiff <= 17400000)
                {
                    lngBreakLength = 1800000;
                    lngNextBreakLength = 0;
                    lngNextBreak = 0;
                } else
                {
                    lngBreakLength = 0;
                    lngNextBreakLength = 0;
                    lngNextBreak = 0;
                }
    }

    private void stopLocationService()
    {
        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        stopService(intent);
    }

    private void startLocationUpdates()
    {
        LocationManager oLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        oLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationListener);

        setGeoSpatialCriteria();

        String strProvider = oLocationManager.getBestProvider(oCriteria, true);

        Location oLocation = oLocationManager.getLastKnownLocation(strProvider);

        updateLocation(oLocation);

        oLocationManager.requestLocationUpdates(strProvider, TIME, DISTANCE, oLocationListener);
    }

    private void startLocationService()
    {
        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        startService(intent);
    }

    private void updateLocation(Location oLocation)
    {
        if (oLocation != null)
        {
            Double dblLatitude = oLocation.getLatitude() * 1E6;
            Double dblLongitude = oLocation.getLongitude() * 1E6;
            GeoPoint oPoint = new GeoPoint(dblLatitude.intValue(), dblLongitude.intValue());
            oMapController.animateTo(oPoint);
        }
    }

    private void setGeoSpatialCriteria()
    {
        oCriteria = new Criteria();
        oCriteria.setAccuracy(Criteria.ACCURACY_FINE);
        oCriteria.setPowerRequirement(Criteria.POWER_LOW);
        oCriteria.setAltitudeRequired(false);
        oCriteria.setBearingRequired(false);
        oCriteria.setSpeedRequired(false);
        oCriteria.setCostAllowed(true);
    }
}
