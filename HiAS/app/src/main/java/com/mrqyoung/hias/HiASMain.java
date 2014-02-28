package com.mrqyoung.hias;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



/**
 * Created by Mr.Q.Young on 13-5-17.
 */
public class HiASMain extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() == null) {
            _log("start an app details");
            showInstalledAppDetails(this, RW.getSavedPackage(HiASMain.this));
            setNotification(this);
            finish();
        } else {
            _log("settings");
        }

        setContentView(R.layout.main);
        initViews();
    }


    private void initViews() {
        Button btnSave = (Button) findViewById(R.id.btnSave);
        final EditText etPackageName = (EditText) findViewById(R.id.etPackage);

        etPackageName.setHint(RW.getSavedPackage(HiASMain.this));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPackageName = etPackageName.getText().toString();
                if (strPackageName.trim().equalsIgnoreCase("")) {return;}
                if (strPackageName.startsWith("*.")) {
                    String cmd = "rm /mnt/sdcard/" + strPackageName;
                    //_log("clear: " + strPackageName);
                    Toast.makeText(HiASMain.this, strPackageName + exec(cmd), 1).show();
                    etPackageName.setText("");
                    return;
                }
                showInstalledAppDetails(getApplicationContext(), strPackageName);
                RW.savePackageName(HiASMain.this, strPackageName);
                finish();
            }
        });

        ((TextView) findViewById(R.id.tvViewDT)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _log("View Data transmission");
                HiASMain.this.startActivity(new Intent(HiASMain.this, DataTrafficHistoryActivity.class));
            }
        });

        ((TextView) findViewById(R.id.tvDelTmp)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _log("delete *.tmp");
                Toast.makeText(HiASMain.this, getString(R.string.c_tmp_file) + exec("rm /sdcard/*.tmp"), 1).show();
            }
        });
    }


    private void setNotification(Context context) {
        final NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_launcher, "Mr.Q.Young--20130518", System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(context, HiASMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("notify","");
        //PendingIntent
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                R.string.app_name,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setLatestEventInfo(
                context,
                getText(R.string.notify_title),
                getText(R.string.notify_tips),
                contentIntent);
        nm.notify(R.string.app_name, notification);
        //cancelNotify(nm);
        new Handler().postDelayed(new Runnable(){
            public void run() {
                nm.cancel(R.string.app_name);
            }
        }, 30 * 1000);
    }


    private void _log(String strArg0) {
        System.out.println(strArg0);
        return;
    }


    public static void showInstalledAppDetails(Context context, String packageName) {
        final String SCHEME = "package";
        final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
        final String APP_PKG_NAME_22 = "pkg";
        final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
        final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final int apiLevel = android.os.Build.VERSION.SDK_INT;
        if (apiLevel >= 9) {
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else {
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }

    private String exec(String cmd) {
        String cmds[] = {"sh", "-c", cmd};
        try { Runtime.getRuntime().exec(cmds); _log("in try: " +cmd); return " cleared";}
        catch (java.io.IOException e) { e.printStackTrace();}
        return " failed to clear";
    }


} //EOF
