package com.hackzurich.documentshelper.network;

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

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ENDPOINT)
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
