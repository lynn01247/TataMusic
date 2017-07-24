package com.tatait.tatamusic.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.adapter.FragmentAdapter;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.constants.Extras;
import com.tatait.tatamusic.executor.NaviMenuExecutor;
import com.tatait.tatamusic.fragment.CollectFragment;
import com.tatait.tatamusic.fragment.LocalMusicFragment;
import com.tatait.tatamusic.fragment.PlayFragment;
import com.tatait.tatamusic.fragment.PlayListFragment;
import com.tatait.tatamusic.fragment.SongListFragment;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.receiver.RemoteControlReceiver;
import com.tatait.tatamusic.service.OnPlayerEventListener;
import com.tatait.tatamusic.service.PlayService;
import com.tatait.tatamusic.utils.CoverLoader;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.SystemUtils;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.binding.Bind;
import com.tatait.tatamusic.utils.permission.PermissionReq;
import com.tatait.tatamusic.utils.permission.PermissionResult;
import com.tatait.tatamusic.utils.permission.Permissions;

/**
 * MusicActivity
 * Created by Lynn on 2015/12/27.
 */
public class MusicActivity extends BaseActivity implements View.OnClickListener, OnPlayerEventListener,
        NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    @Bind(R.id.drawer_layout)
    private DrawerLayout drawerLayout;
    @Bind(R.id.navigation_view)
    private NavigationView navigationView;
    @Bind(R.id.iv_menu)
    private ImageView ivMenu;
    @Bind(R.id.iv_search)
    private ImageView ivSearch;
    @Bind(R.id.tv_local_music)
    private TextView tvLocalMusic;
    @Bind(R.id.tv_online_music)
    private TextView tvOnlineMusic;
    @Bind(R.id.tv_collect_music)
    private TextView tvCollectMusic;
    @Bind(R.id.viewpager)
    private ViewPager mViewPager;
    @Bind(R.id.fl_play_bar)
    private FrameLayout flPlayBar;
    @Bind(R.id.iv_play_bar_cover)
    private ImageView ivPlayBarCover;
    @Bind(R.id.tv_play_bar_title)
    private TextView tvPlayBarTitle;
    @Bind(R.id.tv_play_bar_artist)
    private TextView tvPlayBarArtist;
    @Bind(R.id.iv_play_bar_play)
    private ImageView ivPlayBarPlay;
    @Bind(R.id.iv_play_bar_next)
    private ImageView ivPlayBarNext;
    @Bind(R.id.iv_play_bar_list)
    private ImageView ivPlayBarList;
    @Bind(R.id.pb_play_bar)
    private ProgressBar mProgressBar;
    private View vNavigationHeader;
    private LocalMusicFragment mLocalMusicFragment;
    private CollectFragment mCollectFragment;
    private PlayFragment mPlayFragment;
    private PlayListFragment playListFragment;
    private AudioManager mAudioManager;
    private ComponentName mRemoteReceiver;
    private boolean isPlayFragmentShow = false;
    private boolean isListFragmentShow = false;
    private MenuItem timerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        if (!checkServiceAlive()) {
            return;
        }

        getPlayService().setOnPlayEventListener(this);

        setupView();
        updateLogin(); // updateWeather();
        registerReceiver();
        onChange(getPlayService().getPlayingMusic());
        parseIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    @Override
    protected void setListener() {
        ivMenu.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        tvLocalMusic.setOnClickListener(this);
        tvOnlineMusic.setOnClickListener(this);
        tvCollectMusic.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(this);
        flPlayBar.setOnClickListener(this);
        ivPlayBarPlay.setOnClickListener(this);
        ivPlayBarNext.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        ivPlayBarList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showListFragment();
                    }
                }, 60);

            }
        });
    }

    private void setupView() {
        // add navigation header
        vNavigationHeader = LayoutInflater.from(this).inflate(R.layout.navigation_header, navigationView, false);
        navigationView.addHeaderView(vNavigationHeader);

        // setup view pager
        mLocalMusicFragment = new LocalMusicFragment();
        SongListFragment mSongListFragment = new SongListFragment();
        mCollectFragment = new CollectFragment();
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(mLocalMusicFragment);
        adapter.addFragment(mSongListFragment);
        adapter.addFragment(mCollectFragment);
        mViewPager.setAdapter(adapter);
        tvLocalMusic.setSelected(true);

        // touch img
        ImageView userImg = (ImageView) vNavigationHeader.findViewById(R.id.iv_user_icon);
        userImg.setOnClickListener(this);
    }

    private void updateWeather() {
        PermissionReq.with(this)
                .permissions(Permissions.LOCATION)
                .result(new PermissionResult() {
                    @Override
                    public void onGranted() {
//                        new WeatherExecutor(getPlayService(), vNavigationHeader).execute();
                    }

                    @Override
                    public void onDenied() {
                        ToastUtils.show(getString(R.string.no_permission, Permissions.LOCATION_DESC, "更新天气"));
                    }
                })
                .request();
    }

    public void updateLogin() {
        TextView userName = (TextView) vNavigationHeader.findViewById(R.id.tv_user_name);
        ImageView userImg = (ImageView) vNavigationHeader.findViewById(R.id.iv_user_icon);
        if (Preferences.isLogin()) {
            userName.setText(Preferences.getUserName());
            userImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_user_img));
        } else {
            userName.setText(Preferences.getUserName());
            userImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_user_nologin_img));
        }
    }

    private void registerReceiver() {
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mRemoteReceiver = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mRemoteReceiver);
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extras.EXTRA_NOTIFICATION)) {
            showPlayingFragment();
            setIntent(new Intent());
        }
    }

    /**
     * 更新播放进度
     */
    @Override
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPublish(progress);
        }
    }

    @Override
    public void onChange(Music music) {
        onPlay(music);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onChange(music);

            updateApdater();
        }
    }

    @Override
    public void onPlayerPause() {
        ivPlayBarPlay.setSelected(false);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPlayerPause();
        }
    }

    @Override
    public void onPlayerResume() {
        ivPlayBarPlay.setSelected(true);
        if (mPlayFragment != null && mPlayFragment.isInitialized()) {
            mPlayFragment.onPlayerResume();
        }
    }

    @Override
    public void onTimer(long remain) {
        if (timerItem == null) {
            timerItem = navigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        timerItem.setTitle(remain == 0 ? title : SystemUtils.formatTime(title + "(mm:ss)", remain));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.iv_search:
                startActivity(new Intent(this, SearchMusicActivity.class));
                break;
            case R.id.tv_local_music:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.tv_online_music:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.tv_collect_music:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.fl_play_bar:
                showPlayingFragment();
                break;
            case R.id.iv_play_bar_play:
                play();
                break;
            case R.id.iv_play_bar_next:
                next();
                break;
            case R.id.iv_user_icon:
                if (Preferences.isLogin()) {
                    AlertDialog.Builder cleanDialog = new AlertDialog.Builder(this);
                    cleanDialog.setMessage(getResources().getString(R.string.confirm_logoff));
                    cleanDialog.setPositiveButton(R.string.logoff, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastUtils.show(R.string.has_logoff);
                            Preferences.saveUid("-1");
                            Preferences.saveUserName(getResources().getString(R.string.not_login));
                            Preferences.saveIsLogin(false);
                            if (mCollectFragment == null) {
                                mCollectFragment = new CollectFragment();
                            }
                            mCollectFragment.updateUI();
                            mCollectFragment.setListener();
                            updateLogin();
                        }
                    });
                    cleanDialog.setNegativeButton(R.string.cancel, null);
                    cleanDialog.show();
                } else {
                    startActivityForResult(new Intent(this, UserInfoActivity.class), 0x00010);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x00010 && resultCode == 0x00011) {
            updateLogin();
        }
        if (requestCode == 0x00020 && resultCode == 0x00011) {
            updateLogin();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        drawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
        return NaviMenuExecutor.onNavigationItemSelected(item, this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            tvLocalMusic.setSelected(true);
            tvOnlineMusic.setSelected(false);
            tvCollectMusic.setSelected(false);
        } else if (position == 1) {
            tvLocalMusic.setSelected(false);
            tvOnlineMusic.setSelected(true);
            tvCollectMusic.setSelected(false);
        } else if (position == 2) {
            tvLocalMusic.setSelected(false);
            tvOnlineMusic.setSelected(false);
            tvCollectMusic.setSelected(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void onPlay(Music music) {
        if (music == null) {
            return;
        }

        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
        ivPlayBarCover.setImageBitmap(cover);
        tvPlayBarTitle.setText(music.getTitle());
        tvPlayBarArtist.setText(music.getArtist());
        if (getPlayService().isPlaying() || getPlayService().isPreparing()) {
            ivPlayBarPlay.setSelected(true);
        } else {
            ivPlayBarPlay.setSelected(false);
        }
        mProgressBar.setMax((int) music.getDuration());
        mProgressBar.setProgress(0);

        if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) {
            if (mLocalMusicFragment != null && mLocalMusicFragment.isInitialized()) {
                mLocalMusicFragment.onItemPlay();
            }
        } else {
            if (mCollectFragment != null && mCollectFragment.isInitialized()) {
                mCollectFragment.onItemPlay();
            }
        }
    }

    private void play() {
        getPlayService().playPause();
    }

    private void next() {
        if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) { // 首页的下一首按钮：区分本地和非本地
            updateApdater();

            getPlayService().next(); //本地的next()
        } else {
            updateApdater();

            if (mCollectFragment != null) mCollectFragment.playNext(this);
        }
    }

    public void updateApdater() {
        if (playListFragment != null) {
            playListFragment.updateApdater();
        }
    }

    private void showPlayingFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.add(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = true;
    }

    private void hidePlayingFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        ft.hide(mPlayFragment);
        ft.commitAllowingStateLoss();
        isPlayFragmentShow = false;
    }

    public void showListFragment() {
        if (isListFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (playListFragment == null) {
            playListFragment = new PlayListFragment();
            ft.add(android.R.id.content, playListFragment);
        } else {
            ft.show(playListFragment);
        }
        ft.commitAllowingStateLoss();
        isListFragmentShow = true;
    }

    private void hideListFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.fragment_slide_down);
        if (isPlayFragmentShow) {
            ft.show(mPlayFragment);
        }
        ft.hide(playListFragment);
        ft.commitAllowingStateLoss();
        isListFragmentShow = false;
    }

    public void resetPlayListFragment() {
        if (playListFragment != null) {
            playListFragment = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow && playListFragment != null && isListFragmentShow) {
            hideListFragment();
            return;
        } else if (mPlayFragment != null && isPlayFragmentShow) {
            hidePlayingFragment();
            return;
        } else if (playListFragment != null && isListFragmentShow) {
            hideListFragment();
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 切换夜间模式不保存状态
    }

    @Override
    protected void onDestroy() {
        if (mRemoteReceiver != null) {
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteReceiver);
        }
        PlayService service = AppCache.getPlayService();
        if (service != null) {
            service.setOnPlayEventListener(null);
        }
        super.onDestroy();
    }

    /**
     * 退出事件
     */
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isListFragmentShow) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(0, R.anim.fragment_slide_down);
                ft.hide(playListFragment);
                ft.commitAllowingStateLoss();
                isListFragmentShow = false;
            } else if (isPlayFragmentShow) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(0, R.anim.fragment_slide_down);
                ft.hide(mPlayFragment);
                ft.commitAllowingStateLoss();
                isPlayFragmentShow = false;
            } else if (System.currentTimeMillis() - exitTime > 2000) {
                ToastUtils.show(R.string.press_exit);
                exitTime = System.currentTimeMillis();
            } else {
                // 程序退出
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}