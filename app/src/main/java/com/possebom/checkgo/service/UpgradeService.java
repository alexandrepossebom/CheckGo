package com.possebom.checkgo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.github.snowdream.android.util.Log;
import com.possebom.checkgo.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpgradeService extends Service {

    private File file;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private final static int NOT_DOWNLOAD = 1;
    private final static int NOT_INSTALL = 2;

    @Override
    public IBinder onBind(final Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d("onStartCommand");
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.downloading_app))
                .setContentText(getString(R.string.downloading))
                .setSmallIcon(android.R.drawable.stat_sys_download);

        new Downloader().execute();
        return START_NOT_STICKY;
    }

    private class Downloader extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.setProgress(100, 0, false);
            notificationManager.notify(NOT_DOWNLOAD, builder.build());
            notificationManager.cancel(0);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            builder.setProgress(100, values[0], false);
            notificationManager.notify(NOT_DOWNLOAD, builder.build());
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/checkgo.apk");

                final URL url = new URL("http://possebom.com/android/checkgo.apk");
                final URLConnection connection = url.openConnection();
                connection.connect();
                final int fileLength = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(file.getPath());

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            final Context context = UpgradeService.this;
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            final Notification notification = new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.click_to_install))
                    .setSmallIcon(R.drawable.ic_coins)
                    .setContentIntent(pendingIntent)
                    .build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.cancel(NOT_DOWNLOAD);
            notificationManager.notify(NOT_INSTALL, notification);
        }
    }
}
