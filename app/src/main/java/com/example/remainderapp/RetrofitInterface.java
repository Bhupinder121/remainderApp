package com.example.remainderapp;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RetrofitInterface {

    @POST("/getData")
    Call<JSONObject> sendData(@Body JSONObject map);
}
