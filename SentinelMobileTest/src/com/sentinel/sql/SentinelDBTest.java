package com.sentinel.sql;

import android.content.Context;
import android.test.AndroidTestCase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sentinel.models.GeospatialInformation;

import java.util.Random;
import java.util.UUID;


public class SentinelDBTest extends AndroidTestCase {

    private Context mContext;
    private SentinelDB mSentinelDB;
    private GeospatialInformation mGeospatialInformation;

    String mockUserIdentification;
    long mockTimestamp;
    int mockSessionID;
    double mockLatitude;
    double mockLongitude;
    float mockSpeed;
    int mockOrientation;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mContext = getContext();
        mSentinelDB = new SentinelDB(mContext);

        mockUserIdentification = UUID.randomUUID().toString();
        mockSessionID = new Random().nextInt();
        mockLatitude = 52.810666;
        mockLongitude = -2.107994;
        mockSpeed = 0;
        mockOrientation = 1;

        mGeospatialInformation = new GeospatialInformation(mockSessionID, mockUserIdentification, mockTimestamp, mockLongitude, mockLatitude, mockSpeed, mockOrientation);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        mSentinelDB.deleteGeospatialData();
        mSentinelDB.closeSentinelDatabase();
    }

    public void testCloseSentinelDatabase() throws Exception {
        mSentinelDB.closeSentinelDatabase();
    }

    public void testAddGeospatialData() throws Exception {
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        assertTrue("Database should have 1 or more rows", mSentinelDB.getRowCount() >= 1);
    }

    public void testGetBufferedGeospatialDataCount() throws Exception {
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        assertEquals("Database should have 1 row", 1, mSentinelDB.getRowCount());
    }

    public void testGetBufferedGeospatialDataJsonString() throws Exception {
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        String geospatialJson = mSentinelDB.getBufferedGeospatialDataJsonString();

        assertNotNull(geospatialJson);

        JsonObject jsonObject = jsonDeserialiser(geospatialJson);

        String userID = new Gson().fromJson(jsonObject.get("oUserIdentification"), String.class);
        int sessionID = new Gson().fromJson(jsonObject.get("iSessionID"), Integer.class);
        long timeStamp = new Gson().fromJson(jsonObject.get("lTimeStamp"), Long.class);
        double latitude = new Gson().fromJson(jsonObject.get("dLatitude"), Double.class);
        double longitude = new Gson().fromJson(jsonObject.get("dLongitude"), Double.class);
        float speed = new Gson().fromJson(jsonObject.get("dSpeed"), Float.class);
        int orientation = new Gson().fromJson(jsonObject.get("iOrientation"), Integer.class);

        assertNotNull("the retrieved user ID must not be null", userID);
        assertNotNull("the retrieved session ID must not be null", sessionID);
        assertNotNull("the retrieved timestamp must not be null", timeStamp);
        assertNotNull("the retrieved latitude must not be null", latitude);
        assertNotNull("the retrieved longitude must not be null", longitude);
        assertNotNull("the retrieved altitude must not be null", speed);
        assertNotNull("the retrieved orientation must not be null", orientation);

        assertEquals("The input and retrieved user ID must be equal", mockUserIdentification, userID);
        assertEquals("The input and retrieved session ID must be equal", mockSessionID, sessionID);
        assertEquals("The input and retrieved timestamp must be equal", mockTimestamp, timeStamp);
        assertEquals("The input and retrieved latitude must be equal", mockLatitude, latitude);
        assertEquals("The input and retrieved longitude must be equal", mockLongitude, longitude);
        assertEquals("The input and retrieved speed must be equal", mockSpeed, speed);
        assertEquals("The input and retrieved orientation must be equal", mockOrientation, orientation);
    }

    public void testDeleteGeospatialData() throws Exception {
        assertEquals("Database should have 0 rows", 0, mSentinelDB.getRowCount());
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        assertTrue("Database should have only 1 row", mSentinelDB.getRowCount() == 1);
        mSentinelDB.deleteGeospatialData();
        assertEquals("Database should have 0 rows", 0, mSentinelDB.getRowCount());
    }

    public void testAddMultipleGeospatialDataSets() throws Exception {
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        mSentinelDB.addGeospatialData(mGeospatialInformation);
        mSentinelDB.addGeospatialData(mGeospatialInformation);

        assertTrue("Database should have 5 rows", mSentinelDB.getRowCount() == 5);
        String bufferedJson = mSentinelDB.getBufferedGeospatialDataJsonString();

        assertNotNull(bufferedJson);
    }

    private JsonObject jsonDeserialiser(String jsonToDeserialise) {
        JsonParser jsonParser = new JsonParser();
        String json = new Gson().fromJson(jsonToDeserialise, String.class);
        return jsonParser.parse(json).getAsJsonObject();
    }
}
