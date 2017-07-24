package com.tatait.tatamusic.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 单曲信息
 * Created by Lynn on 2015/11/27.
 */
public class Music implements Parcelable {
    public static final String KEY_ID = "id";
    public static final String KEY_SONG_ID = "song_id";
    public static final String KEY_ALBUM_ID = "album_id";
    public static final String KEY_ALBUM = "album";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_TITLE = "title";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_ARTIST_ID = "artist_id";
    public static final String KEY_DATA = "data";
    public static final String KEY_PIC_BIG = "pic_big";
    public static final String KEY_PIC_SMALL = "pic_small";
    public static final String KEY_FILE_NAME = "fileName";
    public static final String KEY_FILE_SIZE = "fileSize";
    public static final String KEY_PATH = "path";
    public static final String KEY_LRC_LINK = "lrclink";
    public static final String KEY_IS_LOCAL = "is_local";
    public static final String KEY_IS_COLLECT = "is_collect";
    public static final String KEY_COVER_PATH = "cover_path";
    public static final String KEY_TING_UID = "ting_uid";

    // 歌曲类型:本地/网络
    public Type type;
    // [本地歌曲]歌曲id
    private long id;
    // 在线歌曲id
    public long songId;
    // 专辑ID
    public long albumId = -1;
    // 专辑
    public String album;
    // 持续时间
    public long duration;
    // 音乐标题
    public String title;
    // 艺术家
    public String artist;
    // 艺术家ID
    public long artistId;

    public String data;
    // 预览大图
    public String pic_big;
    // 预览小图
    public String pic_small;
    // 文件名
    private String fileName;
    // 文件大小
    public long fileSize;
    // 音乐路径
    public String path;
    // 歌词路径
    public String lrclink;
    //0表示在线 1表示本地
    public long isLocal = 0;
    //0表示没有收藏 1表示收藏
    public long isCollect = 0;
    // 专辑封面路径
    public String CoverPath;

    public String Ting_uid;

    public static final Creator<Music> CREATOR = new Creator<Music>() {

        @Override
        public Music createFromParcel(Parcel source) {
            Music music = new Music();
            Bundle bundle = source.readBundle();
            music.id = bundle.getLong(KEY_ID);
            music.songId = bundle.getLong(KEY_SONG_ID);
            music.albumId = bundle.getLong(KEY_ALBUM_ID);
            music.album = bundle.getString(KEY_ALBUM);
            music.duration = bundle.getLong(KEY_DURATION);
            music.title = bundle.getString(KEY_TITLE);
            music.artist = bundle.getString(KEY_ARTIST);
            music.artistId = bundle.getLong(KEY_ARTIST_ID);
            music.data = bundle.getString(KEY_DATA);
            music.pic_big = bundle.getString(KEY_PIC_BIG);
            music.pic_small = bundle.getString(KEY_PIC_SMALL);
            music.fileSize = bundle.getLong(KEY_FILE_SIZE);
            music.fileName = bundle.getString(KEY_FILE_NAME);
            music.path = bundle.getString(KEY_PATH);
            music.lrclink = bundle.getString(KEY_LRC_LINK);
            music.isLocal = bundle.getLong(KEY_IS_LOCAL);
            music.isCollect = bundle.getLong(KEY_IS_COLLECT);
            music.CoverPath = bundle.getString(KEY_COVER_PATH);
            music.Ting_uid = bundle.getString(KEY_TING_UID);
            return music;
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_ID, id);
        bundle.putLong(KEY_SONG_ID, songId);
        bundle.putLong(KEY_ALBUM_ID, albumId);
        bundle.putString(KEY_ALBUM, album);
        bundle.putLong(KEY_DURATION, duration);
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_ARTIST, artist);
        bundle.putLong(KEY_ARTIST_ID, artistId);
        bundle.putString(KEY_DATA, data);
        bundle.putString(KEY_PIC_BIG, pic_big);
        bundle.putString(KEY_PIC_SMALL, pic_small);
        bundle.putLong(KEY_FILE_SIZE, fileSize);
        bundle.putString(KEY_FILE_NAME, fileName);
        bundle.putString(KEY_PATH, path);
        bundle.putString(KEY_LRC_LINK, lrclink);
        bundle.putLong(KEY_IS_LOCAL, isLocal);
        bundle.putLong(KEY_IS_COLLECT, isCollect);
        bundle.putString(KEY_COVER_PATH, CoverPath);
        bundle.putString(KEY_TING_UID, Ting_uid);
        dest.writeBundle(bundle);
    }

    public enum Type {
        LOCAL,
        ONLINE
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
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

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(long isLocal) {
        this.isLocal = isLocal;
    }

    public long getIsCollect() {
        return isCollect;
    }

    public void setIsCollect(long isCollect) {
        this.isCollect = isCollect;
    }

    public String getPath() {
        return path;
    }

    public String getCoverPath() {
        return CoverPath;
    }

    public void setCoverPath(String coverPath) {
        CoverPath = coverPath;
    }

    public String getTing_uid() {
        return Ting_uid;
    }

    public void setTing_uid(String ting_uid) {
        Ting_uid = ting_uid;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 对比本地歌曲是否相同
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Music)) {
            return false;
        }
        return this.getSongId() == ((Music) o).getSongId();
    }
}