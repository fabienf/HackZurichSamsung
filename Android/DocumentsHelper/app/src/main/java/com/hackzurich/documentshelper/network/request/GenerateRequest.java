package com.hackzurich.documentshelper.network.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request for generation file
 */
public class GenerateRequest {

    @SerializedName("id")
    private String mId;

    @SerializedName("parts")
    private List<String> mParts;

    public GenerateRequest(String id, List<String> parts) {
        mId = id;
        mParts = parts;
    }

    public String getId() {
        return mId;
    }

    public List<String> getParts() {
        return mParts;
    }
}
