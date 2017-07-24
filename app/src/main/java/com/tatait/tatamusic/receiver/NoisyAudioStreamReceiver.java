package com.tatait.tatamusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.service.PlayService;

/**
 * 来电/耳机拔出时暂停播放
 * Created by Lynn on 2016/1/23.
 */
public class NoisyAudioStreamReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PlayService.startCommand(context, Actions.ACTION_MEDIA_PLAY_PAUSE);
    }
}