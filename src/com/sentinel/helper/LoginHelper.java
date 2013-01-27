package com.sentinel.helper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sentinel.models.User;
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

    public static void loginToSystem(HttpResponse oResponse)
    {
        try
        {
            gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject oUserJsonObject;

            HttpEntity oLoginServiceEntity = oResponse.getEntity();
            InputStream oLoginServiceResponseStream = oLoginServiceEntity.getContent();
            Reader oLoginServiceReader = new InputStreamReader(oLoginServiceResponseStream);

            json = gson.fromJson(oLoginServiceReader, String.class);
            oUserJsonObject = parser.parse(json).getAsJsonObject();

            user = new User();
            user.setUserIdentification(gson.fromJson(oUserJsonObject.get("UserKey"), String.class));
            user.setSessionID(gson.fromJson(oUserJsonObject.get("SessionID"), int.class));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
