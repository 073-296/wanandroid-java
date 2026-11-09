package com.example.myproject.entity.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BannerImage {
    @SerializedName("data")
    public List<ImageMessage> data;
    public int errorCode;
    public String errorMsg;



}
