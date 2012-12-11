package com.sentinel.tracking;

import android.content.Context;

import java.io.*;

/**
 * David Russell
 * 11/12/12
 */
public class GISDataBuffer {

    private static final String FILE_NAME;

    static {
        FILE_NAME = "geodata.tmp";
    }

    public static String readJSONStringFromBuffer() {
        try {
            File oBufferedJSONFile = new File(FILE_NAME);
            InputStream oInputStream = new BufferedInputStream(new FileInputStream(oBufferedJSONFile));

            BufferedReader oReader = new BufferedReader(new InputStreamReader(oInputStream));

            StringBuilder oBufferedJSONString = new StringBuilder();
            String strLine;

            while ((strLine = oReader.readLine()) != null) {
                oBufferedJSONString.append(strLine);
            }

            oReader.close();
            oBufferedJSONFile.delete();

            return oBufferedJSONString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeJSONStringToBuffer(Context context, String strJSON) {
        try {
            FileOutputStream oOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            oOutputStream.write(strJSON.getBytes());
            oOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
