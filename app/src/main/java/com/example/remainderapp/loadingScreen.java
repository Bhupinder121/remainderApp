package com.example.remainderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.example.remainderapp.serverConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class loadingScreen extends AppCompatActivity {

    static serverConnection connection;
    public static ArrayList<JSONObject> tasks;
    public static int todayTasks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        getSupportActionBar().hide();
        connection = new serverConnection();
        connection.setup();
        getData(getApplicationContext(), new customCallback() {
            @Override
            public void Data(ArrayList<JSONObject> value, int TodayTasks) throws JSONException {
                tasks = value;
                todayTasks = TodayTasks;
                startActivity(new Intent(loadingScreen.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String error) {

            }
        });
    }
    public static void getData(Context context, customCallback callback){
        connection.getData("SELECT * From task_table", context, 0, new customCallback() {
            @Override
            public void Data(ArrayList<JSONObject> todayvalue, int todaySize) throws JSONException {
                connection.getData("SELECT * FROM notdonetask_table", context, 0, new customCallback() {
                    @Override
                    public void Data(ArrayList<JSONObject> notDonevalue, int notDoneSize) throws JSONException {
                        todayvalue.addAll(notDonevalue);
                        callback.Data(todayvalue, todaySize);
                    }

                    @Override
                    public void onError(String error) {
                        System.out.println(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}