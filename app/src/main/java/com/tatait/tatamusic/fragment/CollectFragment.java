package com.tatait.tatamusic.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tatait.tatamusic.DB.PlaylistsManager;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.activity.MusicActivity;
import com.tatait.tatamusic.activity.UserInfoActivity;
import com.tatait.tatamusic.adapter.CollectMusicAdapter;
import com.tatait.tatamusic.adapter.OnMoreClickListener;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.enums.LoadStateEnum;
import com.tatait.tatamusic.enums.PlayModeEnum;
import com.tatait.tatamusic.executor.DownloadOnlineMusic;
import com.tatait.tatamusic.executor.DownloadSearchedMusic;
import com.tatait.tatamusic.executor.PlayOnlineMusic;
import com.tatait.tatamusic.executor.ShareOnlineMusic;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.CollectMusic;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.model.OnlineMusic;
import com.tatait.tatamusic.model.SearchMusic;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.MusicUtils;
import com.tatait.tatamusic.utils.NetworkUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.ViewUtils;
import com.tatait.tatamusic.utils.binding.Bind;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 收藏音乐列表
 * Created by Lynn on 2015/11/26.
 */
public class CollectFragment extends BaseFragment implements AdapterView.OnItemClickListener
        , OnMoreClickListener {
    private static final int MUSIC_LIST_SIZE = 100;
    @Bind(R.id.rl_collect_music_list)
    private FrameLayout rl_collect_music_list;
    @Bind(R.id.rl_collect_music_login)
    private RelativeLayout rl_collect_music_login;
    @Bind(R.id.btn_collect_login)
    private Button btn_collect_login;
    @Bind(R.id.tv_collect_empty)
    private TextView tvCollectEmpty;
    @Bind(R.id.swipe_ly)
    private SwipeRefreshLayout mSwipeLayout;
    @Bind(R.id.lv_collect_music_list)
    private ListView listView;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private List<CollectMusic.Collect> mCollectMusicList;
    private CollectMusicAdapter mCollectMusicAdapter;
    private ProgressDialog mProgressDialog;
    private int index = 1;
    private int sum = 0;
    private int total = 0;
    private int lastItemIndex = 0;
    private View view;
    private ArrayList<Music> playlist;
    private PlaylistsManager playlistsManager;
    private Activity mActivity;

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
        view = inflater.inflate(R.layout.fragment_collect_music, container, false);
        return view;
    }

    @Override
    protected void init() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }
        mActivity = getActivity();
        updateUI();
    }

    @Override
    public void setListener() {
        if (Preferences.isLogin()) {
            listView.setOnItemClickListener(this);
            mCollectMusicAdapter.setOnMoreClickListener(this);
        } else {
            btn_collect_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mActivity == null ? getActivity() : mActivity, UserInfoActivity.class);
                    startActivityForResult(intent, 0x00020);
                }
            });
        }
    }

    public void updateUI() {
        if (Preferences.isLogin()) {
            rl_collect_music_list.setVisibility(View.VISIBLE);
            rl_collect_music_login.setVisibility(View.GONE);
            mCollectMusicList = AppCache.getCollectMusicList();
            playlistsManager = PlaylistsManager.getInstance(mActivity.getApplicationContext());
            if (mCollectMusicList.isEmpty()) {
                mProgressDialog = new ProgressDialog(mActivity == null ? getActivity() : mActivity);
                mProgressDialog.setMessage(getString(R.string.loading));
                ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOADING);
                getMusic(true, 1);
            }
            if (mCollectMusicAdapter == null) {
                mCollectMusicAdapter = new CollectMusicAdapter(mCollectMusicList);
            }

            // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
            // 设置下拉进度的背景颜色，默认就是白色的
            mSwipeLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
            // 设置下拉进度的主题颜色
            mSwipeLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
            //设置在上下拉刷新的监听
            mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOADING);
                    mHandler.sendEmptyMessageDelayed(0x909, 500);
                }
            });
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                            && lastItemIndex == mCollectMusicAdapter.getCount() - 1) {
                        ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOADING);
                        mHandler.sendEmptyMessageDelayed(0x910, 500);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    lastItemIndex = firstVisibleItem + visibleItemCount - 1;
                }
            });
            listView.setAdapter(mCollectMusicAdapter);
            updateView();
        } else {
            rl_collect_music_list.setVisibility(View.GONE);
            rl_collect_music_login.setVisibility(View.VISIBLE);
        }
    }

    private void updateView() {
        if (mCollectMusicList != null && mCollectMusicList.isEmpty()) {
            tvCollectEmpty.setVisibility(View.VISIBLE);
        } else {
            tvCollectEmpty.setVisibility(View.GONE);
        }
        if (mCollectMusicAdapter != null) {
            mCollectMusicAdapter.updatePlayingPosition(getPlayService());
            mCollectMusicAdapter.notifyDataSetChanged();
        }
    }

    private void getMusic(final boolean isUpRefresh, int mindex) {
        HttpClient.getCollectMusic(mindex, MUSIC_LIST_SIZE, new HttpCallback<CollectMusic>() {
            @Override
            public void onSuccess(CollectMusic response) {
                mSwipeLayout.setRefreshing(false);
                if (response == null) {
                    ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                } else {
                    ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                }
                mCollectMusicAdapter.notifyDataSetChanged();
                if (isUpRefresh) {
                    mCollectMusicList.clear();
                    //为了保险起见可以先判断当前是否在刷新中（旋转的小圈圈在旋转）....
                    if (mSwipeLayout.isRefreshing()) {
                        //关闭刷新动画
                        mSwipeLayout.setRefreshing(false);
                    }
                }
                index = Integer.parseInt(response.getPage() == null ? "1" : response.getPage());
                total = Integer.parseInt(response.getTotal() == null ? "0" : response.getTotal());
                // 对全部曲库的未来支持做铺垫
                sum = Integer.parseInt(response.getSum() == null ? "0" : response.getSum());
                if (response.getData() != null) {
                    mCollectMusicList.addAll(response.getData());
                }
                updateView();
            }

            @Override
            public void onFail(Exception e) {
                mSwipeLayout.setRefreshing(false);
                if (e instanceof RuntimeException) {
                    return;
                }
                ToastUtils.show(R.string.load_fail);
                updateView();
            }
        });
    }

    public void onItemPlay() {
        updateView();
        if (getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            listView.smoothScrollToPosition(getPlayService().getPlayingPosition());
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
        // 当前播放列表是收藏列表
        if (Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())) { // 判断是否是收藏列表
            if (getPlayService() != null) {
                Music playMusic = getPlayService().getPlayingMusic();
                // 还未播放则正常播放;当前歌曲不是已经在播放了的歌曲则正常播放
                if ((playMusic != null && mCollectMusicList != null && mCollectMusicList.get(position) != null &&
                        Long.parseLong(mCollectMusicList.get(position).getSong_id()) != playMusic.getSongId()) || !getPlayService().isPlaying()) {
                    CollectMusic.Collect collect = mCollectMusicList.get(position);
                    OnlineMusic song = new OnlineMusic();
                    song.setTing_uid(collect.getTing_uid());
                    song.setArtist_name(collect.getArtist_name());
                    song.setAlbum_title(collect.getAlbum_title());
                    song.setTitle(collect.getTitle());
                    song.setPic_big(collect.getPic_big());
                    song.setSong_id(collect.getSong_id());
                    song.setLrclink(collect.getLrclink());
                    song.setPic_small(collect.getPic_small());
                    new PlayOnlineMusic(mActivity == null ? getActivity() : mActivity, song) {
                        @Override
                        public void onPrepare() {
                            if (mProgressDialog != null && !mProgressDialog.isShowing())
                                mProgressDialog.show();
                        }

                        @Override
                        public void onExecuteSuccess(Music music) {
                            if (mProgressDialog != null)
                                mProgressDialog.cancel();
                            if (getPlayService() != null)
                                getPlayService().playPosition(music, position);
                            ToastUtils.show(getString(R.string.now_play, music.getTitle()));
                        }

                        @Override
                        public void onExecuteFail(Exception e) {
                            if (mProgressDialog != null)
                                mProgressDialog.cancel();
                            ToastUtils.show(R.string.unable_to_play);
                        }
                    }.execute();
                }
            } else {
                throw new NullPointerException("play service is null");
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(R.string.confirm_change_list);
            dialog.setPositiveButton(R.string.change_list, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Preferences.saveListType(Actions.LIST_TYPE_COLLECT);
                    ((MusicActivity) (mActivity == null ? getActivity() : mActivity)).resetPlayListFragment();

                    playMusicByPosition(true, position);
                    new loadSongs().execute();
                }
            });
            dialog.setNegativeButton(R.string.cancel, null);
            dialog.show();
        }
    }

    public void playNext(MusicActivity activity) {
        if (mActivity == null) mActivity = activity;
        int collectPosition;
        if (!"-1".equals(Preferences.getFinalPosition()) && getPlayService().getPlayingPosition() == 0) {
            collectPosition = Integer.parseInt(Preferences.getFinalPosition());
        } else {
            collectPosition = getPlayService().getPlayingPosition();
        }
        if (Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())) {
            if (mCollectMusicList == null) mCollectMusicList = new ArrayList<>();
            mCollectMusicList = AppCache.getCollectMusicList();
            if (mCollectMusicList.isEmpty()) {
                if (playlistsManager == null)
                    playlistsManager = PlaylistsManager.getInstance(AppCache.getContext());
                ArrayList<Music> mListMusicList = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT, "");
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
        } else {
            if (mCollectMusicList == null) {
                mCollectMusicList = new ArrayList<>();
            }
            if (playlistsManager == null)
                playlistsManager = PlaylistsManager.getInstance(AppCache.getContext());
            ArrayList<Music> mListMusicList = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT, "");
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

        if (mCollectMusicList.isEmpty()) {
            return;
        }
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case SHUFFLE:
                collectPosition = new Random().nextInt(mCollectMusicList.size());
                playMusicByPosition(false, collectPosition);
                break;
            case SINGLE:
                playMusicByPosition(false, collectPosition);
                break;
            case LOOP:
            default:
                playMusicByPosition(false, collectPosition + 1);
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
            new PlayOnlineMusic(mActivity == null ? getActivity() : mActivity, song) {
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

    private class loadSongs extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (playlist == null) playlist = new ArrayList<>();
                // 先清空数据
                playlist.clear();
                playlistsManager.delete(Actions.DB_PLAY_LIST_COLLECT);
                if (mCollectMusicList != null && !mCollectMusicList.isEmpty()) {
                    for (int i = 0; i < mCollectMusicList.size(); i++) {
                        Music music = new Music();
                        CollectMusic.Collect collect = mCollectMusicList.get(i);
                        music.setTitle(collect.getTitle());
                        music.setAlbum(collect.getAlbum_title());
                        music.setPic_big(collect.getPic_big());
                        music.setPic_small(collect.getPic_small());
                        music.setLrclink(collect.getLrclink());
                        music.setTing_uid(collect.getTing_uid());
                        music.setSongId(Long.parseLong(collect.getSong_id()));
                        music.setTitle(collect.getTitle());
                        music.setArtist(collect.getArtist_name());
                        music.setType(Music.Type.ONLINE);
                        playlist.add(music);
                    }
                    // 再统一写入数据库
                    addToSqlList(playlist);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    // 后台将全部数据累计写入数据库
    private void addToSqlList(ArrayList<Music> playlist) {
        if (playlist != null && playlist.size() > 0 && sum > playlist.size()) {
            // 第一遍执行数据
            playlistsManager.insertLists(Actions.DB_PLAY_LIST_COLLECT, playlist);
            // 最大执行 Math.ceil(playlist.size())次数，比如112首歌，则1.12 = 2
            // 循环执行插入数据库
            for (int i = 1; i < Math.ceil((double) sum / MUSIC_LIST_SIZE); i++) {
                final int cur = i;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new addSongsToSql(cur).execute();// 开线程写入数据
                    }
                }, 500 * i);
            }
        } else {
            playlistsManager.insertLists(Actions.DB_PLAY_LIST_COLLECT, playlist);
        }
    }

    // 开线程写入数据
    private class addSongsToSql extends AsyncTask<Void, Void, Void> {
        int nowSize;
        ArrayList<Music> tempList = new ArrayList<>();

        private addSongsToSql(int i) {
            nowSize = i;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HttpClient.getCollectMusic(nowSize + 1, MUSIC_LIST_SIZE, new HttpCallback<CollectMusic>() {
                    @Override
                    public void onSuccess(CollectMusic response) {
                        if (response == null) {
                            return;
                        }
                        if (response.getData() != null) {
                            tempList.clear();
                            for (int i = 0; i < response.getData().size(); i++) {
                                Music music = new Music();
                                CollectMusic.Collect collect = response.getData().get(i);
                                music.setTitle(collect.getTitle());
                                music.setAlbum(collect.getAlbum_title());
                                music.setPic_big(collect.getPic_big());
                                music.setPic_small(collect.getPic_small());
                                music.setLrclink(collect.getLrclink());
                                music.setTing_uid(collect.getTing_uid());
                                music.setSongId(Long.parseLong(collect.getSong_id()));
                                music.setTitle(collect.getTitle());
                                music.setArtist(collect.getArtist_name());
                                music.setType(Music.Type.ONLINE);
                                tempList.add(music);
                            }
                            if (tempList != null && tempList.size() > 0) {
                                playlistsManager.insertLists(Actions.DB_PLAY_LIST_COLLECT, tempList);
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    @Override
    public void onMoreClick(int position) {
        final CollectMusic.Collect onlineMusic = mCollectMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity == null ? getActivity() : mActivity);
        dialog.setTitle(mCollectMusicList.get(position).getTitle());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(onlineMusic.getArtist_name(), onlineMusic.getTitle());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.collect_download : R.array.collect_no_download;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(onlineMusic);
                        break;
                    case 1:// 取消收藏
                        MusicUtils.unCollectMusic(onlineMusic.getSong_id(), mHandler);
                        // 将本地的列表更新
                        playlistsManager.removeItem(Actions.DB_PLAY_LIST_COLLECT, Long.parseLong(onlineMusic.getSong_id()));
                        break;
                    case 2:// 下载
                        download(onlineMusic);
                        break;
                }
            }
        });
        dialog.show();
    }

    private void share(final CollectMusic.Collect onlineMusic) {
        new ShareOnlineMusic(mActivity == null ? getActivity() : mActivity, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
            @Override
            public void onPrepare() {
                if (mProgressDialog != null && !mProgressDialog.isShowing())
                    mProgressDialog.show();
            }

            @Override
            public void onExecuteSuccess(Void aVoid) {
                if (mProgressDialog != null)
                    mProgressDialog.cancel();
            }

            @Override
            public void onExecuteFail(Exception e) {
                if (mProgressDialog != null)
                    mProgressDialog.cancel();
            }
        }.execute();
    }

    private void download(final CollectMusic.Collect collectMusic) {
        if (collectMusic != null && "true".equals(collectMusic.getIs_search())) {
            final SearchMusic.Song song = new SearchMusic.Song();
            song.setArtistname(collectMusic.getArtist_name());
            song.setSongid(collectMusic.getSong_id());
            song.setSongname(collectMusic.getTitle());
            new DownloadSearchedMusic(mActivity == null ? getActivity() : mActivity, song) {
                @Override
                public void onPrepare() {
                    if (mProgressDialog != null && !mProgressDialog.isShowing())
                        mProgressDialog.show();
                }

                @Override
                public void onExecuteSuccess(Void aVoid) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();
                    ToastUtils.show(getString(R.string.now_download, song.getSongname()));
                }

                @Override
                public void onExecuteFail(Exception e) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();
                    ToastUtils.show(R.string.unable_to_download);
                }
            }.execute();
        } else {
            final OnlineMusic onlineMusic = new OnlineMusic();
            onlineMusic.setTing_uid(collectMusic.getTing_uid());
            onlineMusic.setSong_id(collectMusic.getSong_id());
            onlineMusic.setPic_small(collectMusic.getPic_small());
            onlineMusic.setPic_big(collectMusic.getPic_big());
            onlineMusic.setLrclink(collectMusic.getLrclink());
            onlineMusic.setAlbum_title(collectMusic.getAlbum_title());
            onlineMusic.setArtist_name(collectMusic.getArtist_name());
            onlineMusic.setTitle(collectMusic.getTitle());
            new DownloadOnlineMusic(mActivity == null ? getActivity() : mActivity, onlineMusic) {
                @Override
                public void onPrepare() {
                    if (mProgressDialog != null && !mProgressDialog.isShowing())
                        mProgressDialog.show();
                }

                @Override
                public void onExecuteSuccess(Void aVoid) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();
                    ToastUtils.show(getString(R.string.now_download, onlineMusic.getTitle()));
                }

                @Override
                public void onExecuteFail(Exception e) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();
                    ToastUtils.show(R.string.unable_to_download);
                }
            }.execute();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0x909:
                    getMusic(true, 1);
                    break;
                case 0x910:
                    //上拉加载
                    if (index >= total) {
                        ToastUtils.show(R.string.no_more_data);
                        ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                        break;
                    } else {
                        index = index + 1;
                    }
                    getMusic(false, index);
                    break;
                case 0x109:
                    String str = msg.obj == null ? "" : msg.obj.toString();
                    ViewUtils.changeViewState(listView, llLoading, llLoadFail, LoadStateEnum.LOADING);
                    getMusic(true, 0);
                    ToastUtils.show(str);
                    break;
            }
        }

        ;
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0x00020:
                if (resultCode == 0x00011) {
                    updateUI();
                    setListener();
                    getMusic(true, 1);
                    MusicActivity parentActivity = (MusicActivity) (mActivity == null ? getActivity() : mActivity);
                    parentActivity.updateLogin();
                }
                break;
        }
    }
}