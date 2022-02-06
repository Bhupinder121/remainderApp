package com.example.remainderapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    ScreenOnOffManager screenOnOffManager;
    Intent service;
    static ActivityManager manager;
    public static serverConnection connection;
    customAdapter adapter;
    ListView listView;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    ArrayList<JSONObject> tasks;
    int todayTasks;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPhonePermission();

        listView = findViewById(R.id.list);
        listView.setVerticalScrollBarEnabled(false);
        editText = findViewById(R.id.editTask);

        manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        screenOnOffManager = new ScreenOnOffManager();
        service = new Intent(this, ScreenOnOffManager.class);
        connection = new serverConnection();
        connection.setup();

        getData((value, TodayTasks) -> {
            tasks = value;
            todayTasks = TodayTasks;
            adapter = new customAdapter(MainActivity.this, R.layout.remaider_task, tasks, todayTasks);
            listView.setAdapter(adapter);
        });

        if(!isMyServiceRunning(screenOnOffManager.getClass())){
            startService(service);
        }

        dateSetListener = (view, year, month, dayOfMonth) -> {
            String todo = editText.getText().toString();
            JSONObject obj = new JSONObject();
            try {
                obj.put("task", todo);
                String source = year + "-" + (month + 1) + "-" + dayOfMonth;
                obj.put("date", source);
                connection.sendData(obj);
                obj.put("taskName", todo);
                obj.put("taskAddDate", dayOfMonth+"/"+(month+1)+"/"+year+", 12:00:00 am");
                obj.put("isDone", 0);
                obj.put("isNotDone", 0);
                obj.put("taskID", todayTasks+1);
                obj.put("isPermanent", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tasks.add(todayTasks, obj);
            todayTasks++;
            adapter.updateData(tasks, todayTasks);
            editText.setText("");
        };

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    selectDate();
                }
                return false;
            }
        });

    }


    private void selectDate(){
        Calendar currentTime = Calendar.getInstance();
        int currentYear = currentTime.get(Calendar.YEAR);
        int currentMonth = currentTime.get(Calendar.MONTH);
        int currentDate = currentTime.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(
                MainActivity.this,
                android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth,
                dateSetListener,
                currentYear,currentMonth,currentDate);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void getData(customCallback callback){
        connection.getData("SELECT * From task_table", MainActivity.this, (todayvalue, todaySize) -> connection.getData("SELECT * FROM notdonetask_table", MainActivity.this, (notDonevalue, notDoneSize) -> {
            todayvalue.addAll(notDonevalue);
            callback.Data(todayvalue, todaySize);
        }));
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