package com.mrqyoung.hias;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.IBinder;
import android.os.PowerManager;

import java.sql.SQLOutput;

/**
 * Created by Mr.Q.Young on 14-2-18.
 */
public class SrvDumpDataTraffic extends IntentService {
    public static final String ACTION_DUMP_DT = "com.mrqyoung.hias.DUMP_DATA_TRAFFIC";

    public SrvDumpDataTraffic() {
        super(ACTION_DUMP_DT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!intent.getAction().equals(ACTION_DUMP_DT)) return;
        logFormatedDT();
    }


    private void logFormatedDT() {
        String strTxRx = new java.text.SimpleDateFormat("[yyyy/MM/dd HH:mm:ss] : ")
                .format(new java.util.Date(System.currentTimeMillis()));
        strTxRx += DataTrafficWidget.getAppTrafficData(this) + "<br />";
        System.out.println(strTxRx);
        RW.writeLineData(this, strTxRx);
    }

}
