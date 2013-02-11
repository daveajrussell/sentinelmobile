package com.sentinel.app;

import android.content.pm.ActivityInfo;
import android.location.Location;
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
    private Sentinel mSentinelActivity;
    private Solo mSolo;

    public SentinelTest() {
        super(SentinelLogin.class);
    }

    @Override
    public void setUp() throws Exception {
        mSolo = new Solo(getInstrumentation(), getActivity());
        btnLoginView = mSolo.getView(R.id.btn_login);
        btnClockInView = mSolo.getView(R.id.btnClockIn);
        SentinelLogin.isJunit = true;
        SentinelShiftEndingActivity.isJunit = true;
        mSolo.setActivityOrientation(Solo.PORTRAIT);
        mSentinelActivity = performLogin();

    }

    @Override
    public void tearDown() throws Exception {
        if (mSolo.getCurrentActivity().getClass() == Sentinel.class) {
            performLogout();
        }
        mSolo.setActivityOrientation(Solo.PORTRAIT);
        mSolo.finishOpenedActivities();
    }

    private Sentinel performLogin() {
        mSolo.clickOnView(btnLoginView);
        mSolo.assertCurrentActivity("Sentinel Activitiy should have launched", Sentinel.class);
        return (Sentinel) mSolo.getCurrentActivity();
    }

    private void performLogout() {
        mSolo.waitForText("Logout");
        mSolo.clickOnMenuItem("Logout");
        mSolo.clickOnText("Yes");
        mSolo.assertCurrentActivity("Login acitivity should have launched", SentinelLogin.class);
    }

    public void testCanLaunchApplication() throws Exception {
        assertNotNull("Login activity should not be null", getActivity());
    }

    /*public void testBackButtonResumesActivityWhenLoggedIn() throws Exception {
        mSolo.goBack();
        assertNotNull("Activity should not be null", mSolo.getCurrentActivity());
        mSolo.assertCurrentActivity("Sentinel Activitiy should have launched", Sentinel.class);
    }*/

    public void testSetDummyLocation() throws Exception {
        final Location location = new Location("gps");
        location.setLatitude(52.800000);
        location.setLongitude(-2.000000);

        mSentinelActivity.setLastLocation(location);
        assertNotNull("Last location should not be null", mSentinelActivity.getLastLocation());
        assertEquals("Latitudes should be equal", mSentinelActivity.getLastLocation().getLatitude(), location.getLatitude());
        assertEquals("Longitudes should be equal", mSentinelActivity.getLastLocation().getLongitude(), location.getLongitude());

        /*final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                assertNotNull("Last location should not be null", mSentinelActivity.getLastLocation());
                assertEquals("Latitudes should be equal", mSentinelActivity.getLastLocation().getLatitude(), location.getLatitude());
                assertEquals("Longitudes should be equal", mSentinelActivity.getLastLocation().getLongitude(), location.getLongitude());
            }
        });

        mSolo.assertCurrentActivity("Yeah", Sentinel.class);*/
    }

    public void testReorientingPreservesCurrentLocation() throws Exception {
        final Location location = new Location("gps");
        location.setLatitude(52.800000);
        location.setLongitude(-2.000000);

        mSentinelActivity.setLastLocation(location);
        mSolo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        assertNotNull("Last location should not be null", mSentinelActivity.getLastLocation());
        assertEquals("Latitudes should be equal", mSentinelActivity.getLastLocation().getLatitude(), location.getLatitude());
        assertEquals("Longitudes should be equal", mSentinelActivity.getLastLocation().getLongitude(), location.getLongitude());
    }

    public void testClickLogoutShowsAlertDialog() throws Exception {
        mSolo.clickOnMenuItem("Logout");
        assertTrue(mSolo.searchText("Are you sure you wish to logout?"));
        /* Robotium doesn't seem to be clicking on the "No" button, using goBack() instead */
        mSolo.goBack();
        mSolo.assertCurrentActivity("Current activity should be Sentinel activity", Sentinel.class);
    }

    public void testClockOutShowsNotification() throws Exception {
        mSolo.clickOnActionBarItem(R.id.clock_out_action);
        assertTrue("Notification should be shown", mSolo.waitForText("You may not begin your recorded break yet.", 1, 2000));
        mSolo.assertCurrentActivity("Current activity should return to the Sentinel activity", Sentinel.class);
    }

    public void testClockOutAfterFourHoursThirtyAllowsLoggingOut() throws Exception {
        SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(getActivity().getApplicationContext());

        long timeSessionBegan = sentinelSharedPreferences.getSessionBeginDateTime();
        long mockSessionBegan = timeSessionBegan - Time.FOUR_HOURS_TWENTY;
        sentinelSharedPreferences.setSessionBeginDateTime(mockSessionBegan);

        SentinelOnBreakActivity.isJunit = true;

        mSolo.clickOnActionBarItem(R.id.clock_out_action);
        mSolo.assertCurrentActivity("Current activity should be the break activity", SentinelOnBreakActivity.class);
        btnClockInView = mSolo.getView(R.id.btnClockIn);
        assertTrue("Clock in button should not be visible", View.INVISIBLE == btnClockInView.getVisibility());
        assertTrue("After 1 second, the break should end", mSolo.waitForText("00:00"));
        assertTrue("The clock in button should become visible", View.VISIBLE == btnClockInView.getVisibility());
        mSolo.clickOnView(btnClockInView);
        mSolo.assertCurrentActivity("Current activity should be Sentinel activity", Sentinel.class);
    }

    public void testShiftEnding() throws Exception {
        mSolo.waitForText("Remaining");
        mSolo.assertCurrentActivity("Current Activity should be Shift Ending Activity", SentinelShiftEndingActivity.class);
        btnClockOutView = mSolo.getView(R.id.btnClockOut);
        assertTrue("The clock out button should be invisible", View.INVISIBLE == btnClockOutView.getVisibility());
        assertTrue("After 1 second, the shift should end", mSolo.waitForText("00:00"));
        assertTrue("The clock out button should become visible", View.VISIBLE == btnClockOutView.getVisibility());
        mSolo.clickOnView(btnClockOutView);
        mSolo.assertCurrentActivity("Current activity should be Sentinel activity", SentinelLogin.class);
    }

    public void testPhoneReorientation() throws Exception {
        mSolo.setActivityOrientation(Solo.LANDSCAPE);
        assertTrue("Alert should be displayed", mSolo.waitForText("Device is not oriented correctly."));
        //mSolo.setActivityOrientation(Solo.PORTRAIT);
    }

    /* Can't unit test as this starts an activity to a different application and robotium doesn't
    * work across multiple instances */
    /*public void testScanDeliveryItemLaunchesZXIngActiviy() throws Exception {
        mSolo.clickOnText("Scan Delivery Item");
        mSolo.assertCurrentActivity("Current activity should be ZXing activity", GeotagDeliveryZXingActvity.class);
        mSolo.finishOpenedActivities();
    }*/
}
