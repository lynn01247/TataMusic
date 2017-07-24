package com.tatait.tatamusic.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.service.PlayService;
import com.tatait.tatamusic.utils.binding.ViewBinder;
import com.tatait.tatamusic.utils.permission.PermissionReq;

/**
 * 基类<br>
 * Created by Lynn on 2015/11/26.
 */
public abstract class BaseFragment extends Fragment {
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isInitialized;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewBinder.bind(this, view);
        init();
        setListener();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                isInitialized = true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionReq.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected abstract void init();

    protected abstract void setListener();

    public boolean isInitialized() {
        return isInitialized;
    }

    protected PlayService getPlayService() {
        PlayService playService = AppCache.getPlayService();
        if (playService == null) {
            throw new NullPointerException("play service is null");
        }
        return playService;
    }
}