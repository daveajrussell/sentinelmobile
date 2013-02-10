package com.sentinel.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sentinel.app.R;
import com.sentinel.authentication.LogoutAsyncTask;
import com.sentinel.authentication.SentinelLogin;
import com.sentinel.models.User;
import com.sentinel.preferences.SentinelSharedPreferences;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class AuthenticationHelper {

    public static void performLogin(final HttpResponse httpResponse, final Context context) {
        try {
            SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);

            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream inputStream = httpEntity.getContent();
            Reader reader = new InputStreamReader(inputStream);

            JsonParser jsonParser = new JsonParser();
            String json = new Gson().fromJson(reader, String.class);
            JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();

            User user = new User();
            user.setUserIdentification(new Gson().fromJson(jsonObject.get("UserKey"), String.class));
            user.setSessionID(new Gson().fromJson(jsonObject.get("SessionID"), int.class));

            sentinelSharedPreferences.setUserPreferences(user.getUserIdentification(), user.getSessionID());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void performLogoutWithDialog(final Context context) {

        final SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);

        long lngShiftEndTimeDifference = sentinelSharedPreferences.getDrivingEndAlarm() - System.currentTimeMillis();
        String message;

        String time = Utils.getFormattedHrsMinsSecsTimeString(lngShiftEndTimeDifference);
        message = String.format("You still have %1$s of your shift remaining. Are you sure you wish to logout?", time);

        new AlertDialog.Builder(context)
                .setTitle(R.string.alert_title)
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout(context);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
