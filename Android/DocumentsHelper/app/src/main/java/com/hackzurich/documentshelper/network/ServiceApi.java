package com.hackzurich.documentshelper.network;


import com.hackzurich.documentshelper.model.Document;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

/**
 * Define here REST API calls
 */
public interface ServiceApi {

    @Multipart
    @POST("/upload")
    Observable<Document> upload(@Part MultipartBody.Part file);

}
