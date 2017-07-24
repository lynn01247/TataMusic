package com.tatait.tatamusic.executor;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.utils.NetworkUtils;
import com.tatait.tatamusic.utils.Preferences;

/**
 * Created by Lynn on 2017/1/20.
 */
public abstract class PlayMusic implements IExecutor<Music> {
    private Activity mActivity;
    protected Music music;
    private int mTotalStep;
    protected int mCounter = 0;

    public PlayMusic(Activity activity, int totalStep) {
        mActivity = activity;
        mTotalStep = totalStep;
    }

    @Override
    public void execute() {
        checkNetwork();
    }

    private void checkNetwork() {
        boolean mobileNetworkPlay = Preferences.enableMobileNetworkPlay();
        if (NetworkUtils.isActiveNetworkMobile(mActivity) && !mobileNetworkPlay) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.tips);
            builder.setMessage(R.string.play_tips);
            builder.setPositiveButton(R.string.play_tips_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Preferences.saveMobileNetworkPlay(true);
                    getPlayInfoWrapper();
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        } else {
            getPlayInfoWrapper();
        }
    }

    private void getPlayInfoWrapper() {
        onPrepare();
        getPlayInfo();
    }

    protected abstract void getPlayInfo();

    protected void checkCounter() {
        mCounter++;
        if (mCounter == mTotalStep) {
            onExecuteSuccess(music);
        }
    }
}