package com.example.myproject.data.model.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BannerImage {
    @SerializedName("data")
    private List<ImageMessage> data;
    private int errorCode;
    private String errorMsg;

    public List<ImageMessage> getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
