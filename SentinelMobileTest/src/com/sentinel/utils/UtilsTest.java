package com.sentinel.utils;


import android.location.Location;
import junit.framework.TestCase;

public class UtilsTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetFormattedHrsMinsSecsTimeString() throws Exception {
        long twelveHoursFortyEightMinutesAndThirtySixSecondsInMillis = 46116000;
        String timeString;

        timeString = Utils.getFormattedHrsMinsSecsTimeString(twelveHoursFortyEightMinutesAndThirtySixSecondsInMillis);

        assertNotNull("String should not be null", timeString);
        assertEquals("Expected time string", "12:48:36", timeString);
    }

    public void testGetFormattedHrsMinsSecsTimeStringReturnsNullIfInvalid() throws Exception {
        long invalidTimeInMillis = -1231231;
        String timeString;

        timeString = Utils.getFormattedHrsMinsSecsTimeString(invalidTimeInMillis);

        assertNull("Time string should be null", timeString);
    }

    public void testGetFormattedMinsSecsTimeString() throws Exception {
        long twelveHoursFortyEightMinutesAndThirtySixSecondsInMillis = 46116000;
        String timeString;

        timeString = Utils.getFormattedMinsSecsTimeString(twelveHoursFortyEightMinutesAndThirtySixSecondsInMillis);

        assertNotNull("String should not be null", timeString);
        assertEquals("Expected time string", "48:36", timeString);
    }

    public void testGetFormattedMinsSecsTimeStringReturnsNullIfInvalid() throws Exception {
        long invalidTimeInMillis = -1231231;
        String timeString;

        timeString = Utils.getFormattedMinsSecsTimeString(invalidTimeInMillis);

        assertNull("Time string should be null", timeString);
    }

    public void testCheckUpdateIsMoreAccurateOne() throws Exception {
        Location mockLastLocation = new Location("GPS");
        long currentTime = System.currentTimeMillis();

        mockLastLocation.setTime(currentTime);
        mockLastLocation.setLatitude(52.810985);
        mockLastLocation.setLongitude(-2.108731);
        mockLastLocation.setAccuracy(67);

        Location mockNewLocationLessAccurateNewerDifferentProvider = new Location("NETWORK_PROVIDER");
        mockNewLocationLessAccurateNewerDifferentProvider.setTime(currentTime + 30000);
        mockNewLocationLessAccurateNewerDifferentProvider.setLatitude(52.812736);
        mockNewLocationLessAccurateNewerDifferentProvider.setLongitude(-2.112422);
        mockNewLocationLessAccurateNewerDifferentProvider.setAccuracy(43);

        assertTrue("Location is newer and from a different network provider, but is less accurate: less accurate", Utils.checkUpdateIsMoreAccurate(mockNewLocationLessAccurateNewerDifferentProvider, mockLastLocation, 20));
    }

    public void testCheckUpdateIsMoreAccurateTwo() throws Exception {
        Location mockLastLocation = new Location("GPS");
        long currentTime = System.currentTimeMillis();

        mockLastLocation.setTime(currentTime);
        mockLastLocation.setLatitude(52.810985);
        mockLastLocation.setLongitude(-2.108731);
        mockLastLocation.setAccuracy(67);

        Location mockNewLocationMoreAccurateNewerSameProvider = new Location("GPS");
        mockNewLocationMoreAccurateNewerSameProvider.setTime(currentTime + 30000);
        mockNewLocationMoreAccurateNewerSameProvider.setLatitude(52.812736);
        mockNewLocationMoreAccurateNewerSameProvider.setLongitude(-2.112422);
        mockNewLocationMoreAccurateNewerSameProvider.setAccuracy(90);

        assertTrue("Location is new, from same provider and greater than the time between updates: more accurate", Utils.checkUpdateIsMoreAccurate(mockNewLocationMoreAccurateNewerSameProvider, mockLastLocation, 20));
    }

    public void testCheckUpdateIsMoreAccurateThree() throws Exception {
        Location mockLastLocation = new Location("GPS");
        long currentTime = System.currentTimeMillis();

        mockLastLocation.setTime(currentTime);
        mockLastLocation.setLatitude(52.810985);
        mockLastLocation.setLongitude(-2.108731);
        mockLastLocation.setAccuracy(67);

        Location mockNewLocationLessAccurateSameTimeDifferentProvider = new Location("NETWORK_PROVIDER");
        mockNewLocationLessAccurateSameTimeDifferentProvider.setTime(currentTime);
        mockNewLocationLessAccurateSameTimeDifferentProvider.setLatitude(52.812736);
        mockNewLocationLessAccurateSameTimeDifferentProvider.setLongitude(-2.112422);
        mockNewLocationLessAccurateSameTimeDifferentProvider.setAccuracy(22);

        assertTrue("New provider, but same time and less accurate: less accurate", Utils.checkUpdateIsMoreAccurate(mockNewLocationLessAccurateSameTimeDifferentProvider, mockLastLocation, 20));
    }

    public void testCheckUpdateIsMoreAccurateFour() throws Exception {
        Location mockLastLocation = new Location("GPS");
        long currentTime = System.currentTimeMillis();

        mockLastLocation.setTime(currentTime);
        mockLastLocation.setLatitude(52.810985);
        mockLastLocation.setLongitude(-2.108731);
        mockLastLocation.setAccuracy(67);

        Location mockNewLocationMoreAccurateSameTimeDifferentProvider = new Location("NETWORK_PROIVDER");
        mockNewLocationMoreAccurateSameTimeDifferentProvider.setTime(currentTime);
        mockNewLocationMoreAccurateSameTimeDifferentProvider.setLatitude(52.812736);
        mockNewLocationMoreAccurateSameTimeDifferentProvider.setLongitude(-2.112422);
        mockNewLocationMoreAccurateSameTimeDifferentProvider.setAccuracy(82);

        assertFalse("Different provider and more accurate but same time: less accurate", Utils.checkUpdateIsMoreAccurate(mockNewLocationMoreAccurateSameTimeDifferentProvider, mockLastLocation, 20));
    }

    public void testCheckUpdateIsMoreAccurateFive() throws Exception {
        Location mockLastLocation = new Location("GPS");
        long currentTime = System.currentTimeMillis();

        mockLastLocation.setTime(currentTime);
        mockLastLocation.setLatitude(52.810985);
        mockLastLocation.setLongitude(-2.108731);
        mockLastLocation.setAccuracy(67);

        Location mockNewLocationMoreAccurateSameTimeSameProvider = new Location("GPS");
        mockNewLocationMoreAccurateSameTimeSameProvider.setTime(currentTime);
        mockNewLocationMoreAccurateSameTimeSameProvider.setLatitude(52.812736);
        mockNewLocationMoreAccurateSameTimeSameProvider.setLongitude(-2.112422);
        mockNewLocationMoreAccurateSameTimeSameProvider.setAccuracy(82);

        assertFalse("Better accuracy, but same time and provider: less accurate", Utils.checkUpdateIsMoreAccurate(mockNewLocationMoreAccurateSameTimeSameProvider, mockLastLocation, 20));
    }

    public void testCheckUpdateIsMoreAccurateSix() throws Exception {
        Location mockLastLocation = new Location("GPS");
        long currentTime = System.currentTimeMillis();

        mockLastLocation.setTime(currentTime);
        mockLastLocation.setLatitude(52.810985);
        mockLastLocation.setLongitude(-2.108731);
        mockLastLocation.setAccuracy(67);

        Location mockNewLocationLessAccurateNewerSameProvider = new Location("GPS");
        mockNewLocationLessAccurateNewerSameProvider.setTime(currentTime + 30000);
        mockNewLocationLessAccurateNewerSameProvider.setLatitude(52.812736);
        mockNewLocationLessAccurateNewerSameProvider.setLongitude(-2.112422);
        mockNewLocationLessAccurateNewerSameProvider.setAccuracy(34);

        assertFalse("Less accurate and same provider but is newer: more accurate", Utils.checkUpdateIsMoreAccurate(mockNewLocationLessAccurateNewerSameProvider, mockLastLocation, 20));
    }

    public void testCheckUpdateIsMoreAccurateSeven() throws Exception {
        long currentTime = System.currentTimeMillis();

        Location newLocation = new Location("GPS");
        newLocation.setTime(currentTime);
        newLocation.setLatitude(52.812736);
        newLocation.setLongitude(-2.112422);
        newLocation.setAccuracy(34);

        assertTrue("Any location is better than no location", Utils.checkUpdateIsMoreAccurate(newLocation, null, 20));
    }
}
