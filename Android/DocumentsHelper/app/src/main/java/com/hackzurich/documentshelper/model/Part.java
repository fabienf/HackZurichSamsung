package com.hackzurich.documentshelper.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 *
 */
public class Part {


    @SerializedName("name")
    private String mName;

    @SerializedName("description")
    private String mDescription;

    @SerializedName("pages")
    private List<Integer> mPages;

    @SerializedName("keys")
    private List<String> mKeys;

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public List<Integer> getPages() {
        return mPages;
    }

    public List<String> getKeys() {
        return mKeys;
    }
}
