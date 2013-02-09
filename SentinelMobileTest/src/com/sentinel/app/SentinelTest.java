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
    private Sentinel sentinelActivity;
    private Solo solo;
    private Handler handler;

    public SentinelTest() {
        super(SentinelLogin.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        btnLoginView = solo.getView(R.id.btn_login);
        btnClockInView = solo.getView(R.id.btnClockIn);
        sentinelActivity = performLogin();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void tearDown() throws Exception {
        performLogout();
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

        handler.post(new Runnable() {
            @Override
            public void run() {
                sentinelActivity.updateLocation(location);
                assertNotNull("Last location should not be null", sentinelActivity.getLastLocation());
                assertEquals("Last location should equal the location set by update location", sentinelActivity.getLastLocation(), location);
            }
        });
    }

    public void testReorientingPreservesCurrentLocation() throws Exception {
        final Location location = new Location("gps");
        location.setLatitude(52.800000);
        location.setLongitude(-2.000000);

        handler.post(new Runnable() {
            @Override
            public void run() {
                sentinelActivity.updateLocation(location);
                solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                assertNotNull("Last location should not be null", sentinelActivity.getLastLocation());
                assertEquals("Last location should equal the location set by update location", sentinelActivity.getLastLocation(), location);
                solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
    }

    public void testClickLogoutShowsAlertDialog() throws Exception {
        solo.clickOnMenuItem("Logout");
        assertTrue("A dialog should be shown that confirms the logout action", solo.waitForText("You still have "));
        solo.clickOnText("No");
        assertTrue("The dialog should close and return to the previous activity", solo.waitForDialogToClose(1000));
        solo.assertCurrentActivity("Current activity should be Sentinel activity", Sentinel.class);
    }

    public void testClockOutShowsNotification() throws Exception {
        solo.clickOnActionBarItem(R.id.clock_out_action);
        solo.assertCurrentActivity("Current activity should be the break activity", SentinelOnBreakActivity.class);
        assertTrue("Notification should be shown", solo.waitForText("You cannot begin your recorded break yet."));
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


    }
}
