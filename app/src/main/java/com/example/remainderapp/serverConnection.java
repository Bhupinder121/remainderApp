package com.example.remainderapp;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class serverConnection {
    RetrofitInterface retrofitInterface;
    Retrofit retrofit;
    String baseUrl = "http://103.68.22.220:3000";
//    String baseUrl = "https://tesl-server.herokuapp.com";
    int waitfor = 8;

    public void setup(){
        Gson gson = new GsonBuilder().setLenient().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        retrofitInterface = retrofit.create(RetrofitInterface.class);
    }

    public void getData(String data, Context context, int id, com.example.remainderapp.customCallback callback){
        String  date_category = Encryption_Decryption.encrypt(data).replace("+", "t36i")
                .replace("/", "8h3nk1").replace("=", "d3ink2"); // Add encryption

        Call<String> call = retrofitInterface.getData(date_category, "remainder");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                ArrayList<JSONObject> objects = processData(response.body());
                try {
                    callback.Data(objects, objects.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                System.out.println("error");
                if(id < waitfor) {
                    getData(data, context, id + 1, new customCallback() {
                        @Override
                        public void Data(ArrayList<JSONObject> value, int arraySize) throws JSONException {
                            callback.Data(value, arraySize);
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
//
                }
                else {
                    System.out.println(t.getMessage());
                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private ArrayList<JSONObject> processData(String response){
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
        return objects;
    }

    public void sendData(JSONObject data) {
        JSONObject encryptedData = new JSONObject();
        try {
            encryptedData.put("json", Encryption_Decryption.encrypt(data.toString()));
            encryptedData.put("server", "remainder");
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
