package com.sentinel.authentication;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import com.jayway.android.robotium.solo.Solo;
import com.sentinel.app.R;
import com.sentinel.app.Sentinel;

public class SentineLoginTest extends ActivityInstrumentationTestCase2<SentinelLogin> {

    private Solo solo;
    private EditText txtUsername;
    private EditText txtPassword;
    private View btnLoginView;

    public SentineLoginTest() {
        super(SentinelLogin.class);
    }

    @Override
    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
        txtUsername = (EditText) solo.getView(R.id.txt_username);
        txtPassword = (EditText) solo.getView(R.id.txt_password);
        btnLoginView = solo.getView(R.id.btn_login);
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testActivityCanBeLaunched() throws Exception {
        assertNotNull("activity should be launched successfully", getActivity());
    }

    public void testLoginControlsAreInView() throws Exception {
        assertNotNull("Username field should be in view", txtUsername);
        assertNotNull("Password field should be in view", txtPassword);
        assertNotNull("Login button should be in view", btnLoginView);
    }

    public void testEnterNoCredentials() throws Exception {
        solo.clearEditText(txtUsername);
        solo.clearEditText(txtPassword);
        solo.clickOnView(btnLoginView);

        assertTrue("Toast notification should indicate failure", solo.waitForText("You must enter a valid username and password"));
        solo.assertCurrentActivity("Expected to remain in SentinelLogin", SentinelLogin.class);
    }

    public void testUsernameNoPassword() throws Exception {
        solo.clearEditText(txtPassword);
        solo.clickOnView(btnLoginView);

        assertTrue("Toast notification should indicate failure", solo.waitForText("You must enter a valid password"));
        solo.assertCurrentActivity("Expected to remain in SentinelLogin", SentinelLogin.class);
    }

    public void testNoUsernamePassowrd() throws Exception {
        solo.clearEditText(txtUsername);
        solo.clickOnView(btnLoginView);

        assertTrue("Toast notification should indicate failure", solo.waitForText("You must enter a valid username"));
        solo.assertCurrentActivity("Expected to remain in SentinelLogin", SentinelLogin.class);
    }

    public void testValidCredentials() throws Exception {
        solo.clickOnView(btnLoginView);

        assertTrue("Username field should be disabled", !txtUsername.isEnabled());
        assertTrue("Password field should be disabled", !txtPassword.isEnabled());
        assertTrue("Login button should be disabled", !btnLoginView.isEnabled());

        assertTrue("Toast notification should indicate success", solo.waitForText("Authentication Successful"));

        solo.assertCurrentActivity("Sentinel Activitiy should have launched", Sentinel.class);
        performLogout();
    }

    public void testInvalidCredentials() throws Exception {
        solo.clearEditText(txtUsername);
        solo.clearEditText(txtPassword);
        solo.enterText(txtUsername, "WRONG");
        solo.enterText(txtPassword, "BAD");
        solo.clickOnView(btnLoginView);

        assertTrue("Toast notification should indicate failure", solo.waitForText("Authentication Unsuccessful"));
        solo.assertCurrentActivity("Sentinel Activitiy should have launched", SentinelLogin.class);
    }

    public void testLogout() throws Exception {
        performLogin();
        performLogout();
    }

    public void testBackButtonRelaunchesLogin() throws Exception {
        solo.assertCurrentActivity("Login acitivity should have launched", SentinelLogin.class);
        solo.goBack();
        solo.assertCurrentActivity("Login acitivity should have launched", SentinelLogin.class);
    }

    public void testBackButtonAfterLoggingInStaysInSentinelActivity() throws Exception {
        performLogin();
        solo.goBack();
        solo.assertCurrentActivity("Sentinel Activitiy should have launched", Sentinel.class);
        performLogout();
    }

    public void testHomeButtonAfterLoggingInStaysInSentinelActivity() throws Exception {
        performLogin();
        solo.clickOnActionBarHomeButton();
        performLogout();
    }

    public void testReorientActivity() throws Exception {
        solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        assertTrue("Username field should be disabled", txtUsername.isEnabled());
        assertTrue("Password field should be disabled", txtPassword.isEnabled());
        assertTrue("Login button should be disabled", btnLoginView.isEnabled());
    }

    public void testReorientedLogin() throws Exception {
        solo.setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        performLogin();
        performLogout();
    }

    private void performLogin() {
        solo.clickOnView(btnLoginView);
        solo.assertCurrentActivity("Sentinel Activitiy should have launched", Sentinel.class);
    }

    private void performLogout() {
        solo.clickOnMenuItem("Logout");
        solo.clickOnText("Yes");
        solo.assertCurrentActivity("Login acitivity should have launched", SentinelLogin.class);
    }
}
