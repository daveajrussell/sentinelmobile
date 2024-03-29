package com.sentinel.asset;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sentinel.app.Sentinel;
import com.sentinel.utils.HttpResponseCode;
import com.sentinel.utils.JsonBuilder;
import com.sentinel.utils.ServiceHelper;

/**
 * David Russell
 * 23/01/13
 */
public class GeotagDeliveryZXingActvity extends Activity {
    private String processResult;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(GeotagDeliveryZXingActvity.this);

        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (result != null) {
            final String strResultContents = result.getContents();
            if (strResultContents != null) {
                new AssetServiceAsyncTask(this).execute(strResultContents);

                Intent sentinelIntent = new Intent(this, Sentinel.class);
                startActivity(sentinelIntent);
            } else {
                Toast.makeText(this, "Scan Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AssetServiceAsyncTask extends AsyncTask<String, Integer, String> {
        private final String METHOD_NAME = "/GeoTagDelivery";
        private final String URL = "http://webservices.daveajrussell.com/Services/DeliveryService.svc";
        private Context mContext;

        public AssetServiceAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            if (!strings[0].isEmpty()) {
                String assetID = strings[0];
                String strGeoTaggedAssetJson = JsonBuilder.geoTaggedAssetJson(mContext, assetID);
                processResult = ServiceHelper.doPost(null, METHOD_NAME, URL, strGeoTaggedAssetJson, false);
            }
            return processResult;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(HttpResponseCode.OK_RESULT)) {
                Toast.makeText(mContext, "Delivery Successful", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "Delivery Failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}