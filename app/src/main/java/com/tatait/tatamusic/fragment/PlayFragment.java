package com.tatait.tatamusic.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tatait.tatamusic.DB.PlaylistsManager;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.activity.MusicActivity;
import com.tatait.tatamusic.adapter.PlayPagerAdapter;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.enums.PlayModeEnum;
import com.tatait.tatamusic.executor.PlayOnlineMusic;
import com.tatait.tatamusic.executor.SearchLrc;
import com.tatait.tatamusic.model.CollectMusic;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.model.OnlineMusic;
import com.tatait.tatamusic.utils.CoverLoader;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.ScreenUtils;
import com.tatait.tatamusic.utils.SystemUtils;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.binding.Bind;
import com.tatait.tatamusic.widget.AlbumCoverView;
import com.tatait.tatamusic.widget.IndicatorLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.wcy.lrcview.LrcView;

/**
 * 正在播放界面
 * Created by Lynn on 2015/11/27.
 */
public class PlayFragment extends BaseFragment implements View.OnClickListener,
        ViewPager.OnPageChangeListener, SeekBar.OnSeekBarChangeListener {
    @Bind(R.id.ll_content)
    private LinearLayout llContent;
    @Bind(R.id.iv_play_page_bg)
    private ImageView ivPlayingBg;
    @Bind(R.id.iv_back)
    private ImageView ivBack;
    @Bind(R.id.tv_title)
    private TextView tvTitle;
    @Bind(R.id.tv_artist)
    private TextView tvArtist;
    @Bind(R.id.vp_play_page)
    private ViewPager vpPlay;
    @Bind(R.id.il_indicator)
    private IndicatorLayout ilIndicator;
    @Bind(R.id.sb_progress)
    private SeekBar sbProgress;
    @Bind(R.id.tv_current_time)
    private TextView tvCurrentTime;
    @Bind(R.id.tv_total_time)
    private TextView tvTotalTime;
    @Bind(R.id.iv_mode)
    private ImageView ivMode;
    @Bind(R.id.iv_play)
    private ImageView ivPlay;
    @Bind(R.id.iv_next)
    private ImageView ivNext;
    @Bind(R.id.iv_prev)
    private ImageView ivPrev;
    @Bind(R.id.iv_list)
    private ImageView ivList;
    private AlbumCoverView mAlbumCoverView;
    private LrcView mLrcViewSingle;
    private LrcView mLrcViewFull;
    private SeekBar sbVolume;
    private AudioManager mAudioManager;
    private List<View> mViewPagerContent;
    private int mLastProgress;
    private View view;
    private PlaylistsManager playlistsManager;
    private int playPosition;
    private List<Music> mListMusicList;
    private List<CollectMusic.Collect> mCollectMusicList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
            return view;
        }
        view = inflater.inflate(R.layout.fragment_play, container, false);
        return view;
    }

    @Override
    protected void init() {
        initSystemBar();
        initViewPager();
        ilIndicator.create(mViewPagerContent.size());
        initPlayMode();
        onChange(getPlayService().getPlayingMusic());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Actions.VOLUME_CHANGED_ACTION);
        getContext().registerReceiver(mVolumeReceiver, filter);
    }

    @Override
    protected void setListener() {
        ivBack.setOnClickListener(this);
        ivMode.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivList.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        sbProgress.setOnSeekBarChangeListener(this);
        sbVolume.setOnSeekBarChangeListener(this);
        vpPlay.setOnPageChangeListener(this);
    }

    /**
     * 沉浸式状态栏
     */
    private void initSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = ScreenUtils.getSystemBarHeight();
            llContent.setPadding(0, top, 0, 0);
        }
    }

    private void initViewPager() {
        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_cover, null);
        View lrcView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_lrc, null);
        mAlbumCoverView = (AlbumCoverView) coverView.findViewById(R.id.album_cover_view);
        mLrcViewSingle = (LrcView) coverView.findViewById(R.id.lrc_view_single);
        mLrcViewFull = (LrcView) lrcView.findViewById(R.id.lrc_view_full);
        sbVolume = (SeekBar) lrcView.findViewById(R.id.sb_volume);
        mAlbumCoverView.initNeedle(getPlayService().isPlaying());
        initVolume();

        mViewPagerContent = new ArrayList<>(2);
        mViewPagerContent.add(coverView);
        mViewPagerContent.add(lrcView);
        vpPlay.setAdapter(new PlayPagerAdapter(mViewPagerContent));
        playlistsManager = PlaylistsManager.getInstance(AppCache.getContext());
    }

    private void initVolume() {
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        sbVolume.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    private void initPlayMode() {
        int mode = Preferences.getPlayMode();
        ivMode.setImageLevel(mode);
    }

    /**
     * 更新播放进度
     */
    public void onPublish(int progress) {
        sbProgress.setProgress(progress);
        if (mLrcViewSingle.hasLrc()) {
            mLrcViewSingle.updateTime(progress);
            mLrcViewFull.updateTime(progress);
        }
        //更新当前播放时间
        if (progress - mLastProgress >= 1000) {
            tvCurrentTime.setText(formatTime(progress));
            mLastProgress = progress;
        }
    }

    public void onChange(Music music) {
        onPlay(music);
    }

    public void onPlayerPause() {
        ivPlay.setSelected(false);
        mAlbumCoverView.pause();
    }

    public void onPlayerResume() {
        ivPlay.setSelected(true);
        mAlbumCoverView.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_mode:
                switchPlayMode();
                break;
            case R.id.iv_play:
                play();
                break;
            case R.id.iv_next:
                next();
                break;
            case R.id.iv_prev:
                prev();
                break;
            case R.id.iv_list:
                ((MusicActivity) getActivity()).showListFragment();
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        ilIndicator.setCurrent(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == sbProgress) {
            if (getPlayService().isPlaying() || getPlayService().isPausing()) {
                int progress = seekBar.getProgress();
                getPlayService().seekTo(progress);
                mLrcViewSingle.onDrag(progress);
                mLrcViewFull.onDrag(progress);
                tvCurrentTime.setText(formatTime(progress));
                mLastProgress = progress;
            } else {
                seekBar.setProgress(0);
            }
        } else if (seekBar == sbVolume) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(),
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
    }

    private void onPlay(Music music) {
        if (music == null) {
            return;
        }
        tvTitle.setText(music.getTitle());
        tvArtist.setText(music.getArtist());
        sbProgress.setMax((int) music.getDuration());
        sbProgress.setProgress(0);
        mLastProgress = 0;
        tvCurrentTime.setText(R.string.play_time_start);
        tvTotalTime.setText(formatTime(music.getDuration()));
        setCoverAndBg(music);
        setLrc(music);
        if (getPlayService().isPlaying() || getPlayService().isPreparing()) {
            ivPlay.setSelected(true);
            mAlbumCoverView.start();
        } else {
            ivPlay.setSelected(false);
            mAlbumCoverView.pause();
        }
    }

    private void play() {
        getPlayService().playPause();
    }

    private void next() {
        if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) {
            ((MusicActivity) getActivity()).updateApdater();

            getPlayService().next(); //本地的next()
        }else{
            playNext();
            ((MusicActivity) getActivity()).updateApdater();
        }
    }

    private void prev() {
        if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) {
            ((MusicActivity) getActivity()).updateApdater();

            getPlayService().prev();     //本地的prev()
        } else{
            playPrev();
            ((MusicActivity) getActivity()).updateApdater();
        }
    }

    public void playNext() {
        if (!"-1".equals(Preferences.getFinalPosition()) && getPlayService().getPlayingPosition() == 0) {
            playPosition = Integer.parseInt(Preferences.getFinalPosition());
        } else {
            playPosition = getPlayService().getPlayingPosition();
        }
        if(Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())){
            mCollectMusicList = AppCache.getCollectMusicList();
        }else{
            if(mCollectMusicList == null){
                mCollectMusicList = new ArrayList<>();
            }else if(!mCollectMusicList.isEmpty()) {
                mCollectMusicList.clear();
            }
        }
        if (mCollectMusicList != null && mCollectMusicList.isEmpty()) {
            if (playlistsManager == null)
                playlistsManager = PlaylistsManager.getInstance(AppCache.getContext());
            mListMusicList = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT, "");
            if (!mListMusicList.isEmpty()) {
                for (int i = 0; i < mListMusicList.size(); i++) {
                    CollectMusic.Collect collect = new CollectMusic.Collect();
                    Music music = mListMusicList.get(i);
                    collect.setTing_uid(music.getTing_uid());
                    collect.setArtist_name(music.getArtist());
                    collect.setAlbum_title(music.getAlbum());
                    collect.setTitle(music.getTitle());
                    collect.setPic_big(music.getPic_big());
                    collect.setSong_id(Long.valueOf(music.getSongId()).toString());
                    collect.setLrclink(music.getLrclink());
                    collect.setPic_small(music.getPic_small());
                    mCollectMusicList.add(collect);
                }
            }
        }
        if (mCollectMusicList != null && mCollectMusicList.isEmpty()) {
            return;
        }
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                playPosition = new Random().nextInt(mCollectMusicList.size());
                playMusicByPosition(false, playPosition);
                break;
            case SINGLE:
                playMusicByPosition(false, playPosition);
                break;
            case LOOP:
            default:
                playMusicByPosition(false, playPosition + 1);
                break;
        }
    }

    public void playPrev() {
        if (!"-1".equals(Preferences.getFinalPosition()) && getPlayService().getPlayingPosition() == 0) {
            playPosition = Integer.parseInt(Preferences.getFinalPosition());
        } else {
            playPosition = getPlayService().getPlayingPosition();
        }
        if(Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())){
            mCollectMusicList = AppCache.getCollectMusicList();
        }else{
            if(mCollectMusicList !=null && !mCollectMusicList.isEmpty())mCollectMusicList.clear();
        }
        if (mCollectMusicList != null && mCollectMusicList.isEmpty()) {
            if (playlistsManager == null)
                playlistsManager = PlaylistsManager.getInstance(AppCache.getContext());
            mListMusicList = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT, "");
            if (!mListMusicList.isEmpty()) {
                for (int i = 0; i < mListMusicList.size(); i++) {
                    CollectMusic.Collect collect = new CollectMusic.Collect();
                    Music music = mListMusicList.get(i);
                    collect.setTing_uid(music.getTing_uid());
                    collect.setArtist_name(music.getArtist());
                    collect.setAlbum_title(music.getAlbum());
                    collect.setTitle(music.getTitle());
                    collect.setPic_big(music.getPic_big());
                    collect.setSong_id(Long.valueOf(music.getSongId()).toString());
                    collect.setLrclink(music.getLrclink());
                    collect.setPic_small(music.getPic_small());
                    mCollectMusicList.add(collect);
                }
            }
        }
        if (mCollectMusicList != null && mCollectMusicList.isEmpty()) {
            return;
        }
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                playPosition = new Random().nextInt(mCollectMusicList.size());
                playMusicByPosition(false, playPosition);
                break;
            case SINGLE:
                playMusicByPosition(false, playPosition);
                break;
            case LOOP:
            default:
                playMusicByPosition(false, playPosition - 1);
                break;
        }
    }

    public void playMusicByPosition(final boolean isChangeList, int position) {
        final int newPosition;
        if (position < 0) {
            position = mCollectMusicList.size() - 1;
        } else if (position >= mCollectMusicList.size()) {
            position = 0;
        }
        newPosition = position;
        CollectMusic.Collect collect = mCollectMusicList.get(newPosition);
        if (collect != null) {
            OnlineMusic song = new OnlineMusic();
            song.setTing_uid(collect.getTing_uid());
            song.setArtist_name(collect.getArtist_name());
            song.setAlbum_title(collect.getAlbum_title());
            song.setTitle(collect.getTitle());
            song.setPic_big(collect.getPic_big());
            song.setSong_id(collect.getSong_id());
            song.setLrclink(collect.getLrclink());
            song.setPic_small(collect.getPic_small());
            new PlayOnlineMusic(getActivity(), song) {
                @Override
                public void onPrepare() {
                }

                @Override
                public void onExecuteSuccess(Music music) {
                    if (isChangeList) ToastUtils.show(R.string.change_list_success);
                    if (getPlayService() != null) getPlayService().playPosition(music, newPosition);
                }

                @Override
                public void onExecuteFail(Exception e) {
                }
            }.execute();
        }
    }

    private void switchPlayMode() {
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case LOOP:
                mode = PlayModeEnum.SHUFFLE;
                ToastUtils.show(R.string.mode_shuffle);
                break;
            case SHUFFLE:
                mode = PlayModeEnum.SINGLE;
                ToastUtils.show(R.string.mode_one);
                break;
            case SINGLE:
                mode = PlayModeEnum.LOOP;
                ToastUtils.show(R.string.mode_loop);
                break;
        }
        Preferences.savePlayMode(mode.value());
        initPlayMode();
    }

    private void onBackPressed() {
        getActivity().onBackPressed();
        ivBack.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivBack.setEnabled(true);
            }
        }, 300);
    }

    private void setCoverAndBg(Music music) {
        mAlbumCoverView.setCoverBitmap(CoverLoader.getInstance().loadRound(music));
        ivPlayingBg.setImageBitmap(CoverLoader.getInstance().loadBlur(music));
    }

    private void setLrc(final Music music) {
        if (music.getType() == Music.Type.LOCAL) {
            String lrcPath = FileUtils.getLrcFilePath(music);
            if (!TextUtils.isEmpty(lrcPath)) {
                loadLrc(lrcPath);
            } else {
                new SearchLrc(music.getArtist(), music.getTitle()) {
                    @Override
                    public void onPrepare() {
                        vpPlay.setTag(music);// 设置tag防止歌词下载完成后已切换歌曲

                        loadLrc("");
                        setLrcLabel(getString(R.string.searching_lrc));
                    }

                    @Override
                    public void onExecuteSuccess(@NonNull String lrcPath) {
                        if (vpPlay.getTag() != music) {
                            return;
                        }
                        vpPlay.setTag(null);// 清除tag

                        loadLrc(lrcPath);
                        setLrcLabel(getString(R.string.no_lrc));
                    }

                    @Override
                    public void onExecuteFail(Exception e) {
                        if (vpPlay.getTag() != music) {
                            return;
                        }
                        vpPlay.setTag(null); // 清除tag
                        setLrcLabel(getString(R.string.no_lrc));
                    }
                }.execute();
            }
        } else {
            String lrcPath = FileUtils.getLrcDir() + FileUtils.getLrcFileName(music.getArtist(), music.getTitle());
            loadLrc(lrcPath);
        }
    }

    private void loadLrc(String path) {
        File file = new File(path);
        mLrcViewSingle.loadLrc(file);
        mLrcViewFull.loadLrc(file);
    }

    private void setLrcLabel(String label) {
        mLrcViewSingle.setLabel(label);
        mLrcViewFull.setLabel(label);
    }

    private String formatTime(long time) {
        return SystemUtils.formatTime("mm:ss", time);
    }

    private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sbVolume.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    };

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mVolumeReceiver);
        super.onDestroy();
    }
}