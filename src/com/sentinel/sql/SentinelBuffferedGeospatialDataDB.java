package com.sentinel.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.gson.Gson;
import com.sentinel.models.GeospatialInformation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * David Russell
 * 22/01/13
 */
public class SentinelBuffferedGeospatialDataDB
{
    public static final String KEY_ID = "_id";
    public static final String KEY_SESSION_ID_COLUMN = "SESSION_ID_COLUMN";
    public static final String KEY_USER_IDENTITY_COLUMN = "USER_IDENTITY_COLUMN";
    public static final String KEY_TIMESTAMP_COLUMN = "TIMESTAMP_COLUMN";
    public static final String KEY_LATITUDE_COLUMN = "LATITUDE_COLUMN";
    public static final String KEY_LONGITUDE_COLUMN = "LONGITUDE_COLUMN";
    public static final String KEY_ORIENTATION_COLUMN = "ORIENTATION_COLUMN";
    public static final String KEY_SPEED_COLUMN = "SPEED_COLUMN";
    private SentinelDBOpenHelper sentinelDBOpenHelper;
    private Gson oGson;

    public SentinelBuffferedGeospatialDataDB(Context context)
    {
        sentinelDBOpenHelper = new SentinelDBOpenHelper(context, SentinelDBOpenHelper.DATABASE_NAME, null, SentinelDBOpenHelper.DATABASE_VERSION);
    }

    public void closeSentinelDatabase()
    {
        sentinelDBOpenHelper.close();
    }

    private Cursor getBufferedSentinelGeospatialDataCursor()
    {
        String[] result_columns = new String[]{
                KEY_ID,
                KEY_SESSION_ID_COLUMN,
                KEY_USER_IDENTITY_COLUMN,
                KEY_TIMESTAMP_COLUMN,
                KEY_LATITUDE_COLUMN,
                KEY_LONGITUDE_COLUMN,
                KEY_ORIENTATION_COLUMN,
                KEY_SPEED_COLUMN
        };

        String where = null;
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;

        Cursor oCursor = null;

        try
        {
            SQLiteDatabase oSentinelDB = sentinelDBOpenHelper.getWritableDatabase();
            oCursor = oSentinelDB.query(SentinelDBOpenHelper.DATABASE_TABLE,
                    result_columns, where, whereArgs, groupBy, having, order);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return oCursor;
    }

    public int getBufferedGeospatialDataCount()
    {
        Cursor oCursor = getBufferedSentinelGeospatialDataCursor();
        return oCursor.getCount();
    }

    public String getBufferedGeospatialDataJsonString()
    {
        oGson = new Gson();
        Cursor oCursor = getBufferedSentinelGeospatialDataCursor();

        int SESSION_ID_COLUMN_INDEX = oCursor.getColumnIndex(KEY_SESSION_ID_COLUMN);
        int USER_IDENTIFICATION_COLUMN_INDEX = oCursor.getColumnIndex(KEY_USER_IDENTITY_COLUMN);
        int TIMESTAMP_COLUMN_INDEX = oCursor.getColumnIndex(KEY_TIMESTAMP_COLUMN);
        int LATITUDE_COLUMN_INDEX = oCursor.getColumnIndex(KEY_LATITUDE_COLUMN);
        int LONGITUDE_COLUMN_INDEX = oCursor.getColumnIndex(KEY_LONGITUDE_COLUMN);
        int SPEED_COLUMN_INDEX = oCursor.getColumnIndex(KEY_SPEED_COLUMN);
        int ORIENTATION_COLUMN_INDEX = oCursor.getColumnIndex(KEY_ORIENTATION_COLUMN);

        String strJsonString = "";

        JSONStringer strBufferedData;
        JSONObject oBufferedDataJson;
        JSONObject oBufferedDataJsonElement;
        JSONArray oBufferedDataJsonArray;

        if (oCursor.getCount() == 1)
        {
            oCursor.moveToFirst();
            try
            {
                strBufferedData = new JSONStringer()
                        .object()
                        .key("iSessionID").value(oCursor.getInt(SESSION_ID_COLUMN_INDEX))
                        .key("oUserIdentification").value(oCursor.getString(USER_IDENTIFICATION_COLUMN_INDEX))
                        .key("lTimeStamp").value(oCursor.getLong(TIMESTAMP_COLUMN_INDEX))
                        .key("dLatitude").value(oCursor.getDouble(LATITUDE_COLUMN_INDEX))
                        .key("dLongitude").value(oCursor.getDouble(LONGITUDE_COLUMN_INDEX))
                        .key("dSpeed").value(oCursor.getDouble(SPEED_COLUMN_INDEX))
                        .key("iOrientation").value(oCursor.getInt(ORIENTATION_COLUMN_INDEX))
                        .endObject();

                strJsonString = oGson.toJson(strBufferedData.toString());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        } else
            if (oCursor.getCount() > 1)
            {
                try
                {
                    oBufferedDataJsonArray = new JSONArray();
                    oBufferedDataJson = new JSONObject();

                    while (oCursor.moveToNext())
                    {
                        oBufferedDataJsonElement = new JSONObject()
                                .put("iSessionID", oCursor.getInt(SESSION_ID_COLUMN_INDEX))
                                .put("oUserIdentification", oCursor.getString(USER_IDENTIFICATION_COLUMN_INDEX))
                                .put("lTimeStamp", oCursor.getLong(TIMESTAMP_COLUMN_INDEX))
                                .put("dLatitude", oCursor.getDouble(LATITUDE_COLUMN_INDEX))
                                .put("dLongitude", oCursor.getDouble(LONGITUDE_COLUMN_INDEX))
                                .put("dSpeed", oCursor.getDouble(SPEED_COLUMN_INDEX))
                                .put("iOrientation", oCursor.getInt(ORIENTATION_COLUMN_INDEX));
                        oBufferedDataJsonArray.put(oBufferedDataJsonElement);
                    }

                    oBufferedDataJson.put("BufferedData", oBufferedDataJsonArray);

                    strJsonString = oGson.toJson(oBufferedDataJson.toString());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            } else
            {
                // no collection
            }

        oCursor.close();
        return strJsonString;
    }

    public void addGeospatialData(GeospatialInformation oGeoInformation)
    {
        ContentValues newGeospatialValues = new ContentValues();

        newGeospatialValues.put(KEY_SESSION_ID_COLUMN, oGeoInformation.getSessionID());
        newGeospatialValues.put(KEY_USER_IDENTITY_COLUMN, oGeoInformation.getUserIndentification());
        newGeospatialValues.put(KEY_TIMESTAMP_COLUMN, oGeoInformation.getDateTimeStamp());
        newGeospatialValues.put(KEY_LATITUDE_COLUMN, oGeoInformation.getLatitude());
        newGeospatialValues.put(KEY_LONGITUDE_COLUMN, oGeoInformation.getLongitude());
        newGeospatialValues.put(KEY_ORIENTATION_COLUMN, oGeoInformation.getOrientation());
        newGeospatialValues.put(KEY_SPEED_COLUMN, oGeoInformation.getSpeed());

        SQLiteDatabase oSentinelDB = sentinelDBOpenHelper.getWritableDatabase();
        oSentinelDB.insert(SentinelDBOpenHelper.DATABASE_TABLE, null, newGeospatialValues);
    }

    public void deleteGeospatialData()
    {
        SQLiteDatabase oSentinelDB = sentinelDBOpenHelper.getWritableDatabase();
        oSentinelDB.delete(SentinelDBOpenHelper.DATABASE_TABLE, null, null);
    }

    private static class SentinelDBOpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "SentinelDB.db";
        private static final String DATABASE_TABLE = "SentinelDB";
        private static final int DATABASE_VERSION = 3;
        private static final String DATABASE_CREATE =
                "create table " + DATABASE_TABLE + " (" +
                        KEY_ID + " integer primary key autoincrement, " +
                        KEY_SESSION_ID_COLUMN + " integer not null," +
                        KEY_USER_IDENTITY_COLUMN + " text not null," +
                        KEY_TIMESTAMP_COLUMN + " float not null," +
                        KEY_LONGITUDE_COLUMN + " real not null," +
                        KEY_LATITUDE_COLUMN + " real not null," +
                        KEY_ORIENTATION_COLUMN + " integer not null," +
                        KEY_SPEED_COLUMN + " real not null);";

        public SentinelDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            sqLiteDatabase.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2)
        {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(sqLiteDatabase);
        }
    }
}
