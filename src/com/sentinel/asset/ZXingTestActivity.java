package com.sentinel.asset;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sentinel.Sentinel;
import com.sentinel.helper.ResponseStatusHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

/**
 * David Russell
 * 23/01/13
 */
public class ZXingTestActivity extends Activity
{
    private String strProcessResult;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(ZXingTestActivity.this);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        AlertDialog.Builder oResultDialog = new AlertDialog.Builder(this);
        oResultDialog.setTitle("QR Result");

        if (result != null)
        {
            String strResultContents = result.getContents();
            if (strResultContents != null)
            {
                oResultDialog.setMessage(strResultContents);
                oResultDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                });
            }
            else
            {
                oResultDialog.setMessage("QR Scan Failed");
                oResultDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                });
            }
        }

        oResultDialog.show();
    }

    // send: URL, json string of Geo data (includes user key to record delivery against)
    private class AssetServiceAsyncTask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings)
        {
            if (!strings[0].isEmpty() && !strings[1].isEmpty())
            {
                String strAssetURL = strings[0];
                String strGeoDataJson = strings[1];

                try
                {
                    HttpClient oAssetServiceHttpClient = new DefaultHttpClient();
                    HttpPost oAssetServiceHttpPost = new HttpPost(strAssetURL);

                    // define entity
                    // set header type

                    Log.i("SENTINEL_INFO", "Calling: " + strAssetURL);

                    HttpResponse oAssetServiceResponseCode = oAssetServiceHttpClient.execute(oAssetServiceHttpPost);

                    Log.i("SentinelWebService", "Response Status: " + oAssetServiceResponseCode.getStatusLine());

                    int iStatus = oAssetServiceResponseCode.getStatusLine().getStatusCode();
                    switch (iStatus)
                    {
                        case ResponseStatusHelper.OK:
                            // deserialize and return the data
                            strProcessResult = ResponseStatusHelper.OK_RESULT;
                            break;
                        case ResponseStatusHelper.BAD_REQUEST:
                            strProcessResult = ResponseStatusHelper.BAD_REQUEST_RESULT;
                            break;
                        case ResponseStatusHelper.UNAUTHORIZED:
                            strProcessResult = ResponseStatusHelper.UNAUTHORIZED_RESULT;
                            break;
                        case ResponseStatusHelper.NOT_FOUND:
                            strProcessResult = ResponseStatusHelper.NOT_FOUND_RESULT;
                            break;
                        case ResponseStatusHelper.INTERNAL_SERVER_ERROR:
                            strProcessResult = ResponseStatusHelper.INTERNAL_SERVER_ERROR_RESULT;
                            break;
                        default:
                            strProcessResult = ResponseStatusHelper.OTHER_ERROR_RESULT;
                            break;
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            return strProcessResult;
        }
    }
}