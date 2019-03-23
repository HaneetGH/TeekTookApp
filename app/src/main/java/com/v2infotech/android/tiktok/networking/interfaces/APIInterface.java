package com.v2infotech.android.tiktok.networking.interfaces;

import com.v2infotech.android.tiktok.model.GetDetailModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {



    @GET("/profile/getdetails")
    Call<GetDetailModel> doGetUserList(@Query("page") String page);


}
