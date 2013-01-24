package com.sentinel.asset;

import android.content.Context;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.tracking.TrackingHelper;
import org.json.JSONStringer;

/**
 * David Russell
 * 24/01/13
 */
public class AssetHelper
{
    public static String getGeoTaggedAssetJson(Context oContext, String strAssetID)
    {
        GeospatialInformation oLastKnownInformation = TrackingHelper.getLastKnownGeospatialInformation(oContext);
        return buildGeoTaggedAssetJson(oLastKnownInformation, strAssetID);
    }

    public static String buildGeoTaggedAssetJson(GeospatialInformation oLastKnownInformation, String strAssetID)
    {
        String strGeoInformationJson;

        try
        {
            strGeoInformationJson = new JSONStringer()
                    .object()
                        .key("oAssetKey").value(strAssetID)
                        .key("iSessionID").value(oLastKnownInformation.getSessionID())
                        .key("oUserIdentification").value(oLastKnownInformation.getUserIndentification())
                        .key("lTimeStamp").value(oLastKnownInformation.getDateTimeStamp())
                        .key("dLatitude").value(oLastKnownInformation.getLatitude())
                        .key("dLongitude").value(oLastKnownInformation.getLongitude())
                        .key("dSpeed").value(oLastKnownInformation.getSpeed())
                        .key("iOrientation").value(oLastKnownInformation.getOrientation())
                    .endObject().toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            strGeoInformationJson = null;
        }

        return strGeoInformationJson;
    }
}
