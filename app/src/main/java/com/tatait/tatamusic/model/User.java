package com.tatait.tatamusic.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * JavaBean
 * Created by Lynn on 2017/1/11.
 */
public class User {
    // 回参code
    @SerializedName("code")
    private String code;
    // 接口信息
    @SerializedName("info")
    private String info;
    // 用户数据
    @SerializedName("data")
    private List<UserData> data;

    // 数据属性
    public static class UserData {
        // 用户id
        @SerializedName("uid")
        private String uid;
        // 姓名
        @SerializedName("name")
        private String name;
        // 用户token
        @SerializedName("token")
        private String token;
        // 手机
        @SerializedName("mobile")
        private String mobile;
        // 邮箱
        @SerializedName("email")
        private String email;
        // 住址
        @SerializedName("address")
        private String address;
        // 头像
        @SerializedName("imgurl")
        private String imgurl;
        // 备注
        @SerializedName("remark")
        private String remark;
        // 设备id
        @SerializedName("emid")
        private String emid;
        // 默认类别（本APP无效）
        @SerializedName("defaultid")
        private String defaultid;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getImgurl() {
            return imgurl;
        }

        public void setImgurl(String imgurl) {
            this.imgurl = imgurl;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getEmid() {
            return emid;
        }

        public void setEmid(String emid) {
            this.emid = emid;
        }

        public String getDefaultid() {
            return defaultid;
        }

        public void setDefaultid(String defaultid) {
            this.defaultid = defaultid;
        }
    }

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

    public List<UserData> getData() {
        return data;
    }

    public void setData(List<UserData> data) {
        this.data = data;
    }
}