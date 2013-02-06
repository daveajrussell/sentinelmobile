package com.sentinel.tracking;

import android.content.Intent;
import android.location.Location;
import android.test.ServiceTestCase;

public class SentinelLocationServiceTest extends ServiceTestCase<SentinelLocationService> {

    public SentinelLocationServiceTest() {
        super(SentinelLocationService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        SentinelLocationService.isJUnit = true;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testServiceCanBeLaunched() throws Exception {
        bindService(new Intent());
        startService(new Intent());
        assertNotNull("activity should be launched successfully", getService());
    }

    public void testHandleLocationChanged() throws Exception {
        bindService(new Intent());
        startService(new Intent());
        SentinelLocationService sentinelLocationService = getService();

        Location location = new Location("gps");
        location.setLatitude(52.810666);
        location.setLongitude(-2.107994);

        sentinelLocationService.handleLocationChanged(location);
    }
}
