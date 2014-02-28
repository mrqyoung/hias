package com.mrqyoung.hias;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class DataTrafficWidget extends AppWidgetProvider {
    private static BitmapDrawable app_icon = null;
    private static String WIDGET_REFRESH = "com.mrqyoung.hias.widgetRefresh";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.view_data_transmission);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.data_traffic_widget);
        views.setTextViewText(R.id.textView, widgetText);

        //Button-refresh
        Intent btnRefreshIntent = new Intent(WIDGET_REFRESH);
        btnRefreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingRefresh= PendingIntent.getBroadcast(context, appWidgetId, btnRefreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btnRefresh, pendingRefresh);
        int tvId = isWIFI(context) ? R.id.tvWifi : R.id.tvNet;
        views.setTextViewText(tvId, android.text.Html.fromHtml(getAppTrafficData(context)));
        if (app_icon != null) views.setImageViewBitmap(R.id.app_icon, app_icon.getBitmap());

        //Widget-history
        Intent intent = new Intent(context, DataTrafficHistoryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.layoutWidget, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WIDGET_REFRESH)) {
            int mWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            updateAppWidget(context, AppWidgetManager.getInstance(context), mWidgetId);
        }
        super.onReceive(context, intent);
    }


    public static String getAppTrafficData(Context context) {
        PackageManager pm= context.getPackageManager();
        PackageInfo pkgInfo = null;
        String result = context.getString(R.string.err_pkg_not_found);
        String pkgName = RW.getSavedPackage(context);
        try { pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
        } catch (Exception e) { e.printStackTrace();}
        if (pkgInfo == null) return result;

        //Get app UID, Rx or Tx -1 means not supported (2.2 or later)
        int uId = pkgInfo.applicationInfo.uid;
        long TxRx[] = {-1, -1};
        TxRx[0] = TrafficStats.getUidTxBytes(uId);
        TxRx[1] = TrafficStats.getUidRxBytes(uId);

        if (app_icon == null) app_icon = (BitmapDrawable)pkgInfo.applicationInfo.loadIcon(pm);

        return (TxRx[0] < 0 || TxRx[1] < 0) ?
                context.getString(R.string.data_default) : RW.readAndSaveLastDT(context, TxRx);
    }

    private static boolean isWIFI(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        return (networkInfo == null || networkInfo.getType() == 1);
    }
}

