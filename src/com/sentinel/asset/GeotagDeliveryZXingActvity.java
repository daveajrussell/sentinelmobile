package com.sentinel.asset;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sentinel.app.Sentinel;
import com.sentinel.helper.AssetHelper;
import com.sentinel.helper.ResponseStatusHelper;
import com.sentinel.helper.ServiceHelper;

/**
 * David Russell
 * 23/01/13
 */
public class GeotagDeliveryZXingActvity extends Activity
{
    private String processResult;
    private String strGeoTaggedAssetJson;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(GeotagDeliveryZXingActvity.this);
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
            final String strResultContents = result.getContents();
            if (strResultContents != null)
            {
                oResultDialog.setMessage(strResultContents);
                oResultDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        new AssetServiceAsyncTask(getApplicationContext()).execute(strResultContents);
                    }
                });
            } else
            {
                oResultDialog.setMessage("QR Scan Failed");
                oResultDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Intent sentinelIntent = new Intent(getApplicationContext(), Sentinel.class);
                        sentinelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(sentinelIntent);
                    }
                });
            }
        }

        oResultDialog.show();
    }

    private class AssetServiceAsyncTask extends AsyncTask<String, Integer, String>
    {
        private final String METHOD_NAME = "/GeoTagDelivery";
        private final String URL = "http://webservices.daveajrussell.com/Services/DeliveryService.svc";
        private Context oContext;

        public AssetServiceAsyncTask(Context context)
        {
            oContext = context;
        }

        @Override
        protected String doInBackground(String... strings)
        {
            if (!strings[0].isEmpty())
            {
                String strAssetID = strings[0];
                strGeoTaggedAssetJson = AssetHelper.getGeoTaggedAssetJson(oContext, strAssetID);
                processResult = ServiceHelper.doPost(METHOD_NAME, URL, strGeoTaggedAssetJson);
            }
            return processResult;
        }

        @Override
        protected void onPostExecute(String result)
        {
            AlertDialog.Builder oDeliveryAlert;

            if (result == ResponseStatusHelper.OK_RESULT)
            {
                oDeliveryAlert = new AlertDialog.Builder(oContext);
                oDeliveryAlert.setTitle("Delivery Successful");
                oDeliveryAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Intent sentinelIntent = new Intent(oContext, Sentinel.class);
                        sentinelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        oContext.startActivity(sentinelIntent);
                    }
                });
            } else
            {
                oDeliveryAlert = new AlertDialog.Builder(oContext);
                oDeliveryAlert.setTitle("Delivery Failed");
                oDeliveryAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        Intent qrRetryIntent = new Intent(oContext, GeotagDeliveryZXingActvity.class);
                        qrRetryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        oContext.startActivity(qrRetryIntent);
                    }
                });
            }
            oDeliveryAlert.show();
        }
    }
}