package com.sentinel;

public class GIS {

    private long lngDateTimeStamp;
    private double dblLongitude;
    private double dblLatitude;
    private int intOrientation;
    private double dblSpeed;

    public GIS(long lngDateTimeStamp, double lng, double lat, int orientation, double speed) {
        setDateTimeStamp(lngDateTimeStamp);
        dblLongitude = lng;
        dblLatitude = lat;
        intOrientation = orientation;
        dblSpeed = speed;
    }

    public double getSpeed() {
        return dblSpeed;
    }

    public void setSpeed(double dblSpeed) {
        this.dblSpeed = dblSpeed;
    }

    public int getOrientation() {
        return intOrientation;
    }

    public void setOrientation(int intOrientation) {
        this.intOrientation = intOrientation;
    }

    public double getLongitude() {
        return dblLongitude;
    }

    public void setLongitude(double dblLongitude) {
        this.dblLongitude = dblLongitude;
    }

    public double getLatitude() {
        return dblLatitude;
    }

    public void setLatitude(double dblLatitude) {
        this.dblLatitude = dblLatitude;
    }

    public long getDateTimeStamp() {
        return lngDateTimeStamp;
    }

    public void setDateTimeStamp(long lngDateTimeStamp) {
        this.lngDateTimeStamp = lngDateTimeStamp;
    }
}