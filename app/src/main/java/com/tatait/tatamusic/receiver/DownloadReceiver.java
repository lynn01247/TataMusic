package com.tatait.tatamusic.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.utils.ToastUtils;

/**
 * 下载完成广播接收器
 * Created by Lynn on 2015/12/30.
 */
public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        String title = AppCache.getDownloadList().get(id);
        if (!TextUtils.isEmpty(title)) {
            ToastUtils.show(context.getString(R.string.download_success, title));
        }
    }
}