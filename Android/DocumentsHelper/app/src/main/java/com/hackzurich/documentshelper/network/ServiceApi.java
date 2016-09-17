package com.hackzurich.documentshelper.network;


import com.hackzurich.documentshelper.model.Document;
import com.hackzurich.documentshelper.network.request.GenerateRequest;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import rx.Observable;

/**
 * Define here REST API calls
 */
public interface ServiceApi {

    @Multipart
    @POST("/upload")
    Observable<Document> upload(@Part MultipartBody.Part file);

    @POST("/generate")
    @Streaming
    Observable<ResponseBody> generate(@Body GenerateRequest request);

}
