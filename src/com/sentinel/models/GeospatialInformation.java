package com.sentinel.models;

public class GeospatialInformation {

    private int mSessionID;
    private String mUserIndentification;
    private long mTimeStamp;
    private double mLongitude;
    private double mLatitude;
    private double mSpeed;
    private int mOrientation;

    public GeospatialInformation(int sessionID, String id, long stamp, double lng, double lat, double speed, int orientation) {
        mSessionID = sessionID;
        mUserIndentification = id;
        setDateTimeStamp(stamp);
        mLongitude = lng;
        mLatitude = lat;
        mSpeed = speed;
        mOrientation = orientation;
    }

    public String getUserIndentification() {
        return mUserIndentification;
    }

    public long getDateTimeStamp() {
        return mTimeStamp;
    }

    public void setDateTimeStamp(long lTimeStamp) {
        mTimeStamp = lTimeStamp;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getSpeed() {
        return mSpeed;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public int getSessionID() {
        return mSessionID;
    }
}