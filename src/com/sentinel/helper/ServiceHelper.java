package com.sentinel.helper;

import android.content.Context;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

/**
 * David Russell
 * 27/01/13
 */
public class ServiceHelper {
    private static String strPostResult;
    private static int iReponseCode;

    private static HttpClient httpClient;
    private static HttpPost httpPost;
    private static StringEntity stringEntity;
    private static HttpResponse httpResponse;

    private static String doPostResult;

    public static String doPost(String methodName, String url, String entity) {
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url + methodName);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");

            Log.i("SENTINEL_SERVICE_HELPER", "Passing: " + entity + " to " + url + methodName);

            stringEntity = new StringEntity(entity);
            httpPost.setEntity(stringEntity);
            httpResponse = httpClient.execute(httpPost);

            Log.i("SENTINEL_SERVICE_HELPER", "Response Code: " + httpResponse.getStatusLine() + " returned from " + url + methodName);

            iReponseCode = httpResponse.getStatusLine().getStatusCode();
            strPostResult = getResponse(iReponseCode);

        } catch (Exception e) {
            e.printStackTrace();
            strPostResult = ResponseStatusHelper.EXCEPTION_THROWN_RESULT;
        }

        return strPostResult;
    }

    public static String doPostAndLogin(Context context, String methodName, String url, String entity) {
        doPostResult = doPost(methodName, url, entity);

        if (doPostResult == ResponseStatusHelper.OK_RESULT) {
            LoginHelper.loginToSystem(httpResponse, context);
        }

        return doPostResult;
    }

    private static String getResponse(int iReponseCode) {
        switch (iReponseCode) {
            case ResponseStatusHelper.OK:
                strPostResult = ResponseStatusHelper.OK_RESULT;
                break;
            case ResponseStatusHelper.BAD_REQUEST:
                strPostResult = ResponseStatusHelper.BAD_REQUEST_RESULT;
                break;
            case ResponseStatusHelper.UNAUTHORIZED:
                strPostResult = ResponseStatusHelper.UNAUTHORIZED_RESULT;
                break;
            case ResponseStatusHelper.NOT_FOUND:
                strPostResult = ResponseStatusHelper.NOT_FOUND_RESULT;
                break;
            case ResponseStatusHelper.INTERNAL_SERVER_ERROR:
                strPostResult = ResponseStatusHelper.INTERNAL_SERVER_ERROR_RESULT;
                break;
            default:
                strPostResult = ResponseStatusHelper.OTHER_ERROR_RESULT;
                break;
        }

        return strPostResult;
    }
}
