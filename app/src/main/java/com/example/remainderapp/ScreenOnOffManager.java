package com.example.remainderapp;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;



public class ScreenOnOffManager extends Service {
    private boolean previousState = false;
    private Date stateChanged = null;
    private boolean sendNoti = false, wakeup = false;
    public static boolean isCall = false;
    final boolean[] toggle = {true};

    boolean oneTime = false;
    private long max_service_time = (long) 60 * 1000 * 15;
    private long waitTime = 30000;
    private long sleepTime = (long) 2.16e+7;
    private static serverConnection connection;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        connection = new serverConnection();
        connection.setup();
        sharedPreferences = getSharedPreferences(MainActivity.SharedName, MODE_PRIVATE);



        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            startMyOwnForeground();
        }
        else {
            startForeground(1, new Notification());
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null) {
            return START_STICKY;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startTimer();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    private String timeManager() throws ParseException {
        float storedTime = sharedPreferences.getFloat("notiTime", 15);
        max_service_time = (long) 60 * 1000 * (long) storedTime;
//        System.out.println(max_service_time/60000);
        String notification = "";
        java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");
        int hou = Calendar.getInstance().getTime().getHours();
        int min = Calendar.getInstance().getTime().getMinutes();
        int sec = Calendar.getInstance().getTime().getSeconds();
        String source = hou + ":" + min + ":" + sec;
        Date currentTime = df.parse(source);
        if(isCall){
            if(!screenState()) {
                toggle[0] = false;
                oneTime = true;
            }
        }
        else {
            if(oneTime) {
                oneTime = false;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("running after 30 s");
                        toggle[0] = true;
                    }
                }, waitTime);
            }
        }
        if(screenState() != previousState && toggle[0]){// Add test Call receiver
            previousState = screenState();
            System.out.println(previousState);
            try {
                stateChanged = df.parse(source);
            } catch (ParseException e) {
                e.printStackTrace();
            }
//            if(wakeup){
//                wakeup = false;
//                sendNoti = false;
//                wakeup = false;
//                wakeUp();
//                showQuote();
//            }
            if(sendNoti){
                sendNoti = false;
                stateChanged = currentTime;
                showData();
                // send notification;
                System.out.println("off Notification");
            }
        }
        else{
            long timedifference = currentTime.getTime()-stateChanged.getTime();
            if(!isCall){
                if(timedifference >= sleepTime){
                    if(!screenState()){
                        wakeup = true;
                    }
                }
                else if(timedifference >= max_service_time){
                    if(screenState() && toggle[0]){
                        showData();
                        // send notification
                        stateChanged = currentTime;
                        System.out.println("On Notification");
                    }
                    else {
                        stateChanged = currentTime;
                        showData();
                        System.out.println("off Notification");
//                        sendNoti = true;
                    }
                }
            }
        }
        return notification;
    }

    private void getData(Context context, customCallback callback){
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

    private void showQuote(){
        connection.getData("SELECT * FROM quotes_table ORDER BY RAND() LIMIT 1", ScreenOnOffManager.this,0, new customCallback() {
            @Override
            public void Data(ArrayList<JSONObject> value, int arraySize) throws JSONException {
                String quote = value.get(0).getString("quoteName");
                sendNotification(getApplicationContext(),"Quote of the day",quote) ;
            }

            @Override
            public void onError(String error) {
                System.out.println(error);
            }
        });
    }

    private void showData(){
        getData(getApplicationContext(), new customCallback() {
            @Override
            public void Data(ArrayList<JSONObject> value, int arraySize) throws JSONException {
                List<String> tasks = new ArrayList<>();
                int count = 0;
                for (JSONObject obj : value) {
                    if(obj.getInt("isDone") == 0) {
                        count++;
                        tasks.add(obj.getString("taskName"));
                    }
                }
                if (count > 0){
                    String taskNames = countFrequencies(tasks);
                    showTasKs(taskNames, count);
                }
            }

            @Override
            public void onError(String error) {
                System.out.println(error);
            }
        });
    }

    private void showTasKs(String task, int count) {
        RemoteViews collaspedView = new RemoteViews(getPackageName(), R.layout.collasped_notification);
        RemoteViews expanedView = new RemoteViews(getPackageName(), R.layout.list_notification);
        String remainingTask = count + " Task Remaining";

        expanedView.setTextViewText(R.id.editTask, task);
        collaspedView.setTextViewText(R.id.remainingTask, remainingTask);

        Intent resIntent = new Intent(this, MainActivity.class);
        PendingIntent PenIntent = PendingIntent.getActivity(this, 3, resIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, "WakeUP")
//                .setContentTitle(title)
//                .setContentText(task)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(remainingTask)
                .setContentText(task)
//                .setCustomContentView(collaspedView)
                .setCustomBigContentView(expanedView)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(PenIntent)
                .build();

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, notification);
    }

    public static String countFrequencies(List<String> list) {
        StringBuilder tasks = new StringBuilder();
        tasks.append("").append("\n");
        Set<String> st = new HashSet<>(list);
        for (String s : st) {
            tasks.append(Collections.frequency(list, s)).append(" ").append(s).append("\n");
        }
        return tasks.toString();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel("WakeUP", "WakeUP", NotificationManager.IMPORTANCE_MAX);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

//    private void getData(customCallback callback){
//        connection.getData("SELECT * From task_table", getApplicationContext(),0, (todayvalue, todaySize) -> connection.getData("SELECT * FROM notdonetask_table", getApplicationContext(),0, (notDonevalue, notDoneSize) -> {
//            todayvalue.addAll(notDonevalue);
//            callback.Data(todayvalue, todaySize);
//        }));
//    }

    private void wakeUp() {
        Intent resIntent = new Intent(this, MainActivity.class);
        PendingIntent PenIntent = PendingIntent.getActivity(this, 1, resIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, "WakeUP")
                .setContentTitle("WAKE UP")
                .setContentText("The task is Make Bed")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(PenIntent)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(2, notification);
    }

    public static void sendNotification(Context context, String title,String Info) {
        Notification notification = new NotificationCompat.Builder(context, "WakeUP")
                .setContentTitle(title)
                .setContentText(Info)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(3, notification);
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
//                wakeUp
                try {
                    timeManager();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private boolean screenState(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.remainderApp";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}
