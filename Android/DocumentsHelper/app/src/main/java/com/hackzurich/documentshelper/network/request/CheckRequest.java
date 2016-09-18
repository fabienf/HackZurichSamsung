package com.hackzurich.documentshelper.network.request;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class CheckRequest {

    @SerializedName("filename")
    private String mFilename;

    public CheckRequest(String filename) {
        mFilename = filename;
    }

    public String getFilename() {
        return mFilename;
    }
}
