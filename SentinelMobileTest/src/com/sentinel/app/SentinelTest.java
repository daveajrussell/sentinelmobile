package com.sentinel.app;

import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class SentinelTest extends ActivityUnitTestCase<Sentinel> {

    private Sentinel sentinelActivity;

    public SentinelTest() {
        super(Sentinel.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Intent sentinelIntent = new Intent(getInstrumentation().getTargetContext(), Sentinel.class);
        startActivity(sentinelIntent, null, null);

        sentinelActivity = getActivity();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSomething() throws Exception {
        sentinelActivity.onCreate(null);
    }
}
