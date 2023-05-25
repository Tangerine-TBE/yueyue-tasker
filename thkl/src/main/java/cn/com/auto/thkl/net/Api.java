package cn.com.auto.thkl.net;

import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


import cn.com.auto.thkl.BuildConfig;
import cn.com.auto.thkl.Constant;

import java.util.concurrent.TimeUnit;

import cn.com.auto.thkl.utils.L;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {


    private static ApiService apiService;

    //单例
    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (Api.class) {
                if (apiService == null) {
                    new Api();
                }
            }
        }
        return apiService;
    }


    private Api() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                }
        }).setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).addInterceptor(httpLoggingInterceptor).readTimeout(20, TimeUnit.SECONDS).writeTimeout(20, TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient).baseUrl(BuildConfig.SYSTEM_VALUE.equals("debug") ? Constant.BASE_URL_DEBUG : Constant.BASE_URL_RELEASE).addConverterFactory(GsonConverterFactory.create())//请求的结果转为实体类
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();

        apiService = retrofit.create(ApiService.class);
    }
}
