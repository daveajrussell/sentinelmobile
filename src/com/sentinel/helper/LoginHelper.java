package com.sentinel.helper;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sentinel.models.User;
import com.sentinel.preferences.SentinelSharedPreferences;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * David Russell
 * 27/01/13
 */
public class LoginHelper
{
    private static Gson gson;
    private static String json;
    private static User user;

    private static HttpEntity httpEntity;
    private static InputStream inputStream;
    private static Reader reader;
    private static JsonParser jsonParser;
    private static JsonObject jsonObject;

    private static SentinelSharedPreferences sentinelSharedPreferences;

    public static void loginToSystem(HttpResponse httpResponse, Context context)
    {
        try
        {
            gson = new Gson();
            sentinelSharedPreferences = new SentinelSharedPreferences(context);

            httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
            reader = new InputStreamReader(inputStream);

            jsonParser = new JsonParser();
            json = gson.fromJson(reader, String.class);
            jsonObject = jsonParser.parse(json).getAsJsonObject();

            user = new User();
            user.setUserIdentification(gson.fromJson(jsonObject.get("UserKey"), String.class));
            user.setSessionID(gson.fromJson(jsonObject.get("SessionID"), int.class));

            sentinelSharedPreferences.setUserPreferences(user.getUserIdentification(), user.getSessionID());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
