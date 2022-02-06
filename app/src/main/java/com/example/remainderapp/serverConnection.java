package com.example.remainderapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class serverConnection {
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;
    String baseUrl = "http://192.168.0.118:3000";
    

    public void setup(){
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    public void getData(String date_category, Context context, com.example.remainderapp.customCallback callback){
        date_category = Encryption_Decryption.encrypt(date_category).replace("+", "t36i")
                .replace("/", "8h3nk1").replace("=", "d3ink2"); // Add encryption
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = baseUrl+"/sendData?data_query="+date_category;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    String encrypedData = response.replace("t36i", "+").replace("8h3nk1", "/").replace("d3ink2", "=");
                    String decryptionData = Encryption_Decryption.decrypt(encrypedData);
                    ArrayList<JSONObject> objects = new ArrayList<>();
                    try {
                        JSONArray jsonArray = new JSONArray(decryptionData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            objects.add(jsonArray.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callback.Data(objects, objects.size());
                }, error -> System.out.println("onErrorResponse: "+error));
        queue.add(stringRequest);
    }

    public void sendData(JSONObject data) {
        JSONObject encryptedData = new JSONObject();
        try {
            encryptedData.put("json", Encryption_Decryption.encrypt(data.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Call<JSONObject> call = retrofitInterface.sendData(encryptedData);
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, retrofit2.Response<JSONObject> response) {

            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {

            }
        });
    }

}
