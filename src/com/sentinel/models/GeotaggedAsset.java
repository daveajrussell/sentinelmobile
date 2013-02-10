package com.sentinel.models;

/**
 * David Russell
 * 24/01/13
 */
public class GeotaggedAsset {
    private String mAssetKey;
    private GeospatialInformation mGeospatialInformation;

    public String getAssetKey() {
        return mAssetKey;
    }

    public void setAssetKey(String assetKey) {
        mAssetKey = assetKey;
    }

    public GeospatialInformation getGeospatialInformation() {
        return mGeospatialInformation;
    }

    public void setGeospatialInformation(GeospatialInformation geospatialInformation) {
        mGeospatialInformation = geospatialInformation;
    }
}
