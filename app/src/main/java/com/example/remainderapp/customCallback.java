package com.example.remainderapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public interface customCallback {
    void Data(ArrayList<JSONObject> value, int arraySize) throws JSONException;
    void onError(String error);
}
