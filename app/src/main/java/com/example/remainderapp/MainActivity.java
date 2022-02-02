package com.example.remainderapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    ScreenOnOffManager screenOnOffManager;
    Intent service;
    static ActivityManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        screenOnOffManager = new ScreenOnOffManager();
        service = new Intent(this, ScreenOnOffManager.class);
        startService(service);
//        if(!isMyServiceRunning(screenOnOffManager.getClass())){
//            startService(service);
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        System.out.println(serviceClass.getName());
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }


    @Override
    protected void onDestroy() {
        //stopService(mServiceIntent);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

}