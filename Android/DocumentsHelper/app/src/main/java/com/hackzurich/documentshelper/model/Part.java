package com.hackzurich.documentshelper.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 *
 */
public class Part {

    @SerializedName("id")
    private String mId;

    @SerializedName("name")
    private String mName;

    @SerializedName("description")
    private String mDescription;

    @SerializedName("pages")
    private List<Integer> mPages;

    @SerializedName("keys")
    private List<String> mKeys;

    @Expose
    private boolean mSelected;

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

    public String getId() {
        return mId;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }
}
