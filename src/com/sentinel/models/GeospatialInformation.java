package com.sentinel.models;

public class GeospatialInformation
{

    private int iSessionID;
    private String oUserIndentification;
    private long lTimeStamp;
    private double dLongitude;
    private double dLatitude;
    private double dSpeed;
    private int iOrientation;

    public GeospatialInformation(int sessionID, String id, long stamp, double lng, double lat, int orientation, double speed)
    {
        iSessionID = sessionID;
        oUserIndentification = id;
        setDateTimeStamp(stamp);
        dLongitude = lng;
        dLatitude = lat;
        iOrientation = orientation;
        dSpeed = speed;
    }

    public String getUserIndentification()
    {
        return oUserIndentification;
    }

    public void setUserIndentification(String oUserIndentification)
    {
        this.oUserIndentification = oUserIndentification;
    }

    public long getDateTimeStamp()
    {
        return lTimeStamp;
    }

    public void setDateTimeStamp(long lTimeStamp)
    {
        this.lTimeStamp = lTimeStamp;
    }

    public double getLatitude()
    {
        return dLatitude;
    }

    public void setLatitude(double dLatitude)
    {
        this.dLatitude = dLatitude;
    }

    public double getLongitude()
    {
        return dLongitude;
    }

    public void setLongitude(double dLongitude)
    {
        this.dLongitude = dLongitude;
    }

    public double getSpeed()
    {
        return dSpeed;
    }

    public void setSpeed(double dSpeed)
    {
        this.dSpeed = dSpeed;
    }

    public int getOrientation()
    {
        return iOrientation;
    }

    public void setOrientation(int iOrientation)
    {
        this.iOrientation = iOrientation;
    }

    public int getSessionID()
    {
        return iSessionID;
    }

    public void setSessionID(int iSessionID)
    {
        this.iSessionID = iSessionID;
    }
}