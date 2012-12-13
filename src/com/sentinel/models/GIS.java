package com.sentinel.models;

public class GIS {

    private long lngDateTimeStamp;
    private double dLongitude;
    private double dLatitude;
    private int intOrientation;
    private double dSpeed;

    public GIS(long lngDateTimeStamp, double lng, double lat, int orientation, double speed) {
        setDateTimeStamp(lngDateTimeStamp);
        dLongitude = lng;
        dLatitude = lat;
        intOrientation = orientation;
        dSpeed = speed;
    }

    public double getSpeed() {
        return dSpeed;
    }

    public void setSpeed(double dSpeed) {
        this.dSpeed = dSpeed;
    }

    public int getOrientation() {
        return intOrientation;
    }

    public void setOrientation(int intOrientation) {
        this.intOrientation = intOrientation;
    }

    public double getLongitude() {
        return dLongitude;
    }

    public void setLongitude(double dLongitude) {
        this.dLongitude = dLongitude;
    }

    public double getLatitude() {
        return dLatitude;
    }

    public void setLatitude(double dLatitude) {
        this.dLatitude = dLatitude;
    }

    public long getDateTimeStamp() {
        return lngDateTimeStamp;
    }

    public void setDateTimeStamp(long lngDateTimeStamp) {
        this.lngDateTimeStamp = lngDateTimeStamp;
    }
}