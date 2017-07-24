package com.tatait.tatamusic.model;

import com.google.gson.annotations.SerializedName;

/**
 * JavaBean
 * Created by Lynn on 2017/1/11.
 */
public class Info {
    // 回参code
    @SerializedName("code")
    private String code;
    // 接口信息
    @SerializedName("info")
    private String info;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}