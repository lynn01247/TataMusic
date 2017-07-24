package com.tatait.tatamusic.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.Splash;
import com.tatait.tatamusic.service.PlayService;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.binding.Bind;
import com.tatait.tatamusic.utils.permission.PermissionReq;
import com.tatait.tatamusic.utils.permission.PermissionResult;
import com.tatait.tatamusic.utils.permission.Permissions;

import java.io.File;
import java.util.Calendar;

/**
 * SplashActivity
 * Created by Lynn on 2015/12/27.
 */
public class SplashActivity extends BaseActivity {
    private static final String SPLASH_FILE_NAME = "splash";

    @Bind(R.id.iv_splash)
    private ImageView ivSplash;
    @Bind(R.id.tv_copyright)
    private TextView tvCopyright;
    private ServiceConnection mPlayServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        tvCopyright.setText(getString(R.string.copyright, year));

        checkService();
    }

    private void checkService() {
        if (AppCache.getPlayService() == null) {
            showSplash();
            updateSplash();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindService();
                }
            }, 1000);
        } else {
            startMusicActivity();
            finish();
        }
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, PlayService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final PlayService playService = ((PlayService.PlayBinder) service).getService();
            AppCache.setPlayService(playService);
            PermissionReq.with(SplashActivity.this)
                    .permissions(Permissions.STORAGE)
                    .result(new PermissionResult() {
                        @Override
                        public void onGranted() {
                            playService.updateMusicList();
                            startMusicActivity();
                            finish();
                        }

                        @Override
                        public void onDenied() {
                            ToastUtils.show(getString(R.string.no_permission, Permissions.STORAGE_DESC, "扫描本地歌曲"));
                            finish();
                            playService.stop();
                        }
                    })
                    .request();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private void showSplash() {
        File splashImg = new File(FileUtils.getSplashDir(this), SPLASH_FILE_NAME);
        if (splashImg.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(splashImg.getPath());
            ivSplash.setImageBitmap(bitmap);
        }
    }

    private void updateSplash() {
        HttpClient.getSplash(new HttpCallback<Splash>() {
            @Override
            public void onSuccess(Splash response) {
                if (response == null || TextUtils.isEmpty(response.getUrl())) {
                    return;
                }

                final String url = response.getUrl();
                String lastImgUrl = Preferences.getSplashUrl();
                if (TextUtils.equals(lastImgUrl, url)) {
                    return;
                }

                HttpClient.downloadFile(url, FileUtils.getSplashDir(AppCache.getContext()), SPLASH_FILE_NAME,
                        new HttpCallback<File>() {
                            @Override
                            public void onSuccess(File file) {
                                Preferences.saveSplashUrl(url);
                            }

                            @Override
                            public void onFail(Exception e) {
                            }
                        });
            }

            @Override
            public void onFail(Exception e) {
            }
        });
    }

    private void startMusicActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MusicActivity.class);
        intent.putExtras(getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        if (mPlayServiceConnection != null) {
            unbindService(mPlayServiceConnection);
        }
        super.onDestroy();
    }
}