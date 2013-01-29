package com.sentinel.models;

/**
 * David Russell
 * 24/01/13
 */
public class GeotaggedAsset {
    private String strAssetKey;
    private GeospatialInformation oGeospatialInformation;

    public String getAssetKey() {
        return strAssetKey;
    }

    public void setAssetKey(String strAssetKey) {
        this.strAssetKey = strAssetKey;
    }

    public GeospatialInformation geGeospatialInformation() {
        return oGeospatialInformation;
    }

    public void setGeospatialInformation(GeospatialInformation oGeospatialInformation) {
        this.oGeospatialInformation = oGeospatialInformation;
    }
}
