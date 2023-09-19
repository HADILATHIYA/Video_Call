package com.example.datingapp.Original;

import android.app.Activity;
import android.util.Log;

import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SSGRetrofitClient {
    private static Retrofit ourInstance;

    public static Retrofit getInstance(Activity activity) {
        if (ourInstance == null) {
            try {
                Log.e("zfsfZSfvZXcvz", "getInstance: "+AESSUtils.decryptA(activity , "6ABD1F2FEBB37EC5A667EC583C4672B0274702A4C6A376CEB9B608C3EECF5DF7") );
                ourInstance = new Builder().baseUrl(AESSUtils.decryptA(activity , "6ABD1F2FEBB37EC5A667EC583C4672B0274702A4C6A376CEB9B608C3EECF5DF7"))
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ourInstance;
    }

    private SSGRetrofitClient() {
    }
}
