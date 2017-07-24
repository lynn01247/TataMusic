package com.tatait.tatamusic.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * JavaBean
 * Created by Lynn on 2017/1/11.
 */
public class CollectMusic {
    // 回参code
    @SerializedName("code")
    private String code;
    // 接口信息
    @SerializedName("info")
    private String info;
    // 当前页
    @SerializedName("page")
    private String page;
    // 每页数
    @SerializedName("pageSize")
    private String pageSize;
    // 总页数
    @SerializedName("total")
    private String total;
    // 总条数
    @SerializedName("sum")
    private String sum;
    // 歌曲数据
    @SerializedName("data")
    private List<Collect> data;

    // 数据属性
    public static class Collect {
        // 歌曲标题
        @SerializedName("title")
        private String title;
        // 艺术家
        @SerializedName("artist_name")
        private String artist_name;
        // 歌曲id
        @SerializedName("song_id")
        private String song_id;
        // 歌曲类型
        @SerializedName("is_search")
        private String is_search;

        @SerializedName("pic_big")
        private String pic_big;
        @SerializedName("pic_small")
        private String pic_small;
        @SerializedName("lrclink")
        private String lrclink;
        @SerializedName("ting_uid")
        private String ting_uid;
        @SerializedName("album_title")
        private String album_title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getArtist_name() {
            return artist_name;
        }

        public void setArtist_name(String artist_name) {
            this.artist_name = artist_name;
        }

        public String getSong_id() {
            return song_id;
        }

        public void setSong_id(String song_id) {
            this.song_id = song_id;
        }

        public String getIs_search() {
            return is_search;
        }

        public void setIs_search(String is_search) {
            this.is_search = is_search;
        }

        public String getPic_big() {
            return pic_big;
        }

        public void setPic_big(String pic_big) {
            this.pic_big = pic_big;
        }

        public String getPic_small() {
            return pic_small;
        }

        public void setPic_small(String pic_small) {
            this.pic_small = pic_small;
        }

        public String getLrclink() {
            return lrclink;
        }

        public void setLrclink(String lrclink) {
            this.lrclink = lrclink;
        }

        public String getTing_uid() {
            return ting_uid;
        }

        public void setTing_uid(String ting_uid) {
            this.ting_uid = ting_uid;
        }

        public String getAlbum_title() {
            return album_title;
        }

        public void setAlbum_title(String album_title) {
            this.album_title = album_title;
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

    public List<Collect> getData() {
        return data;
    }

    public void setData(List<Collect> data) {
        this.data = data;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }
}