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

/**
 * David Russell
 * 22/01/13
 */
public class SentinelDB {
    public static final String KEY_ID = "_id";
    public static final String KEY_SESSION_ID_COLUMN = "SESSION_ID_COLUMN";
    public static final String KEY_USER_IDENTITY_COLUMN = "USER_IDENTITY_COLUMN";
    public static final String KEY_TIMESTAMP_COLUMN = "TIMESTAMP_COLUMN";
    public static final String KEY_LATITUDE_COLUMN = "LATITUDE_COLUMN";
    public static final String KEY_LONGITUDE_COLUMN = "LONGITUDE_COLUMN";
    public static final String KEY_ORIENTATION_COLUMN = "ORIENTATION_COLUMN";
    public static final String KEY_SPEED_COLUMN = "SPEED_COLUMN";
    private static final String DATABASE_NAME = "SentinelDB.db";
    private static final String DATABASE_TABLE = "SentinelDB";
    private static final int DATABASE_VERSION = 3;

    private SentinelDBOpenHelper mSentinelDBOpenHelper;

    public SentinelDB(Context context) {
        mSentinelDBOpenHelper = new SentinelDBOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void closeSentinelDatabase() {
        mSentinelDBOpenHelper.close();
    }

    private Cursor getBufferedSentinelGeospatialDataCursor() {
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

        Cursor cursor = null;

        try {
            SQLiteDatabase sentinelDB = mSentinelDBOpenHelper.getReadableDatabase();

            cursor = sentinelDB.query(DATABASE_TABLE,
                    result_columns, where, whereArgs, groupBy, having, order);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return cursor;
    }

    public int getRowCount() {
        Cursor cursor = null;
        int count = 0;

        try {
            String query = "SELECT * FROM " + DATABASE_TABLE;
            SQLiteDatabase sentinelDB = mSentinelDBOpenHelper.getReadableDatabase();
            cursor = sentinelDB.rawQuery(query, null);
            count = cursor.getCount();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            cursor.close();
        }

        return count;
    }

    public String getBufferedGeospatialDataJsonString() {
        Cursor cursor = getBufferedSentinelGeospatialDataCursor();

        JSONObject oBufferedDataJson;
        JSONArray oBufferedDataJsonArray;

        try {

            if (cursor.getCount() == 1) {
                cursor.moveToFirst();

                return new Gson().toJson(getJSONObjectFromCursor(cursor).toString());

            } else if (cursor.getCount() > 1) {
                oBufferedDataJsonArray = new JSONArray();
                oBufferedDataJson = new JSONObject();

                while (cursor.moveToNext()) {
                    oBufferedDataJsonArray.put(getJSONObjectFromCursor(cursor));
                }

                oBufferedDataJson.put("BufferedData", oBufferedDataJsonArray);
                return new Gson().toJson(oBufferedDataJson.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            cursor.close();
        }
        return null;
    }

    private JSONObject getJSONObjectFromCursor(final Cursor cursor) {

        int SESSION_ID_COLUMN_INDEX = cursor.getColumnIndex(KEY_SESSION_ID_COLUMN);
        int USER_IDENTIFICATION_COLUMN_INDEX = cursor.getColumnIndex(KEY_USER_IDENTITY_COLUMN);
        int TIMESTAMP_COLUMN_INDEX = cursor.getColumnIndex(KEY_TIMESTAMP_COLUMN);
        int LATITUDE_COLUMN_INDEX = cursor.getColumnIndex(KEY_LATITUDE_COLUMN);
        int LONGITUDE_COLUMN_INDEX = cursor.getColumnIndex(KEY_LONGITUDE_COLUMN);
        int SPEED_COLUMN_INDEX = cursor.getColumnIndex(KEY_SPEED_COLUMN);
        int ORIENTATION_COLUMN_INDEX = cursor.getColumnIndex(KEY_ORIENTATION_COLUMN);

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject()
                    .put("iSessionID", cursor.getInt(SESSION_ID_COLUMN_INDEX))
                    .put("oUserIdentification", cursor.getString(USER_IDENTIFICATION_COLUMN_INDEX))
                    .put("lTimeStamp", cursor.getLong(TIMESTAMP_COLUMN_INDEX))
                    .put("dLatitude", cursor.getDouble(LATITUDE_COLUMN_INDEX))
                    .put("dLongitude", cursor.getDouble(LONGITUDE_COLUMN_INDEX))
                    .put("dSpeed", cursor.getDouble(SPEED_COLUMN_INDEX))
                    .put("iOrientation", cursor.getInt(ORIENTATION_COLUMN_INDEX));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonObject;
    }

    public void addGeospatialData(GeospatialInformation oGeoInformation) {
        ContentValues newGeospatialValues = new ContentValues();

        newGeospatialValues.put(KEY_SESSION_ID_COLUMN, oGeoInformation.getSessionID());
        newGeospatialValues.put(KEY_USER_IDENTITY_COLUMN, oGeoInformation.getUserIndentification());
        newGeospatialValues.put(KEY_TIMESTAMP_COLUMN, oGeoInformation.getDateTimeStamp());
        newGeospatialValues.put(KEY_LATITUDE_COLUMN, oGeoInformation.getLatitude());
        newGeospatialValues.put(KEY_LONGITUDE_COLUMN, oGeoInformation.getLongitude());
        newGeospatialValues.put(KEY_ORIENTATION_COLUMN, oGeoInformation.getOrientation());
        newGeospatialValues.put(KEY_SPEED_COLUMN, oGeoInformation.getSpeed());

        SQLiteDatabase oSentinelDB = mSentinelDBOpenHelper.getWritableDatabase();

        oSentinelDB.insert(DATABASE_TABLE, null, newGeospatialValues);
        oSentinelDB.close();
    }

    public void deleteGeospatialData() {
        SQLiteDatabase oSentinelDB = mSentinelDBOpenHelper.getWritableDatabase();
        oSentinelDB.delete("SentinelDB", null, null);
        oSentinelDB.close();
    }

    private static class SentinelDBOpenHelper extends SQLiteOpenHelper {
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

        public SentinelDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(sqLiteDatabase);
        }
    }
}
