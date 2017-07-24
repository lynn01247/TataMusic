package com.tatait.tatamusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.service.PlayService;

/**
 * 耳机线控
 * Created by Lynn on 2016/1/21.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
            return;
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                PlayService.startCommand(context, Actions.ACTION_MEDIA_PLAY_PAUSE);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                PlayService.startCommand(context, Actions.ACTION_MEDIA_NEXT);
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                PlayService.startCommand(context, Actions.ACTION_MEDIA_PREVIOUS);
                break;
        }
    }
}