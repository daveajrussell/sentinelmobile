package com.sentinel.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static String getFormattedHrsMinsSecsTimeString(final long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(cal.getTime());
    }

    public static String getFormattedMinsSecsTimeString(final long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        SimpleDateFormat simgleDateFormat = new SimpleDateFormat("mm:ss");
        return simgleDateFormat.format(cal.getTime());
    }
}
