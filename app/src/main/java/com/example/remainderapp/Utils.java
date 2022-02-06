package com.example.remainderapp;

import android.content.Context;
import android.widget.Toast;

public class Utils {
    public static void showMes(Context context, String mess){
        Toast.makeText(context, mess, Toast.LENGTH_SHORT).show();
    }
}
