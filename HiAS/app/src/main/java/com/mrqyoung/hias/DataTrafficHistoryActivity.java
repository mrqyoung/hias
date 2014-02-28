package com.mrqyoung.hias;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by Mr.Q.Young on 13-12-30.
 */
public class DataTrafficHistoryActivity extends Activity {
    private Context mContext = DataTrafficHistoryActivity.this;
    private PowerManager.WakeLock wakeLock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_traffic_history);
        initViews();
    }


    private void initViews() {
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        ListView listView = (ListView) findViewById(R.id.listView);
        TextView emptyView = (TextView) findViewById(R.id.list_empty);
        String thd = RW.getTrafficHistoryData(mContext);
        if (thd != null) {
            emptyView.setText(android.text.Html.fromHtml(thd));
            listView.setEmptyView(emptyView);
        }

        final java.io.File f = new java.io.File("/sdcard/com.mrqyoung.hias");

        toggleButton.setChecked(f.exists());

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    acquireWakeLock();
                    delayToStartBroadcast();
                    mContext.deleteFile("dt");
                    f.mkdir();
                    System.out.println("START-Dump-Data-Traffic");
                } else {
                    releaseWakeLock();
                    cancelBroadcast(mContext);
                    f.delete();
                    System.out.println("STOP-Dump-Data-Traffic");
                }
            }
        });
    }


    private int _i = 5 * 60; // interval - seconds
    private void delayToStartBroadcast() {
        String interval = ((TextView) findViewById(R.id.et_dump_interval)).getText().toString();
        if (interval.isEmpty()) {
            Toast.makeText(mContext, "DEFAULT: 5min", 1).show();
        } else {
            _i = Integer.parseInt(interval);
            if (_i < 10) _i = 5 * 60;
            Toast.makeText(mContext, "Frequency(s): " + _i, 1).show();
        }
        new android.os.Handler().postDelayed(new Runnable(){
            public void run() {
                sendBroadcastRepeat(mContext, _i);
            }
        }, 30 * 1000);
    }


    private void sendBroadcastRepeat(Context c, int interval){
        if (!new java.io.File("/sdcard/com.mrqyoung.hias").exists()) return;
        Intent intent = new Intent(SrvDumpDataTraffic.ACTION_DUMP_DT);
        PendingIntent pi = PendingIntent.getService(c, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pi == null) pi = PendingIntent.getService(c, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval*1000, pi);
    }

    private void cancelBroadcast(Context c){
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SrvDumpDataTraffic.ACTION_DUMP_DT);
        PendingIntent pi = PendingIntent.getService(c, 0, intent, PendingIntent.FLAG_NO_CREATE);
        am.cancel(pi);
        pi.cancel();
    }


    private void acquireWakeLock() {
        if (wakeLock != null) return;
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "mrqyoung-walklosk");
        wakeLock.acquire();
        System.out.println("----SCREEN-WAKE-UP----");
    }

    private void releaseWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
            System.out.println("----WakeLock-OFF----");
        }
    }
}
