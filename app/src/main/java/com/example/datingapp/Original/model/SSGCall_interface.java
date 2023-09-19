package com.example.datingapp.Original.model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SSGCall_interface {
    @GET("call_start.php?go=retro&lan=en&vc=18")
    Call<List<SSGCall_model>> getPost(@Query("iid") String str, @Query("myid") String str2, @Query("phoneno") String str3, @Query("version") String str4);
}
