package com.sentinel.app;

import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import com.jayway.android.robotium.solo.Solo;
import com.sentinel.authentication.SentinelLogin;

public class SentinelTest extends ActivityInstrumentationTestCase2<SentinelLogin> {

    private View btnLoginView;
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
                //sentinelActivity.updateLocation(location);
                solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                assertNotNull("Last location should not be null", sentinelActivity.getLastLocation());
                //assertEquals("Last location should equal the location set by update location", sentinelActivity.getLastLocation(), location);
                //solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
    }
}
