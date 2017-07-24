package com.tatait.tatamusic.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Locale;

/**
 * 启动画面Java bean
 * Created by Lynn on 2016/3/2.
 */
public class Splash {
    private static final String URL = "http://omzogcv8w.bkt.clouddn.com/%s.png";

    @SerializedName("data")
    private List<ImgBean> data;

    public String getUrl() {
        if (data != null && !data.isEmpty()) {
            String baseUrl = data.get(0).mainImg;
            if (!TextUtils.isEmpty(baseUrl)) {
                return String.format(Locale.getDefault(), URL, baseUrl);
            }
        }
        return null;
    }

    private static class ImgBean {
        @SerializedName("mainImg")
        private String mainImg;
    }
}