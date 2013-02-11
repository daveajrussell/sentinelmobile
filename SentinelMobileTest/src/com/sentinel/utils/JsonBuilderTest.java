package com.sentinel.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.test.AndroidTestCase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sentinel.models.Credentials;
import com.sentinel.preferences.SentinelSharedPreferences;

import java.util.Random;
import java.util.UUID;

public class JsonBuilderTest extends AndroidTestCase {

    private static final String MOCK_PROVIDER = "MOCK_PROVIDER";

    String mockAssetID;
    String mockUserIdentification;
    long mockTimestamp;
    int mockSessionID;
    double mockLatitude;
    double mockLongitude;
    double mockAltitude;
    float mockAccuracy;
    float mockSpeed;
    int mockOrientation;
    private Credentials mCredentials;
    private Location mLocation;
    private LocationManager mLocationManager;
    private Context mContext;
    private SentinelSharedPreferences mSharedPreferences;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mLocation = new Location("MOCK_PROVIDER");
        mockAssetID = UUID.randomUUID().toString();
        mockTimestamp = System.currentTimeMillis();
        mockUserIdentification = UUID.randomUUID().toString();
        mockSessionID = new Random().nextInt();
        mockLatitude = 52.810666;
        mockLongitude = -2.107994;
        mockSpeed = 0;
        mockAltitude = 56;
        mockAccuracy = 80;
        mockOrientation = 1;

        mContext = getContext();

        mSharedPreferences = new SentinelSharedPreferences(mContext);
        mSharedPreferences.setUserPreferences(mockUserIdentification, mockSessionID);

        String mockUsername = "DR_DRIVER";
        String mockPassword = "randomness";
        mCredentials = new Credentials(mockUsername, mockPassword);

        mLocation.setTime(mockTimestamp);
        mLocation.setElapsedRealtimeNanos(mockTimestamp);
        mLocation.setLatitude(mockLatitude);
        mLocation.setLongitude(mockLongitude);
        mLocation.setAltitude(mockAltitude);
        mLocation.setAccuracy(mockAccuracy);

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (null == mLocationManager.getProvider(MOCK_PROVIDER)) {
            mLocationManager.addTestProvider(MOCK_PROVIDER, true, true, true, false, false, true, false, 0, 0);
        }

        if (!mLocationManager.isProviderEnabled(MOCK_PROVIDER)) {
            mLocationManager.setTestProviderEnabled(MOCK_PROVIDER, true);
        }
        mLocationManager.setTestProviderLocation(MOCK_PROVIDER, mLocation);

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mLocationManager.removeTestProvider(MOCK_PROVIDER);
    }

    public void testUserCredentialsJsonFromCredentials() throws Exception {
        String mockCredentialsJson = JsonBuilder.userCredentialsJson(mCredentials);

        assertNotNull("Json string must not be null", mockCredentialsJson);

        JsonObject jsonObject = jsonDeserialiser(mockCredentialsJson);

        String username = new Gson().fromJson(jsonObject.get("strUsername"), String.class);
        String password = new Gson().fromJson(jsonObject.get("strPassword"), String.class);

        assertNotNull("the retrieved username must not be null", username);
        assertNotNull("the retrieved password must not be null", password);

        assertEquals("The input username and retrieved username must be equal", username, mCredentials.getUsername());
        assertEquals("The input password and retrieved password must be equal", password, mCredentials.getPassword());
    }

    public void testUserCredentialsJsonFromContext() throws Exception {
        mSharedPreferences.setUserPreferences(mockUserIdentification, mockSessionID);

        String credentialsJson = JsonBuilder.userCredentialsJson(getContext());

        assertNotNull("Json string must not be null", credentialsJson);

        JsonObject jsonObject = jsonDeserialiser(credentialsJson);

        String userIdentification = new Gson().fromJson(jsonObject.get("oUserIdentification"), String.class);
        int sessionID = new Gson().fromJson(jsonObject.get("iSessionID"), Integer.class);

        assertNotNull("the retrieved user ID must not be null", userIdentification);
        assertNotNull("the retrieved session ID must not be null", sessionID);

        assertEquals("The input user ID and retrieved user ID must be equal", mockUserIdentification, userIdentification);
        assertEquals("The input session ID and retrieved session ID must be equal", mockSessionID, sessionID);
    }

    public void testGeospatialDataJsonFromContext() throws Exception {
        mSharedPreferences.setUserPreferences(mockUserIdentification, mockSessionID);

        String geospatialJson = JsonBuilder.geospatialDataJson(getContext(), mLocation);

        assertNotNull("Json string must not be null", geospatialJson);

        JsonObject jsonObject = jsonDeserialiser(geospatialJson);

        double latitude = new Gson().fromJson(jsonObject.get("dLatitude"), Double.class);
        double longitude = new Gson().fromJson(jsonObject.get("dLongitude"), Double.class);
        float speed = new Gson().fromJson(jsonObject.get("dSpeed"), Float.class);

        assertNotNull("the retrieved latitude must not be null", latitude);
        assertNotNull("the retrieved longitude must not be null", longitude);
        assertNotNull("the retrieved altitude must not be null", speed);

        assertEquals("The input and retrieved latitude must be equal", mockLatitude, latitude);
        assertEquals("The input and retrieved longitude must be equal", mockLongitude, longitude);
        assertEquals("The input and retrieved speed must be equal", mockSpeed, speed);
    }

    public void testGeoTaggedAssetJson() throws Exception {
        String assetJson = JsonBuilder.geoTaggedAssetJson(getContext(), mockAssetID);

        assertNotNull("Json string must not be null", assetJson);

        JsonObject jsonObject = jsonDeserialiser(assetJson);

        String assetID = new Gson().fromJson(jsonObject.get("oAssetKey"), String.class);
        String userID = new Gson().fromJson(jsonObject.get("oUserIdentification"), String.class);
        int sessionID = new Gson().fromJson(jsonObject.get("iSessionID"), Integer.class);
        long timeStamp = new Gson().fromJson(jsonObject.get("lTimeStamp"), Long.class);
        double latitude = new Gson().fromJson(jsonObject.get("dLatitude"), Double.class);
        double longitude = new Gson().fromJson(jsonObject.get("dLongitude"), Double.class);
        float speed = new Gson().fromJson(jsonObject.get("dSpeed"), Float.class);
        int orientation = new Gson().fromJson(jsonObject.get("iOrientation"), Integer.class);

        assertNotNull("the retrieved asset ID must not be null", assetID);
        assertNotNull("the retrieved user ID must not be null", userID);
        assertNotNull("the retrieved session ID must not be null", sessionID);
        assertNotNull("the retrieved timestamp must not be null", timeStamp);
        assertNotNull("the retrieved latitude must not be null", latitude);
        assertNotNull("the retrieved longitude must not be null", longitude);
        assertNotNull("the retrieved altitude must not be null", speed);
        assertNotNull("the retrieved orientation must not be null", orientation);

        assertEquals("The input and retrieved asset ID must be equal", mockAssetID, assetID);
        assertEquals("The input and retrieved user ID must be equal", mockUserIdentification, userID);
        assertEquals("The input and retrieved session ID must be equal", mockSessionID, sessionID);
        assertEquals("The input and retrieved latitude must be equal", mockLatitude, latitude);
        assertEquals("The input and retrieved longitude must be equal", mockLongitude, longitude);
        assertEquals("The input and retrieved speed must be equal", mockSpeed, speed);
        assertEquals("The input and retrieved orientation must be equal", mockOrientation, orientation);
    }

    private JsonObject jsonDeserialiser(String jsonToDeserialise) {
        JsonParser jsonParser = new JsonParser();
        String json = new Gson().fromJson(jsonToDeserialise, String.class);
        return jsonParser.parse(json).getAsJsonObject();
    }
}
