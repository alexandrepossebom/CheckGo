package com.possebom.checkgo.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.github.snowdream.android.util.Log;
import com.possebom.checkgo.R;
import com.possebom.checkgo.service.UpgradeService;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * Created by alexandre on 30/04/15.
 */
public class UpgradeCheck extends AsyncTask<Void, Void, Void> {
    private Context context;

    public UpgradeCheck(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {

        int remoteVersion;
        final int localVersion = getVersion();

        final OkHttpClient client = new OkHttpClient();
        final String url = "http://possebom.com/android/checkgo_version.txt";
        final Request request = new Request.Builder().url(url).build();
        try {
            final Response response = client.newCall(request).execute();
            final String result = response.body().string().trim();
            remoteVersion = Integer.parseInt(result);
        } catch (final Exception e) {
            remoteVersion = -1;
            Log.e("Error updating: " + e.getMessage());

            //TODO
            e.printStackTrace();
        }

        if (remoteVersion > localVersion) {
            Log.d("Update needed local is: " + localVersion + " remote is: " + remoteVersion);
            createNotification();
        } else {
            Log.d("NO update local is: " + localVersion + " remote is: " + remoteVersion);
        }

        return null;
    }

    private int getVersion() {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (final PackageManager.NameNotFoundException e) {
            return -1;
        }
    }


    public void createNotification() {
        final Intent intent = new Intent(context, UpgradeService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        final Notification notification = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.new_version_available))
                .setSmallIcon(R.drawable.ic_coins)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.stat_sys_download, context.getString(R.string.download), pendingIntent)
                .build();

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

}
