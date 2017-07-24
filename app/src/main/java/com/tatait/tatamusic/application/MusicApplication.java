package com.tatait.tatamusic.application;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tatait.tatamusic.BuildConfig;
import com.tatait.tatamusic.http.HttpInterceptor;
import com.tatait.tatamusic.utils.Preferences;
import com.tencent.bugly.Bugly;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.tatait.tatamusic.api.KeyStore.BUGLY_APP_ID_;

/**
 * 自定义Application
 * Created by Lynn on 2015/11/27.
 */
public class MusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCache.init(this);
        AppCache.updateNightMode(Preferences.isNightMode());
        initOkHttpUtils();
        initImageLoader();
        initBugly();
    }

    private void initOkHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpInterceptor())
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    private void initImageLoader() {
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSize(2 * 1024 * 1024) // 2MB
                .diskCacheSize(50 * 1024 * 1024) // 50MB
                .build();
        ImageLoader.getInstance().init(configuration);
    }

    private void initBugly() {
        if (!BuildConfig.DEBUG) {
            Bugly.init(this, BUGLY_APP_ID_, false);
        }
    }
}