package com.example.remainderapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ScreenOnOffManager screenOnOffManager;
    Intent service;
    static ActivityManager manager;
    serverConnection connection;
    ListAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPhonePermission();

        listView = findViewById(R.id.list);
        listView.setVerticalScrollBarEnabled(false);

        manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        screenOnOffManager = new ScreenOnOffManager();
        service = new Intent(this, ScreenOnOffManager.class);
        connection = new serverConnection();
        connection.setup();

        if(!isMyServiceRunning(screenOnOffManager.getClass())){
            startService(service);
        }

        getData(new customCallback() {
            @Override
            public void StringData(String value) {
                System.out.println(value);
            }

            @Override
            public void JsonData(ArrayList<JSONObject> jsonObjects) {
                adapter = new customAdapter(MainActivity.this, R.layout.remaider_task, jsonObjects);
                listView.setAdapter(adapter);
            }
        });
    }


    private void getData(customCallback callback){
        connection.getData("SELECT * From task_table", MainActivity.this, new customCallback() {
            @Override
            public void StringData(String value) {
                System.out.println(value);
            }

            @Override
            public void JsonData(ArrayList<JSONObject> jsonObjects) {
                callback.JsonData(jsonObjects);
            }
        });
    }

    private void checkPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED}, 1);
        }
    }


    public static boolean isMyServiceRunning(Class<?> serviceClass) {
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