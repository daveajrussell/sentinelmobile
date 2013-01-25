package com.sentinel.tracking;

import android.content.Context;

import java.io.*;

/**
 * David Russell
 * 11/12/12
 */
public class GISDataBuffer
{

    private static final String FILE_NAME;

    static
    {
        FILE_NAME = "geodata.txt";
    }

    public static String readJSONStringFromBuffer(Context context)
    {
        try
        {
            FileInputStream oInputStream = context.openFileInput(FILE_NAME);
            InputStreamReader oInputStreamReader = new InputStreamReader(oInputStream);
            BufferedReader oBufferedReader = new BufferedReader(oInputStreamReader);

            StringBuilder oBufferedJSONString = new StringBuilder();
            String strLine;

            while ((strLine = oBufferedReader.readLine()) != null)
            {
                oBufferedJSONString.append(strLine);
            }

            oBufferedReader.close();

            writeNull(context);

            return oBufferedJSONString.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeJSONStringToBuffer(Context context, String strJSON)
    {
        try
        {
            FileOutputStream oOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            oOutputStream.write(strJSON.getBytes());
            oOutputStream.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void writeNull(Context context)
    {
        try
        {
            FileOutputStream oOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            oOutputStream.write(null);
            oOutputStream.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
