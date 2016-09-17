package com.hackzurich.documentshelper.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 */
public class ServiceClient {

    private static final String ENDPOINT = "http://hackzurich16.eu-gb.mybluemix.net";

    private static ServiceClient mClient;

    public ServiceApi mServiceApi;

    private ServiceClient() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ENDPOINT)
                .client(okHttpClient)
                .build();

        mServiceApi = retrofit.create(ServiceApi.class);
    }

    public static ServiceClient getInstance() {
        if (mClient == null) {
            mClient = new ServiceClient();
        }
        return mClient;
    }

    public ServiceApi getServiceApi() {
        return mServiceApi;
    }

}
