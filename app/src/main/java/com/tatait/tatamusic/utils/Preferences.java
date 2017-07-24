package com.tatait.tatamusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.model.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * SharedPreferences工具类
 * Created by Lynn on 2015/11/28.
 */
public class Preferences {
    private static final String MUSIC_ID = "music_id";
    private static final String PLAY_MODE = "play_mode";
    private static final String SPLASH_URL = "splash_url";
    private static final String NIGHT_MODE = "night_mode";
    private static final String HAS_LOGIN = "has_login";
    private static final String USER_NAME = "user_name";
    private static final String UID = "uid";
    private static final String TOKEN = "token";
    private static final String QUEUE_LIST = "queue_list";
    private static final String LAST_POSITION = "last_position";
    private static final String FINAL_POSITION = "final_position";
    private static final String LIST_TYPE = "list_type";
    private static final String LIST_NEED_CLEAN = "list_need_clean";
    private static final String LIST_CHANGE_TO_LOCAL = "list_change_to_local";
    private static final String COLL_CHANGE_TO_LOCAL = "coll_change_to_local";

    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static long getCurrentSongId() {
        return getLong(MUSIC_ID, -1);
    }

    public static void saveCurrentSongId(long id) {
        saveLong(MUSIC_ID, id);
    }

    public static int getPlayMode() {
        return getInt(PLAY_MODE, 0);
    }

    public static void savePlayMode(int mode) {
        saveInt(PLAY_MODE, mode);
    }

    public static String getSplashUrl() {
        return getString(SPLASH_URL, "");
    }

    public static void saveSplashUrl(String url) {
        saveString(SPLASH_URL, url);
    }

    public static boolean enableMobileNetworkPlay() {
        return getBoolean(sContext.getString(R.string.setting_key_mobile_network_play), false);
    }

    public static void saveMobileNetworkPlay(boolean enable) {
        saveBoolean(sContext.getString(R.string.setting_key_mobile_network_play), enable);
    }

    public static boolean enableMobileNetworkDownload() {
        return getBoolean(sContext.getString(R.string.setting_key_mobile_network_download), false);
    }

    public static void saveMobileNetworkDownload(boolean enable) {
        saveBoolean(sContext.getString(R.string.setting_key_mobile_network_download), enable);
    }

    public static boolean isNightMode() {
        return getBoolean(NIGHT_MODE, false);
    }

    public static void saveNightMode(boolean on) {
        saveBoolean(NIGHT_MODE, on);
    }

    public static boolean isLogin() {
        return getBoolean(HAS_LOGIN, false);
    }

    public static void saveIsLogin(boolean loginStatus) {
        saveBoolean(HAS_LOGIN, loginStatus);
    }

    public static String getUserName() {
        return getString(USER_NAME, "未登录");
    }

    public static void saveUserName(String userName) {
        saveString(USER_NAME, userName);
    }

    public static String getLastPosition() {
        return getString(LAST_POSITION, "-1");
    }

    public static void saveLastPosition(String position) {
        saveString(LAST_POSITION, position);
    }

    public static String getFinalPosition() {
        return getString(FINAL_POSITION, "-1");
    }

    public static void saveFinalPosition(String position) {
        saveString(FINAL_POSITION, position);
    }

    public static String getUserToken() {
        return getString(TOKEN, "");
    }

    public static void saveUserToken(String uid) {
        saveString(TOKEN, uid);
    }

    public static String getUid() {
        return getString(UID, "-1");
    }

    public static void saveUid(String uid) {
        saveString(UID, uid);
    }

    public static boolean isCollectChangeToLocal() {
        return getBoolean(COLL_CHANGE_TO_LOCAL, false);
    }

    public static void saveCollectChangeToLocal(boolean collectChangeToLocal) {
        saveBoolean(COLL_CHANGE_TO_LOCAL, collectChangeToLocal);
    }

    public static boolean isListChangeToLocal() {
        return getBoolean(LIST_CHANGE_TO_LOCAL, false);
    }

    public static void saveListChangeToLocal(boolean listChangeToLocal) {
        saveBoolean(LIST_CHANGE_TO_LOCAL, listChangeToLocal);
    }

    public static String getListType() {
        return getString(LIST_TYPE, Actions.LIST_TYPE_LOCAL);
    }

    public static void saveListType(String listType) {
        saveString(LIST_TYPE, listType);
    }

    public static boolean isListNeedClean() {
        return getBoolean(LIST_NEED_CLEAN, false);
    }

    public static void saveListNeedClean(boolean ListNeedClean) {
        saveBoolean(LIST_NEED_CLEAN, ListNeedClean);
    }

    public static ArrayList<Music> getQueueList() {
        return getList(QUEUE_LIST, Music.class);
    }

    public static void saveQueueList(List<Music> musicList) {
        saveList(QUEUE_LIST, musicList);
    }

    private static boolean getBoolean(String key, boolean defValue) {
        return getPreferences().getBoolean(key, defValue);
    }

    private static void saveBoolean(String key, boolean value) {
        getPreferences().edit().putBoolean(key, value).apply();
    }

    private static int getInt(String key, int defValue) {
        return getPreferences().getInt(key, defValue);
    }

    private static void saveInt(String key, int value) {
        getPreferences().edit().putInt(key, value).apply();
    }

    private static long getLong(String key, long defValue) {
        return getPreferences().getLong(key, defValue);
    }

    private static void saveLong(String key, long value) {
        getPreferences().edit().putLong(key, value).apply();
    }

    private static String getString(String key, @Nullable String defValue) {
        return getPreferences().getString(key, defValue);
    }

    private static void saveString(String key, @Nullable String value) {
        getPreferences().edit().putString(key, value).apply();
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sContext);
    }

    private static <T> void saveList(String tag, List<T> datalist) {
        if (null == datalist)
            return;
        Gson gson = new Gson();
        //转换成json数据，再保存
        if (datalist.size() > 50) {
            List<T> subList = datalist.subList(0, 50);
            String strJson = gson.toJson(subList);
            getPreferences().edit().putString(tag, strJson).apply();
        } else {
            String strJson = gson.toJson(datalist);
            getPreferences().edit().putString(tag, strJson).apply();
        }
    }

    private static <T> ArrayList<T> getList(String tag, Class<T> cls) {
        ArrayList<T> datalist = new ArrayList<>();
        String strJson = getPreferences().getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        JsonArray arry = new JsonParser().parse(strJson).getAsJsonArray();
        for (JsonElement jsonElement : arry) {
            datalist.add(gson.fromJson(jsonElement, cls));
        }
        return datalist;
    }
}