package com.tatait.tatamusic.constants;

/**
 * Actions
 * Created by Lynn on 2016/1/22.
 */
public interface Actions {
    String ACTION_MEDIA_PLAY_PAUSE = "com.tatait.tatamusic.ACTION_MEDIA_PLAY_PAUSE";
    String ACTION_MEDIA_NEXT = "com.tatait.tatamusic.ACTION_MEDIA_NEXT";
    String ACTION_MEDIA_PREVIOUS = "com.tatait.tatamusic.ACTION_MEDIA_PREVIOUS";
    String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    String LIST_TYPE_COLLECT = "collect";
    String LIST_TYPE_LOCAL = "local";
    String LIST_TYPE_ONLINE = "online";
    int DB_PLAY_LIST_LOCAL = 0;
    int DB_PLAY_LIST_COLLECT = 1;
    int DB_PLAY_LIST_ONLINE = 2;
}