package com.tatait.tatamusic.http;

import android.os.Build;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lynn on 2017/3/30.
 */
public class HttpInterceptor implements Interceptor {
    private static final String UA = "User-Agent";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader(UA, makeUA())
                .build();
        return chain.proceed(request);
    }

    private String makeUA() {
        String brand = Build.BRAND;
        if (brand.contains(" ")) {
            brand = brand.replace(" ", "");
        }
        String model = Build.MODEL;
        if (model.contains(" ")) {
            model = model.replace(" ", "");
        }
        String release = Build.VERSION.RELEASE;
        if (release.contains(" ")) {
            release = release.replace(" ", "");
        }
        return brand + "/" + model + "/" + release;
    }
}