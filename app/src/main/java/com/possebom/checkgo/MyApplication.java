package com.possebom.checkgo;

import android.app.Application;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.crashlytics.android.Crashlytics;
import com.github.snowdream.android.util.FilePathGenerator;
import com.github.snowdream.android.util.Log;
import com.possebom.checkgo.controller.CGController;
import com.possebom.checkgo.service.UpdateService;

/**
 * Created by alexandre on 06/02/15.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics.start(this);

        CGController.INSTANCE.restore(this);

        Log.setEnabled(true);
        Log.setLog2FileEnabled(true);
        Log.setLog2ConsoleEnabled(true);
        Log.setGlobalTag("CheckGo");
        Log.setFilePathGenerator(new FilePathGenerator.DefaultFilePathGenerator(Environment.getExternalStorageDirectory().getPath(), "CheckGo", ".log"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//            jobScheduler.cancelAll();
            if (jobScheduler.getAllPendingJobs().isEmpty()) {
                UpdateService.scheduleJob(this);
            }
        }

    }
}
