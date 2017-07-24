package com.tatait.tatamusic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.tatait.tatamusic.DB.PlaylistsManager;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.application.Notifier;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.enums.PlayModeEnum;
import com.tatait.tatamusic.model.CollectMusic;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.receiver.NoisyAudioStreamReceiver;
import com.tatait.tatamusic.utils.MusicUtils;
import com.tatait.tatamusic.utils.Preferences;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * 音乐播放后台服务
 * Created by Lynn on 2015/11/27.
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = "Service";
    private static final long TIME_UPDATE = 100L;
    private List<Music> mMusicList;
    private List<CollectMusic.Collect> mCollectMusicList;
    private MediaPlayer mPlayer = new MediaPlayer();
    private IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private Handler mHandler = new Handler();
    private AudioManager mAudioManager;
    private OnPlayerEventListener mListener;
    // 正在播放的歌曲[本地|网络]
    private Music mPlayingMusic;
    // 正在播放的本地歌曲的序号
    private int mPlayingPosition;
    private boolean isPausing;
    private boolean isPreparing;
    private long quitTimerRemain;
    private PlaylistsManager playlistsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicList = AppCache.getMusicList();
        mCollectMusicList = AppCache.getCollectMusicList();
        playlistsManager = PlaylistsManager.getInstance(AppCache.getContext());
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mPlayer.setOnCompletionListener(this);
        Notifier.init(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Actions.ACTION_MEDIA_PLAY_PAUSE:
                    playPause();
                    break;
                case Actions.ACTION_MEDIA_NEXT:
                    next();
                    break;
                case Actions.ACTION_MEDIA_PREVIOUS:
                    prev();
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    /**
     * 每次启动时扫描音乐
     */
    public void updateMusicList() {
        MusicUtils.scanMusic(this, mMusicList);
        int finalPosition = Integer.parseInt(Preferences.getFinalPosition());
        if (finalPosition == -1) {
            finalPosition = mPlayingPosition;
        }
        if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) {
            if (!mMusicList.isEmpty()) {
                updatePlayingPosition();
                if (finalPosition >= mMusicList.size()) finalPosition = mMusicList.size() - 1;
                mPlayingMusic = (mPlayingMusic == null) ? mMusicList.get(finalPosition) : mPlayingMusic;
            }
        } else{
            if (!mCollectMusicList.isEmpty()) {
                updatePlayingPosition();
                if (finalPosition >= mCollectMusicList.size())
                    finalPosition = mCollectMusicList.size() - 1;
                CollectMusic.Collect collect = mCollectMusicList.get(finalPosition);
                Music music = new Music();
                music.setType(Music.Type.ONLINE);
                music.setSongId(Long.parseLong(collect.getSong_id()));
                music.setArtist(collect.getArtist_name());
                music.setTitle(collect.getTitle());
                music.setTitle(collect.getTitle());
                music.setAlbum(collect.getAlbum_title());
                mPlayingMusic = (mPlayingMusic == null) ? music : mPlayingMusic;
            } else {
                if (playlistsManager == null)
                    playlistsManager = PlaylistsManager.getInstance(AppCache.getContext());
                List<Music> playlist = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT, "");
                updatePlayingPosition();
                if (!playlist.isEmpty()) {
                    if (playlist.size() <= finalPosition) finalPosition = playlist.size() - 1;
                    mPlayingMusic = (mPlayingMusic == null) ? playlist.get(finalPosition) : mPlayingMusic;
                }
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    public void setOnPlayEventListener(OnPlayerEventListener listener) {
        mListener = listener;
    }

    public void play(int position) {
        if (mMusicList.isEmpty()) {
            return;
        }

        if (position < 0) {
            position = mMusicList.size() - 1;
        } else if (position >= mMusicList.size()) {
            position = 0;
        }

        Music music = mMusicList.get(position);
        Preferences.saveCurrentSongId(music.getSongId());
        playPosition(music, position);
    }

    public void playPosition(Music music, int position) {
        mPlayingPosition = position;
        // 记录最后一个位置，扫描要用。
        Preferences.saveFinalPosition(Integer.valueOf(position).toString());
        mPlayingMusic = music;
        try {
            mPlayer.reset();
            mPlayer.setDataSource(music.getPath());
            mPlayer.prepareAsync();
            isPreparing = true;
            mPlayer.setOnPreparedListener(mPreparedListener);
            if (mListener != null) {
                mListener.onChange(music);
            }
            Notifier.showPlay(music);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // for online
    public void play(Music music) {
        mPlayingMusic = music;
        try {
            mPlayer.reset();
            mPlayer.setDataSource(music.getPath());
            mPlayer.prepareAsync();
            isPreparing = true;
            mPlayer.setOnPreparedListener(mPreparedListener);
            if (mListener != null) {
                mListener.onChange(music);
            }
            Notifier.showPlay(music);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            isPreparing = false;
            start();
        }
    };

    public void playPause() {
        if (isPreparing()) {
            return;
        }

        if (isPlaying()) {
            pause();
        } else if (isPausing()) {
            resume();
        } else {
            play(getPlayingPosition());
        }
    }

    private void start() {
        mPlayer.start();
        isPausing = false;
        mHandler.post(mBackgroundRunnable);
        Notifier.showPlay(mPlayingMusic);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        registerReceiver(mNoisyReceiver, mNoisyFilter);
    }

    private void pause() {
        if (!isPlaying()) {
            return;
        }

        mPlayer.pause();
        isPausing = true;
        mHandler.removeCallbacks(mBackgroundRunnable);
        Notifier.showPause(mPlayingMusic);
        mAudioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        if (mListener != null) {
            mListener.onPlayerPause();
        }
    }

    private void resume() {
        if (!isPausing()) {
            return;
        }

        start();
        if (mListener != null) {
            mListener.onPlayerResume();
        }
    }

    public void next() {
        if (mMusicList.isEmpty()) {
            return;
        }
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(mMusicList.size());
                play(mPlayingPosition);
                break;
            case SINGLE:
                play(mPlayingPosition);
                break;
            case LOOP:
            default:
                play(mPlayingPosition + 1);
                break;
        }
    }

    public void prev() {
        if (mMusicList.isEmpty()) {
            return;
        }
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                mPlayingPosition = new Random().nextInt(mMusicList.size());
                play(mPlayingPosition);
                break;
            case SINGLE:
                play(mPlayingPosition);
                break;
            case LOOP:
            default:
                play(mPlayingPosition - 1);
                break;
        }
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            mPlayer.seekTo(msec);
            if (mListener != null) {
                mListener.onPublish(msec);
            }
        }
    }

    public boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    public boolean isPausing() {
        return mPlayer != null && isPausing;
    }

    public boolean isPreparing() {
        return mPlayer != null && isPreparing;
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    /**
     * 获取正在播放的歌曲[本地|网络]
     */
    public Music getPlayingMusic() {
        return mPlayingMusic;
    }

    /**
     * 删除或下载歌曲后刷新正在播放的本地歌曲的序号
     */
    public void updatePlayingPosition() {
        int position = 0;
        long id = Preferences.getCurrentSongId();
        if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) {
            for (int i = 0; i < mMusicList.size(); i++) {
                if (mMusicList.get(i).getSongId() == id) {
                    position = i;
                    break;
                }
            }
            mPlayingPosition = position;
            Preferences.saveCurrentSongId(mMusicList.get(mPlayingPosition).getSongId());
        } else if(Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())){
            if (mCollectMusicList != null && mCollectMusicList.size() > 0) {
                for (int i = 0; i < mCollectMusicList.size(); i++) {
                    if (Long.parseLong(mCollectMusicList.get(i).getSong_id()) == id) {
                        position = i;
                        break;
                    }
                }
                mPlayingPosition = position;
                Preferences.saveCurrentSongId(Long.parseLong(mCollectMusicList.get(mPlayingPosition).getSong_id()));
            }
        }else{
            List<Music> list = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT,"");
            if ( list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getSongId() == id) {
                        position = i;
                        break;
                    }
                }
                mPlayingPosition = position;
                Preferences.saveCurrentSongId(list.get(mPlayingPosition).getSongId());
            }
        }
    }

    private Runnable mBackgroundRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && mListener != null) {
                mListener.onPublish(mPlayer.getCurrentPosition());
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                pause();
                break;
        }
    }

    public void startQuitTimer(long milli) {
        stopQuitTimer();
        if (milli > 0) {
            quitTimerRemain = milli + DateUtils.SECOND_IN_MILLIS;
            mHandler.post(mQuitRunnable);
        } else {
            quitTimerRemain = 0;
            if (mListener != null) {
                mListener.onTimer(quitTimerRemain);
            }
        }
    }

    private void stopQuitTimer() {
        mHandler.removeCallbacks(mQuitRunnable);
    }

    private Runnable mQuitRunnable = new Runnable() {
        @Override
        public void run() {
            quitTimerRemain -= DateUtils.SECOND_IN_MILLIS;
            if (quitTimerRemain > 0) {
                if (mListener != null) {
                    mListener.onTimer(quitTimerRemain);
                }
                mHandler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
            } else {
                AppCache.clearStack();
                stop();
            }
        }
    };

    @Override
    public void onDestroy() {
        AppCache.setPlayService(null);
        super.onDestroy();
    }

    public void stop() {
        pause();
        stopQuitTimer();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        Notifier.cancelAll();
        AppCache.setPlayService(null);
        stopSelf();
    }

    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }
}