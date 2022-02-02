package com.example.remainderapp;

import static com.example.remainderapp.MainActivity.isMyServiceRunning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class restarter extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!isMyServiceRunning(ScreenOnOffManager.class)) {
                context.startForegroundService(new Intent(context, ScreenOnOffManager.class));
            }
        } else {
            if(!isMyServiceRunning(ScreenOnOffManager.class)) {
                context.startService(new Intent(context, ScreenOnOffManager.class));
            }
        }

    }
}
