package com.sentinel.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.sentinel.Sentinel;
import com.sentinel.preferences.SentinelSharedPreferences;

/**
 * David Russell
 * 10/12/12
 */
public class LoginServiceBroadcastReceiver extends BroadcastReceiver {

    public static final String USER_IDENTIFICATION;
    public static final String CREDENTIALS_AUTHENTICATION;


    static {
        USER_IDENTIFICATION = "USER_IDENTIFICATION";
        CREDENTIALS_AUTHENTICATION = "com.sentinel.auth.action.CREDENTIALS_AUTHENTICATION";
    }

    public void onReceive(Context context, Intent intent) {
            SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(context);
            String strUserIdentification = intent.getStringExtra(USER_IDENTIFICATION);

            oSentinelSharedPreferences.setUserPreferences(strUserIdentification);

            Intent sentinelIntent = new Intent(context, Sentinel.class);
            sentinelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sentinelIntent);
    }
}
