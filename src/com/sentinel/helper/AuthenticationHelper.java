package com.sentinel.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.sentinel.app.R;
import com.sentinel.authentication.LogoutAsyncTask;
import com.sentinel.authentication.SentinelLogin;
import com.sentinel.preferences.SentinelSharedPreferences;

public class AuthenticationHelper {

    public static void performLogoutWithDialog(final Context context) {

        final SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);

        long lngShiftEndTimeDifference = sentinelSharedPreferences.getDrivingEndAlarm() - System.currentTimeMillis();
        String message;

        String time = Utils.getFormattedHrsMinsSecsTimeString(lngShiftEndTimeDifference);
        message = String.format("You still have %1$s of your shift remaining. Are you sure you wish to logout?", time);

        new AlertDialog.Builder(context)
                .setTitle(R.string.alert_title)
                .setMessage(message)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout(context);
                    }
                }).show();
    }

    public static void performLogout(final Context context) {
        final SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);
        TrackingHelper.stopLocationService(context);

        String userCredentialsJson = JsonBuilder.userCredentialsJson(context);

        sentinelSharedPreferences.clearSharedPreferences();

        Intent loginIntent = new Intent(context, SentinelLogin.class);
        loginIntent.putExtra(SentinelLogin.CANCEL_ALARM, true);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(loginIntent);

        new LogoutAsyncTask().execute(userCredentialsJson);
    }
}
