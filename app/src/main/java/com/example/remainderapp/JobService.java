package com.example.remainderapp;

import static com.example.remainderapp.MainActivity.isMyServiceRunning;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class JobService extends android.app.job.JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("ExampleJobService", "onStartJob: Job Started");
        ReStartService(params);
        return false;
    }

    private void ReStartService(JobParameters parameters){
        Context context = JobService.this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!isMyServiceRunning(ScreenOnOffManager.class)) {
                context.startForegroundService(new Intent(context, ScreenOnOffManager.class));
            }
        } else {
            if(!isMyServiceRunning(ScreenOnOffManager.class)) {
                context.startService(new Intent(context, ScreenOnOffManager.class));
            }
        }
        jobFinished(parameters, false);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
