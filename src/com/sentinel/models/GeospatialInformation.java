package com.sentinel.models;

public class GeospatialInformation {

    private int iSessionID;
    private String oUserIndentification;
    private long lTimeStamp;
    private double dLongitude;
    private double dLatitude;
    private double dSpeed;
    private int iOrientation;

    public GeospatialInformation(int sessionID, String id, long stamp, double lng, double lat, double speed, int orientation) {
        iSessionID = sessionID;
        oUserIndentification = id;
        setDateTimeStamp(stamp);
        dLongitude = lng;
        dLatitude = lat;
        dSpeed = speed;
        iOrientation = orientation;
    }

    public String getUserIndentification() {
        return oUserIndentification;
    }

    public long getDateTimeStamp() {
        return lTimeStamp;
    }

    public void setDateTimeStamp(long lTimeStamp) {
        this.lTimeStamp = lTimeStamp;
    }

    public double getLatitude() {
        return dLatitude;
    }

    public double getLongitude() {
        return dLongitude;
    }

    public double getSpeed() {
        return dSpeed;
    }

    public int getOrientation() {
        return iOrientation;
    }

    public int getSessionID() {
        return iSessionID;
    }
}