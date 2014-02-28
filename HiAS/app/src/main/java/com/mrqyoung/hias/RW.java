package com.mrqyoung.hias;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Formatter;

import java.io.FileInputStream;
import java.io.FileOutputStream;


/**
 * Created by Mr.Q.Young on 13-12-31.
 *
 * 1. get package name from SharedPreferences
 * 2. save current data traffic
 * 3. read data traffic
 * 4. SQLite
 * 4. save package name
 */
public class RW {
    public static String getSavedPackage(Context c) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(c);
        return preference.getString("Package","com.aspire.mm");
    }

    public static void savePackageName(Context c, String pkg) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(c);
        preference.edit().putString("Package", pkg).commit();
    }

    public static String readAndSaveLastDT(Context c, long[] TxRx) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(c);
        long TxRx_old[] = getLastDT(preference);
        long TxRx_delta[] = {TxRx[0] - TxRx_old[0], TxRx[1] - TxRx_old[1]};
        String format_row = c.getString(R.string.format_traffic_data);
        // save
        SharedPreferences.Editor editor = preference.edit();
        editor.putLong("DT_Tx", TxRx[0]);
        editor.putLong("DT_Rx", TxRx[1]);
        editor.commit();

        return (TxRx_delta[0] < 0 || TxRx_delta[1] < 0)
                ? String.format(format_row, Formatter.formatFileSize(c, TxRx[0]), Formatter.formatFileSize(c, TxRx[1]))
                : String.format(format_row, Formatter.formatFileSize(c, TxRx_delta[0]), Formatter.formatFileSize(c, TxRx_delta[1]));
    }

    private static long[] getLastDT(SharedPreferences preference) {
        long TxRx[] = {0, 0};
        TxRx[0] = preference.getLong("DT_Tx", 0);
        TxRx[1] = preference.getLong("DT_Rx", 0);
        return TxRx;
    }


    public static void writeLineData(Context c, String strData) {
        try {
            FileOutputStream fout = c.openFileOutput("dt", Context.MODE_APPEND);
            byte[]  bytes = strData.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) { System.out.println("Error on write data"); }
    }

    public static String getTrafficHistoryData(Context c) {
        String result = null;
        try {
            FileInputStream fin = c.openFileInput("dt");
            byte[] buffer = new byte[fin.available()];
            fin.read(buffer);
            fin.close();
            result = new String(buffer);
        } catch (Exception e) { System.out.println("Error on read data");}
        return result;
    }

}
