package com.example.remainderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.Button;
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

import static com.example.remainderapp.loadingScreen.tasks;
import static com.example.remainderapp.loadingScreen.todayTasks;

public class MainActivity extends AppCompatActivity {
    ScreenOnOffManager screenOnOffManager;
    Intent service;
    static ActivityManager manager;
    public static serverConnection connection;
    customAdapter adapter;
    ListView listView;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private static View addBookPopupView;
    private static View addQuotePopupView;
    private static AlertDialog dialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static AlertDialog.Builder dialogBuilder;


    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPhonePermission();

        listView = findViewById(R.id.list);
        listView.setVerticalScrollBarEnabled(false);
        editText = findViewById(R.id.editTask);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        dialogBuilder = new AlertDialog.Builder(this);
        addBookPopupView = getLayoutInflater().inflate(R.layout.add_book, null);
        addQuotePopupView = getLayoutInflater().inflate(R.layout.add_quote,null);

        manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        screenOnOffManager = new ScreenOnOffManager();
        service = new Intent(this, ScreenOnOffManager.class);
        connection = new serverConnection();
        connection.setup();

        adapter = new customAdapter(MainActivity.this, R.layout.remaider_task, tasks, todayTasks);
        listView.setAdapter(adapter);

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

        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                selectDate();
            }
            return false;
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            System.out.println("REFRESH");
            getData(MainActivity.this, new customCallback() {
                @Override
                public void Data(ArrayList<JSONObject> value, int arraySize) throws JSONException {
                    adapter.updateData(value, arraySize);
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.first_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.quotebutton) {
            connection.getData("SELECT * FROM quotes_table ORDER BY RAND() LIMIT 1", getApplicationContext(), new customCallback() {
                @Override
                public void Data(ArrayList<JSONObject> value, int arraySize) throws JSONException {
                    String quote = value.get(0).getString("quoteName");
                    ScreenOnOffManager.sendNotification(MainActivity.this, "Quote of the day",quote);
                }
            });
        }
        else if (id == R.id.add_book) {
            add_book_dialog();
        }
        else if(id== R.id.add_quote){
            add_quote_dialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void add_quote_dialog(){
        if (addQuotePopupView.getParent() != null) {
            ((ViewGroup) addQuotePopupView.getParent()).removeView(addQuotePopupView);
        }

        dialogBuilder.setView(addQuotePopupView);
        dialog = dialogBuilder.create();
        dialog.show();
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

    public static void getData(Context context, customCallback callback){
        connection.getData("SELECT * From task_table", context, (todayvalue, todaySize) -> connection.getData("SELECT * FROM notdonetask_table", context, (notDonevalue, notDoneSize) -> {
            todayvalue.addAll(notDonevalue);
            callback.Data(todayvalue, todaySize);
        }));
    }

    public static void add_book_dialog() {
        if (addBookPopupView.getParent() != null) {
            ((ViewGroup) addBookPopupView.getParent()).removeView(addBookPopupView);
        }
        EditText bookName = addBookPopupView.findViewById(R.id.bookName);
        EditText bookPages = addBookPopupView.findViewById(R.id.pages);
        Button Add = addBookPopupView.findViewById(R.id.add);
        Add.setOnClickListener(v -> {
            if (bookName.getText().toString() != "" && bookPages.getText().toString() != "") {
                String BookName = bookName.getText().toString();
                String BookPages = bookPages.getText().toString();
                JSONObject obj = new JSONObject();
                try {
                    obj.put("book", BookName);
                    obj.put("pages", BookPages);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                connection.sendData(obj);
                System.out.println("book name is " + BookName + " and pages is " + BookPages);
            }
            bookName.setText("");
            bookPages.setText("");
        });
        dialogBuilder.setView(addBookPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
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