package com.sentinel.app;

import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import com.jayway.android.robotium.solo.Solo;
import com.sentinel.authentication.SentinelLogin;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.utils.Time;

public class SentinelTest extends ActivityInstrumentationTestCase2<SentinelLogin> {

    private View btnLoginView;
    private View btnClockInView;
    private View btnClockOutView;
    private Sentinel sentinelActivity;
    private Solo solo;

    public SentinelTest() {
        super(SentinelLogin.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        btnLoginView = solo.getView(R.id.btn_login);
        btnClockInView = solo.getView(R.id.btnClockIn);
        SentinelLogin.isJunit = true;
        SentinelShiftEndingActivity.isJunit = true;
        sentinelActivity = performLogin();

    }

    @Override
    public void tearDown() throws Exception {
        if (solo.getCurrentActivity().getClass() == Sentinel.class) {
            performLogout();
        }
        solo.finishOpenedActivities();
    }

    private Sentinel performLogin() {
        solo.clickOnView(btnLoginView);
        solo.assertCurrentActivity("Sentinel Activitiy should have launched", Sentinel.class);
        return (Sentinel) solo.getCurrentActivity();
    }

    private void performLogout() {
        solo.clickOnMenuItem("Logout");
        solo.clickOnText("Yes");
        solo.assertCurrentActivity("Login acitivity should have launched", SentinelLogin.class);
    }

    public void testCanLaunchApplication() throws Exception {
        assertNotNull("Login activity should not be null", getActivity());
    }

    public void testBackButtonResumesActivityWhenLoggedIn() throws Exception {
        solo.goBack();
        assertNotNull("Activity should not be null", solo.getCurrentActivity());
        assertSame(Sentinel.class, solo.getCurrentActivity().getClass());
    }

    public void testSetDummyLocation() throws Exception {
        final Location location = new Location("gps");
        location.setLatitude(52.800000);
        location.setLongitude(-2.000000);

        final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                assertNotNull("Last location should not be null", sentinelActivity.getLastLocation());
                assertEquals("Latitudes should be equal", sentinelActivity.getLastLocation().getLatitude(), location.getLatitude());
                assertEquals("Longitudes should be equal", sentinelActivity.getLastLocation().getLongitude(), location.getLongitude());
            }
        });

        solo.assertCurrentActivity("Yeah", Sentinel.class);
    }

    public void testReorientingPreservesCurrentLocation() throws Exception {
        final Location location = new Location("gps");
        location.setLatitude(52.800000);
        location.setLongitude(-2.000000);

        final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                sentinelActivity.updateLocation(location);
                solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                assertNotNull("Last location should not be null", sentinelActivity.getLastLocation());
                assertEquals("Latitudes should be equal", sentinelActivity.getLastLocation().getLatitude(), location.getLatitude());
                assertEquals("Longitudes should be equal", sentinelActivity.getLastLocation().getLongitude(), location.getLongitude());
                //solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        });

        solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void testClickLogoutShowsAlertDialog() throws Exception {
        solo.clickOnMenuItem("Logout");
        assertTrue(solo.searchText("Are you sure you wish to logout?"));
        /* Robotium doesn't seem to be clicking on the "No" button, using goBack() instead */
        solo.goBack();
        solo.assertCurrentActivity("Current activity should be Sentinel activity", Sentinel.class);
    }

    public void testClockOutShowsNotification() throws Exception {
        solo.clickOnActionBarItem(R.id.clock_out_action);
        assertTrue("Notification should be shown", solo.waitForText("You may not begin your recorded break yet.", 1, 2000));
        solo.assertCurrentActivity("Current activity should return to the Sentinel activity", Sentinel.class);
    }

    public void testClockOutAfterFourHoursThirtyAllowsLoggingOut() throws Exception {
        SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(getActivity().getApplicationContext());

        long timeSessionBegan = sentinelSharedPreferences.getSessionBeginDateTime();
        long mockSessionBegan = timeSessionBegan - Time.FOUR_HOURS_TWENTY;
        sentinelSharedPreferences.setSessionBeginDateTime(mockSessionBegan);

        SentinelOnBreakActivity.isJunit = true;

        solo.clickOnActionBarItem(R.id.clock_out_action);
        solo.assertCurrentActivity("Current activity should be the break activity", SentinelOnBreakActivity.class);
        btnClockInView = solo.getView(R.id.btnClockIn);
        assertTrue("Clock in button should not be visible", View.INVISIBLE == btnClockInView.getVisibility());
        assertTrue("After 1 second, the break should end", solo.waitForText("00:00"));
        assertTrue("The clock in button should become visible", View.VISIBLE == btnClockInView.getVisibility());
        solo.clickOnView(btnClockInView);
        solo.assertCurrentActivity("Current activity should be Sentinel activity", Sentinel.class);
    }

    public void testShiftEnding() throws Exception {
        //assertTrue("Within 30 seconds the Activity should switch to Shift Ending", solo.waitForActivity(SentinelShiftEndingActivity.class.getName(), 30000));
        solo.waitForText("Remaining");
        btnClockOutView = solo.getView(R.id.btnClockOut);
        assertTrue("The clock out button should be invisible", View.INVISIBLE == btnClockOutView.getVisibility());
        assertTrue("After 1 second, the shift should end", solo.waitForText("00:00"));
        assertTrue("The clock out button should become visible", View.VISIBLE == btnClockOutView.getVisibility());
        solo.clickOnView(btnClockOutView);
        solo.assertCurrentActivity("Current activity should be Sentinel activity", SentinelLogin.class);
    }

    public void testPhoneReorientation() throws Exception {
        solo.setActivityOrientation(Solo.LANDSCAPE);
        assertTrue("Alert should be displayed", solo.waitForText("Device is not oriented correctly."));
        solo.setActivityOrientation(Solo.PORTRAIT);
    }

    /* Can't unit test as this starts an activity to a different application and robotium doesn't
    * work across multiple instances */
    /*public void testScanDeliveryItemLaunchesZXIngActiviy() throws Exception {
        solo.clickOnText("Scan Delivery Item");
        solo.assertCurrentActivity("Current activity should be ZXing activity", GeotagDeliveryZXingActvity.class);
        solo.finishOpenedActivities();
    }*/
}
