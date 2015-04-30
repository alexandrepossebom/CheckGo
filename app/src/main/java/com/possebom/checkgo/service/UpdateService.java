package com.possebom.checkgo.service;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.github.snowdream.android.util.Log;
import com.possebom.checkgo.util.UpdateCards;
import com.possebom.checkgo.util.UpgradeCheck;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by alexandre on 06/02/15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class UpdateService extends JobService implements UpdateCards.UpdateInterface {

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i("onStartJob");

        if (!UpdateCards.isUpdated()) {
            Log.d("Need Update !!!");
            UpdateCards.start(this, this);
            new UpgradeCheck(getApplication().getApplicationContext()).execute();
        }
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i("onStopJob");
        return false;
    }

    public static void scheduleJob(final Context context) {
        scheduleJob(context, true);
    }

    private static void scheduleJob(final Context context, final boolean isOk) {
        final JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        final Calendar calendarNextRun = Calendar.getInstance();

        if (!isOk) {
            calendarNextRun.add(Calendar.HOUR_OF_DAY, 1);
        } else {
            final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            calendarNextRun.set(Calendar.MINUTE, 30);
            calendarNextRun.set(Calendar.SECOND, 0);
            if (hour < 12) {
                calendarNextRun.set(Calendar.HOUR_OF_DAY, 13);
            } else {
                calendarNextRun.set(Calendar.HOUR_OF_DAY, 6);
                calendarNextRun.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        final long nextRun = calendarNextRun.getTimeInMillis() - System.currentTimeMillis();
        final long deadLine = nextRun + TimeUnit.HOURS.toMillis(5);

        final JobInfo.Builder builder = new JobInfo.Builder(0, new ComponentName(context, UpdateService.class));
        builder.setMinimumLatency(nextRun);
        builder.setOverrideDeadline(deadLine);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPersisted(true);

        jobScheduler.schedule(builder.build());
        Log.d("Job Scheduled! to: " + calendarNextRun.getTime());
    }

    @Override
    public void updateSuccess() {
        scheduleJob(this);
    }

    @Override
    public void updateError() {
        scheduleJob(this, false);
    }
}
