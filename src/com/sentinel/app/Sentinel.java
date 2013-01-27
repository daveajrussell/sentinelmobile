package com.sentinel.app;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
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
import com.sentinel.asset.ZXingTestActivity;
import com.sentinel.authentication.ClockOutAsyncTask;
import com.sentinel.helper.JsonHelper;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.tracking.SentinelLocationService;

import java.util.Calendar;

public class Sentinel extends MapActivity
{
    private static final int TIME;
    private static final int DISTANCE;

    static
    {
        TIME = 5000;
        DISTANCE = 5;
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
    private AlarmManager alarmManager;
    private MapView oMapView;
    private SentinelSharedPreferences oSentinelSharedPreferences;
    private PendingIntent nextBreakPendingIntent;
    private JsonHelper jsonHelper;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        oMapView = (MapView) findViewById(R.id.mapview);
        oMapController = oMapView.getController();
        oMapView.setBuiltInZoomControls(true);
        oMapController.setZoom(5);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        oSentinelSharedPreferences = new SentinelSharedPreferences(this);
        jsonHelper = new JsonHelper(this);

        //setAlarms();
        startLocationService();
        startLocationUpdates();
    }

    private void setAlarms()
    {
        int iAlarmType = AlarmManager.ELAPSED_REALTIME;
        String ALARM_ACTION;
        Intent intentToFire;

        long lngNextBreakAlarm = oSentinelSharedPreferences.getNextAlarm();
        ALARM_ACTION = "NEXT_BREAK_ALARM";
        intentToFire = new Intent(ALARM_ACTION);
        nextBreakPendingIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);

        alarmManager.set(iAlarmType, lngNextBreakAlarm, nextBreakPendingIntent);
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
                Intent zxingIntent = new Intent(this, ZXingTestActivity.class);
                startActivity(zxingIntent);
                break;
            case R.id.clock_out:
                clockOut();
                break;
            case R.id.logout_action:
                String userCredentialsJson = jsonHelper.getUserCredentialsJsonFromSharedPreferences();
                new ClockOutAsyncTask(getApplicationContext()).execute(userCredentialsJson);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    private void startLocationService()
    {
        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        startService(intent);
    }

    private void clockOut()
    {
        stopLocationService();
        SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(this);
        oSentinelSharedPreferences.setBreakTakenDateTime(Calendar.getInstance().getTimeInMillis());

        long lngSessionBegin = oSentinelSharedPreferences.getSessionBeginDateTime();
        long lngNow = System.currentTimeMillis();
        long lngDiff = lngSessionBegin - lngNow;
        long lngBreak = calculateBreak(lngDiff);

        if (lngBreak > 0)
        {
            Intent oClockInIntent = new Intent(Sentinel.this, SentinelOnBreakActivity.class);
            oClockInIntent.putExtra(SentinelOnBreakActivity.BREAK_LENGTH, lngBreak);
            startActivity(oClockInIntent);
        }
        else
        {
            AlertDialog.Builder oResultDialog = new AlertDialog.Builder(getApplicationContext());
            oResultDialog.setTitle("Warning");
            oResultDialog.setMessage("You may not begin your recorded break yet.");
            oResultDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    // do nothing
                }
            });
        }
    }

    private long calculateBreak(long lngDiff)
    {
        // driver has been driving for 2 hours (+- 5 minutes)
        if (lngDiff >= 6900000 || lngDiff <= 7500000)
        {
            // break for 15 minutes, set flat that a break of 30 must be taken 2.5 hours later
            return 900000;
        }
        // driver has been driving for 4.5 hours (+- 5 minutes)
        else
            if (lngDiff >= 15900000 || lngDiff <= 16500000)
            {
                // break for 45 minutes
                return 2700000;
            }
            // driver has been driving for 4.75 hours (including 15 minute break +- 5 minutes)
            else
                if (lngDiff >= 16800000 || lngDiff <= 17400000)
                {
                    // break for 30 minutes
                    return 1800000;
                }
                else
                {
                    return 0;
                }
    }

    private void stopLocationService()
    {
        Intent intent = new Intent(Sentinel.this, SentinelLocationService.class);
        stopService(intent);
    }

    private void startLocationUpdates()
    {
        LocationManager oLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        oLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, DISTANCE, oLocationListener);

        setGeoSpatialCriteria();

        String strProvider = oLocationManager.getBestProvider(oCriteria, true);

        Location oLocation = oLocationManager.getLastKnownLocation(strProvider);

        updateLocation(oLocation);

        oLocationManager.requestLocationUpdates(strProvider, TIME, DISTANCE, oLocationListener);
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
