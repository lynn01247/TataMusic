package com.tatait.tatamusic.model;

import com.google.gson.annotations.SerializedName;

/**
 * JavaBean
 * Created by Lynn on 2016/1/13.
 */
public class Lrc {
    @SerializedName("lrcContent")
    private String lrcContent;

    public String getLrcContent() {
        return lrcContent;
    }

    public void setLrcContent(String lrcContent) {
        this.lrcContent = lrcContent;
    }
}