package com.hackzurich.documentshelper.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Document for printing
 */
public class Document {

    @SerializedName("id")
    private String mId;

    @SerializedName("file")
    private String mFile;

    @SerializedName("type")
    private String mType;

    @SerializedName("parts")
    private List<Part> mParts;

    public String getFile() {
        return mFile;
    }

    public String getType() {
        return mType;
    }

    public List<Part> getParts() {
        return mParts;
    }

    public String getId() {
        return mId;
    }
}
