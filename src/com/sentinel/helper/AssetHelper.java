package com.sentinel.helper;

import android.content.Context;
import com.sentinel.models.GeospatialInformation;
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

        if (oLastKnownInformation != null)
            return buildGeoTaggedAssetJson(oLastKnownInformation, strAssetID);
        else
            return null;
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
