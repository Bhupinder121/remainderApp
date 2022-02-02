package com.example.remainderapp;

import static com.example.remainderapp.MainActivity.isMyServiceRunning;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class bootReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            System.out.println("booted");
            JobService.enqueueWork(context, new Intent());
//            context.startService(new Intent(context, ScreenOnOffManager.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}
