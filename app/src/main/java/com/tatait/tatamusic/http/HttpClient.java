package com.tatait.tatamusic.http;

import android.graphics.Bitmap;

import com.tatait.tatamusic.model.ArtistInfo;
import com.tatait.tatamusic.model.CollectMusic;
import com.tatait.tatamusic.model.DownloadInfo;
import com.tatait.tatamusic.model.Info;
import com.tatait.tatamusic.model.Lrc;
import com.tatait.tatamusic.model.OnlineMusic;
import com.tatait.tatamusic.model.OnlineMusicList;
import com.tatait.tatamusic.model.SearchMusic;
import com.tatait.tatamusic.model.Splash;
import com.tatait.tatamusic.model.User;
import com.tatait.tatamusic.utils.Preferences;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

import okhttp3.Call;

/**
 * Created by Lynn on 2017/2/8.
 */
public class HttpClient {
    private static final String SPLASH_URL = "https://turtle.leanapp.cn/getMainImg";
    private static final String BASE_URL = "http://tingapi.ting.baidu.com/v1/restserver/ting";
    private static final String API_URL = "https://turtle.leanapp.cn/";
    private static final String METHOD_GET_MUSIC_LIST = "baidu.ting.billboard.billList";
    private static final String METHOD_DOWNLOAD_MUSIC = "baidu.ting.song.play";
    private static final String METHOD_ARTIST_INFO = "baidu.ting.artist.getInfo";
    private static final String METHOD_SEARCH_MUSIC = "baidu.ting.search.catalogSug";
    private static final String METHOD_LRC = "baidu.ting.song.lry";
    private static final String METHOD_GET_REGISTER = "registerUser";
    private static final String METHOD_GET_LOGIN = "loginUser";
    private static final String METHOD_COLLECT_MUSIC = "collectMusic";
    private static final String METHOD_GET_COLLECT_MUSIC = "getCollectMusic";
    private static final String METHOD_UNCOLLECT_MUSIC = "unCollectMusic";
    private static final String METHOD_POST_FEEK_BACK = "addFeekBack";
    private static final String PARAM_METHOD = "method";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_SONG_ID = "songid";
    private static final String PARAM_TING_UID = "tinguid";
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_MOBILE = "mobile";
    private static final String PARAM_IMGURL = "imgurl";
    private static final String PARAM_FROM = "from";
    private static final String PARAM_SONGID = "song_id";
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_ARTIST_NAME = "artist_name";
    private static final String PARAM_ALBUM_TITLE = "album_title";
    private static final String PARAM_LRC_LINK = "lrc_link";
    private static final String PARAM_PIC_BIG = "pic_big";
    private static final String PARAM_PIC_SMALL = "pic_small";
    private static final String PARAM_IS_SEARCH = "is_search";
    private static final String PARAM_UID = "uid";
    private static final String PARAM_PAGE_INDEX = "pageIndex";
    private static final String PARAM_PAGE_SIZE = "pageSize";
    private static final String PARAM_CONTENT = "content";
    private static final String PARAM_TOKEN = "token";

    public static void getSplash(final HttpCallback<Splash> callback) {
        OkHttpUtils.get().url(SPLASH_URL).build()
                .execute(new JsonCallback<Splash>(Splash.class) {
                    @Override
                    public void onResponse(Splash response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void downloadFile(String url, String destFileDir, String destFileName, final HttpCallback<File> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new FileCallBack(destFileDir, destFileName) {
                    @Override
                    public void inProgress(float progress, long total, int id) {
                    }

                    @Override
                    public void onResponse(File file, int id) {
                        callback.onSuccess(file);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getSongListInfo(String type, int size, int offset, final HttpCallback<OnlineMusicList> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_GET_MUSIC_LIST)
                .addParams(PARAM_TYPE, type)
                .addParams(PARAM_SIZE, String.valueOf(size))
                .addParams(PARAM_OFFSET, String.valueOf(offset))
                .build()
                .execute(new JsonCallback<OnlineMusicList>(OnlineMusicList.class) {
                    @Override
                    public void onResponse(OnlineMusicList response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getMusicDownloadInfo(String songId, final HttpCallback<DownloadInfo> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_DOWNLOAD_MUSIC)
                .addParams(PARAM_SONG_ID, songId)
                .build()
                .execute(new JsonCallback<DownloadInfo>(DownloadInfo.class) {
                    @Override
                    public void onResponse(DownloadInfo response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getBitmap(String url, final HttpCallback<Bitmap> callback) {
        OkHttpUtils.get().url(url).build()
                .execute(new BitmapCallback() {
                    @Override
                    public void onResponse(Bitmap bitmap, int id) {
                        callback.onSuccess(bitmap);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getLrc(String songId, final HttpCallback<Lrc> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_LRC)
                .addParams(PARAM_SONG_ID, songId)
                .build()
                .execute(new JsonCallback<Lrc>(Lrc.class) {
                    @Override
                    public void onResponse(Lrc response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void searchMusic(String keyword, final HttpCallback<SearchMusic> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_SEARCH_MUSIC)
                .addParams(PARAM_QUERY, keyword)
                .build()
                .execute(new JsonCallback<SearchMusic>(SearchMusic.class) {
                    @Override
                    public void onResponse(SearchMusic response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getArtistInfo(String tingUid, final HttpCallback<ArtistInfo> callback) {
        OkHttpUtils.get().url(BASE_URL)
                .addParams(PARAM_METHOD, METHOD_ARTIST_INFO)
                .addParams(PARAM_TING_UID, tingUid)
                .build()
                .execute(new JsonCallback<ArtistInfo>(ArtistInfo.class) {
                    @Override
                    public void onResponse(ArtistInfo response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getRegisterUser(String userName, String passwWord, final HttpCallback<User> callback) {
        OkHttpUtils.get().url(API_URL + METHOD_GET_REGISTER)
                .addParams(PARAM_NAME, userName)
                .addParams(PARAM_PASSWORD, passwWord)
                .addParams(PARAM_MOBILE, "1")
                .addParams(PARAM_IMGURL, "http://omzogcv8w.bkt.clouddn.com/turtleUser_" + ((int) (Math.random() * 50) + 1) + ".png")
                .addParams(PARAM_FROM, "TataMusic")
                .build()
                .execute(new JsonCallback<User>(User.class) {
                    @Override
                    public void onResponse(User response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getLoginUser(String userName, String passwWord, final HttpCallback<User> callback) {
        OkHttpUtils.get().url(API_URL + METHOD_GET_LOGIN)
                .addParams(PARAM_NAME, userName)
                .addParams(PARAM_PASSWORD, passwWord)
                .build()
                .execute(new JsonCallback<User>(User.class) {
                    @Override
                    public void onResponse(User response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void collectMusic(OnlineMusic onlineMusic, String isSearch, final HttpCallback<CollectMusic> callback) {
        OkHttpUtils.get().url(API_URL + METHOD_COLLECT_MUSIC)
                .addParams(PARAM_SONGID, onlineMusic.getSong_id())
                .addParams(PARAM_TITLE, onlineMusic.getTitle())
                .addParams(PARAM_ARTIST_NAME, onlineMusic.getArtist_name())
                .addParams(PARAM_ALBUM_TITLE, onlineMusic.getAlbum_title())
                .addParams(PARAM_LRC_LINK, onlineMusic.getLrclink())
                .addParams(PARAM_PIC_BIG, onlineMusic.getPic_big())
                .addParams(PARAM_PIC_SMALL, onlineMusic.getPic_small())
                .addParams(PARAM_TING_UID, onlineMusic.getTing_uid())
                .addParams(PARAM_IS_SEARCH, isSearch)
                .addParams(PARAM_UID, Preferences.getUid())
                .build()
                .execute(new JsonCallback<CollectMusic>(CollectMusic.class) {
                    @Override
                    public void onResponse(CollectMusic response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void getCollectMusic(int index, int pageSize, final HttpCallback<CollectMusic> callback) {
        OkHttpUtils.get().url(API_URL + METHOD_GET_COLLECT_MUSIC)
                .addParams(PARAM_UID, Preferences.getUid())
                .addParams(PARAM_PAGE_INDEX, String.valueOf(index))
                .addParams(PARAM_PAGE_SIZE, String.valueOf(pageSize))
                .build()
                .execute(new JsonCallback<CollectMusic>(CollectMusic.class) {
                    @Override
                    public void onResponse(CollectMusic response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void unCollectMusic(String song_id, final HttpCallback<CollectMusic> callback) {
        OkHttpUtils.get().url(API_URL + METHOD_UNCOLLECT_MUSIC)
                .addParams(PARAM_UID, Preferences.getUid())
                .addParams(PARAM_SONGID, song_id)
                .build()
                .execute(new JsonCallback<CollectMusic>(CollectMusic.class) {
                    @Override
                    public void onResponse(CollectMusic response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }

    public static void postFeekBack(String content, String phone, final HttpCallback<Info> callback) {
        OkHttpUtils.get().url(API_URL + METHOD_POST_FEEK_BACK)
                .addParams(PARAM_CONTENT, content + "【TataMusic:" + phone + "】")
                .addParams(PARAM_UID, Preferences.getUid())
                .addParams(PARAM_TOKEN, Preferences.getUserToken())
                .build()
                .execute(new JsonCallback<Info>(Info.class) {
                    @Override
                    public void onResponse(Info response, int id) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        callback.onFail(e);
                    }

                    @Override
                    public void onAfter(int id) {
                        callback.onFinish();
                    }
                });
    }
}