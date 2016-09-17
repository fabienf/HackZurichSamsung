package com.hackzurich.documentshelper.network.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request for generation file
 */
public class GenerateRequest {

    @SerializedName("id")
    private String mId;

    @SerializedName("pages")
    private List<List<Integer>> mPages;

    public GenerateRequest(String id, List<List<Integer>> pages) {
        mId = id;
        mPages = pages;
    }

    public String getId() {
        return mId;
    }

    public List<List<Integer>> getPages() {
        return mPages;
    }
}
