package com.example.remainderapp;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class JobService extends JobIntentService {

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ScreenOnOffManager.class, 1, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }
}
