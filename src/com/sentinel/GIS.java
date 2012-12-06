package com.sentinel;

/**
 * Created with IntelliJ IDEA.
 * User: Dave
 * Date: 05/12/12
 * Time: 19:46
 * To change this template use File | Settings | File Templates.
 */
public class GIS {

    private long m_lngDateTimeStamp;
    private double m_dLongitude;
    private double m_dLatitude;
    private int m_intOrientation;
    private double m_dSpeed;

    public GIS() {
    }

    public GIS(long lngDateTimeStamp, double lng, double lat, int orientation, double speed) {
        setDateTimeStamp(lngDateTimeStamp);
        m_dLongitude = lng;
        m_dLatitude = lat;
        m_intOrientation = orientation;
        m_dSpeed = speed;
    }

    public double getSpeed() {
        return m_dSpeed;
    }

    public void setSpeed(double m_dblSpeed) {
        this.m_dSpeed = m_dblSpeed;
    }

    public int getOrientation() {
        return m_intOrientation;
    }

    public void setOrientation(int m_intOrientation) {
        this.m_intOrientation = m_intOrientation;
    }

    public double getLongitude() {
        return m_dLongitude;
    }

    public void setLongitude(double m_dLongitude) {
        this.m_dLongitude = m_dLongitude;
    }

    public double getLatitude() {
        return m_dLatitude;
    }

    public void setLatitude(double m_dLatitude) {
        this.m_dLatitude = m_dLatitude;
    }

    public long getDateTimeStamp() {
        return m_lngDateTimeStamp;
    }

    public void setDateTimeStamp(long m_lngDateTimeStamp) {
        this.m_lngDateTimeStamp = m_lngDateTimeStamp;
    }
}