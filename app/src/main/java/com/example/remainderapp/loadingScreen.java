package com.example.remainderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.example.remainderapp.serverConnection;

import org.json.JSONObject;

import java.util.ArrayList;

public class loadingScreen extends AppCompatActivity {

    serverConnection connection;
    public static ArrayList<JSONObject> tasks;
    public static int todayTasks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        getSupportActionBar().hide();
        connection = new serverConnection();
        connection.setup();
        getData(getApplicationContext(), (value, TodayTasks) -> {
            tasks = value;
            todayTasks = TodayTasks;
            startActivity(new Intent(loadingScreen.this, MainActivity.class));
            finish();
        });
    }
    public void getData(Context context, customCallback callback){
        connection.getData("SELECT * From task_table", context, 0, (todayvalue, todaySize) -> connection.getData("SELECT * FROM notdonetask_table", context, 0, (notDonevalue, notDoneSize) -> {
            todayvalue.addAll(notDonevalue);
            System.out.println(todayvalue);
            callback.Data(todayvalue, todaySize);
        }));
    }
}