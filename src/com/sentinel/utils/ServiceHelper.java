package com.sentinel.utils;

import android.content.Context;
import android.util.Log;
import com.sentinel.services.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public abstract class ServiceHelper {

    public static String doPost(final Context context, final String methodName, final String url, final String entity, final boolean login) {
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url + methodName);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

            Log.i("SENTINEL_SERVICE_HELPER", "Passing: " + entity + " to " + url + methodName);

            StringEntity stringEntity = new StringEntity(entity);
            httpPost.setEntity(stringEntity);
            HttpResponse httpResponse = httpClient.execute(httpPost);

            Log.i("SENTINEL_SERVICE_HELPER", "Response Code: " + httpResponse.getStatusLine() + " returned from " + url + methodName);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String response = getResponse(statusCode);

            if (login) {
                if (response.equals(HttpResponseCode.OK_RESULT)) {
                    AuthenticationHelper.performLogin(httpResponse, context);
                }
            }
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return HttpResponseCode.EXCEPTION_THROWN_RESULT;
        }
    }

    private static String getResponse(int responseCode) {
        switch (responseCode) {
            case HttpResponseCode.OK:
                return HttpResponseCode.OK_RESULT;
            case HttpResponseCode.BAD_REQUEST:
                return HttpResponseCode.BAD_REQUEST_RESULT;
            case HttpResponseCode.UNAUTHORIZED:
                return HttpResponseCode.UNAUTHORIZED_RESULT;
            case HttpResponseCode.NOT_FOUND:
                return HttpResponseCode.NOT_FOUND_RESULT;
            case HttpResponseCode.INTERNAL_SERVER_ERROR:
                return HttpResponseCode.INTERNAL_SERVER_ERROR_RESULT;
            default:
                return HttpResponseCode.OTHER_ERROR_RESULT;
        }
    }

    public static void sendGISToLocationService(final Context context, final String strGeospatialJson) {
        new LocationServiceAsyncTask(context).execute(strGeospatialJson);
    }

    public static void sendBufferedGeospatialDataToLocationService(final Context context, final String strGeospatialJsonSet) {
        new BufferedGeospatialDataAsyncTask(context).execute(strGeospatialJsonSet);
    }

    public static void sendHistoricalDataToLocationService(final Context context, final String historicalGeospatialJson) {
        new HistoricalGeospatialDataAsyncTask(context).execute(historicalGeospatialJson);
    }

    public static void sendSpeedingNotification(final String speedingNotificationJson) {
        new SpeedingNotificationAsyncTask().execute(speedingNotificationJson);
    }

    public static void sendOrientationNotification(final String orientationNotificationJson) {
        new OrientationNotificationAsyncTask().execute(orientationNotificationJson);
    }
}
