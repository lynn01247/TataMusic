package com.tatait.tatamusic.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tatait.tatamusic.DB.PlaylistsManager;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.adapter.OnMoreClickListener;
import com.tatait.tatamusic.adapter.OnlineMusicAdapter;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.constants.Extras;
import com.tatait.tatamusic.enums.LoadStateEnum;
import com.tatait.tatamusic.executor.DownloadOnlineMusic;
import com.tatait.tatamusic.executor.PlayOnlineMusic;
import com.tatait.tatamusic.executor.ShareOnlineMusic;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.model.OnlineMusic;
import com.tatait.tatamusic.model.OnlineMusicList;
import com.tatait.tatamusic.model.SongListInfo;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.ImageUtils;
import com.tatait.tatamusic.utils.MusicUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.ScreenUtils;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.ViewUtils;
import com.tatait.tatamusic.utils.binding.Bind;
import com.tatait.tatamusic.widget.AutoLoadListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * OnlineMusicActivity
 * Created by Lynn on 2015/12/27.
 */
public class OnlineMusicActivity extends BaseActivity implements OnItemClickListener
        , OnMoreClickListener, AutoLoadListView.OnLoadListener {
    private static final int MUSIC_LIST_SIZE = 20;
    private static final int SQL_LIST_SIZE = 90;

    @Bind(R.id.lv_online_music_list)
    private AutoLoadListView lvOnlineMusic;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;

    private View vHeader;
    private SongListInfo mListInfo;
    private OnlineMusicList mOnlineMusicList;
    private List<OnlineMusic> mMusicList = new ArrayList<>();
    private OnlineMusicAdapter mAdapter = new OnlineMusicAdapter(mMusicList);
    private ProgressDialog mProgressDialog, sqlProgressDialog;
    private int mOffset = 0;
    private PlaylistsManager playlistsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_music);

        if (!checkServiceAlive()) {
            return;
        }
        playlistsManager = PlaylistsManager.getInstance(getApplicationContext());
        mListInfo = (SongListInfo) getIntent().getSerializableExtra(Extras.MUSIC_LIST_TYPE);
        setTitle(mListInfo.getTitle());

        init();
        onLoad();
    }

    private void init() {
        vHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_music_list_header, null);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(150));
        vHeader.setLayoutParams(params);
        lvOnlineMusic.addHeaderView(vHeader, null, false);
        lvOnlineMusic.setAdapter(mAdapter);
        lvOnlineMusic.setOnLoadListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
    }

    @Override
    protected void setListener() {
        lvOnlineMusic.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }

    private void getMusic(final int offset) {
        HttpClient.getSongListInfo(mListInfo.getType(), MUSIC_LIST_SIZE, offset, new HttpCallback<OnlineMusicList>() {
            @Override
            public void onSuccess(OnlineMusicList response) {
                lvOnlineMusic.onLoadComplete();
                mOnlineMusicList = response;
                if (offset == 0 && response == null) {
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                } else if (offset == 0) {
                    initHeader();
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                }
                if (response == null || response.getSong_list() == null || response.getSong_list().size() == 0) {
                    lvOnlineMusic.setEnable(false);
                    return;
                }
                mOffset += MUSIC_LIST_SIZE;
                mMusicList.addAll(response.getSong_list());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception e) {
                lvOnlineMusic.onLoadComplete();
                if (e instanceof RuntimeException) {
                    lvOnlineMusic.setEnable(false); // 歌曲全部加载完成
                    return;
                }
                if (offset == 0) {
                    ViewUtils.changeViewState(lvOnlineMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                } else {
                    ToastUtils.show(R.string.load_fail);
                }
            }
        });
    }

    @Override
    public void onLoad() {
        getMusic(mOffset);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent != null && parent.getAdapter() != null) {
            play((OnlineMusic) parent.getAdapter().getItem(position));
        } else {
            throw new NullPointerException("parent is null");
        }
    }

    @Override
    public void onMoreClick(int position) {
        final OnlineMusic onlineMusic = mMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(mMusicList.get(position).getTitle());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(onlineMusic.getArtist_name(), onlineMusic.getTitle());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.online_music_dialog_without_download : R.array.online_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(onlineMusic);
                        break;
                    case 1:// 查看歌手信息
                        artistInfo(onlineMusic);
                        break;
                    case 2:// 收藏
                        collectMusic(onlineMusic, "false");
                        break;
                    case 3:// 下载
                        download(onlineMusic);
                        break;
                }
            }
        });
        dialog.show();
    }

    private void initHeader() {
        final ImageView ivHeaderBg = (ImageView) vHeader.findViewById(R.id.iv_header_bg);
        final ImageView ivCover = (ImageView) vHeader.findViewById(R.id.iv_cover);
        TextView tvTitle = (TextView) vHeader.findViewById(R.id.tv_title);
        TextView tvAddList = (TextView) vHeader.findViewById(R.id.tv_add_list);
        TextView tvUpdateDate = (TextView) vHeader.findViewById(R.id.tv_update_date);
        TextView tvComment = (TextView) vHeader.findViewById(R.id.tv_comment);
        tvTitle.setText((mOnlineMusicList.getBillboard().getName() == null || "".equals(mOnlineMusicList.getBillboard().getName())) ? getString(R.string.baidu_bang) : mOnlineMusicList.getBillboard().getName());
        tvUpdateDate.setText(getString(R.string.recent_update, mOnlineMusicList.getBillboard().getUpdate_date()));
        tvAddList.setText(getString(R.string.add_list, Integer.valueOf(mOnlineMusicList.getBillboard().getBillboard_songnum()).toString()));
        tvComment.setText((mOnlineMusicList.getBillboard().getComment() == null || "".equals(mOnlineMusicList.getBillboard().getComment())) ? getString(R.string.baidu_bang_no_des) : mOnlineMusicList.getBillboard().getComment());
        if (mOnlineMusicList.getBillboard().getPic_s640() == null || "".equals(mOnlineMusicList.getBillboard().getPic_s640())) {
            ivCover.setImageDrawable(getResources().getDrawable(R.drawable.default_cover));
        } else {
            ImageSize imageSize = new ImageSize(200, 200);
            ImageLoader.getInstance().loadImage(mOnlineMusicList.getBillboard().getPic_s640(), imageSize,
                    ImageUtils.getCoverDisplayOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            ivCover.setImageBitmap(loadedImage);
                            ivHeaderBg.setImageBitmap(ImageUtils.blur(loadedImage));
                        }
                    });
        }
        tvAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_to_list();
            }
        });
    }

    private void add_to_list() {
        final String type = Actions.LIST_TYPE_ONLINE + mListInfo.getType();
        if (!type.equals(Preferences.getListType())) {
            AlertDialog.Builder changeDialog = new AlertDialog.Builder(this);
            changeDialog.setMessage(getString(R.string.confirm_change_list_title, mListInfo.getTitle()));
            changeDialog.setPositiveButton(R.string.change_list, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mMusicList != null && !mMusicList.isEmpty()) {
                        Preferences.saveListType(type);
                        // 再统一写入数据库
                        playlistsManager.delete(Actions.DB_PLAY_LIST_COLLECT);
                        addToSqlList();
                    }
                }
            });
            changeDialog.setNegativeButton(R.string.cancel, null);
            changeDialog.show();
        } else {
            ToastUtils.show(getString(R.string.cur_music_list, mListInfo.getTitle()));
        }
    }

    private void addToSqlList() {
        // 循环执行插入数据库
        if (mOnlineMusicList != null && mOnlineMusicList.getBillboard() != null && Integer.parseInt(mOnlineMusicList.getBillboard().getBillboard_songnum()) > 0) {
            sqlProgressDialog = new ProgressDialog(this);
            sqlProgressDialog.setMessage(getString(R.string.loading_list));
            sqlProgressDialog.show();
            int sum = Integer.parseInt(mOnlineMusicList.getBillboard().getBillboard_songnum());
            final int limit = (int) (Math.ceil((double) sum / SQL_LIST_SIZE));
            for (int i = 0; i < limit; i++) {
                final int cur = i;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new addSongsToSql(cur, limit).execute();// 开线程写入数据
                    }
                }, 500*i);
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (sqlProgressDialog != null && sqlProgressDialog.isShowing())
                        sqlProgressDialog.cancel();
                }
            }, 60000);
        }

    }

    // 开线程写入数据
    private class addSongsToSql extends AsyncTask<Void, Void, Void> {
        int nowSize, limit;
        ArrayList<Music> tempList = new ArrayList<>();

        private addSongsToSql(int i, int j) {
            nowSize = i;
            limit = j;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                HttpClient.getSongListInfo(mListInfo.getType(), SQL_LIST_SIZE, nowSize * SQL_LIST_SIZE, new HttpCallback<OnlineMusicList>() {
                    @Override
                    public void onSuccess(OnlineMusicList response) {

                        if (response == null) {
                            return;
                        }

                        if (response.getSong_list() != null) {
                            tempList.clear();
                            for (int i = 0; i < response.getSong_list().size(); i++) {
                                Music music = new Music();
                                OnlineMusic onlineMusic = response.getSong_list().get(i);
                                music.setTitle(onlineMusic.getTitle());
                                music.setAlbum(onlineMusic.getAlbum_title());
                                music.setPic_big(onlineMusic.getPic_big());
                                music.setPic_small(onlineMusic.getPic_small());
                                music.setLrclink(onlineMusic.getLrclink());
                                music.setTing_uid(onlineMusic.getTing_uid());
                                music.setSongId(Long.parseLong(onlineMusic.getSong_id()));
                                music.setTitle(onlineMusic.getTitle());
                                music.setArtist(onlineMusic.getArtist_name());
                                music.setType(Music.Type.ONLINE);
                                tempList.add(music);
                            }
                            if (tempList != null && tempList.size() > 0) {
                                playlistsManager.insertLists(Actions.DB_PLAY_LIST_COLLECT, tempList);
                            }
                        }
                        if (nowSize == limit - 1) {
                            if (sqlProgressDialog != null)
                                sqlProgressDialog.cancel();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int size = playlistsManager.getMusicSize(Actions.DB_PLAY_LIST_COLLECT);
                                    if (size == Integer.parseInt(mOnlineMusicList.getBillboard().getBillboard_songnum())) {
                                        ToastUtils.show(R.string.change_list_success);
                                    } else {
                                        ToastUtils.show(getString(R.string.get_music_data_size, size + ""));
                                    }
                                }
                            }, 1000);
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

    private void play(OnlineMusic onlineMusic) {
        if (onlineMusic != null) {
            new PlayOnlineMusic(this, onlineMusic) {
                @Override
                public void onPrepare() {
                    if (mProgressDialog != null && !mProgressDialog.isShowing())
                        mProgressDialog.show();
                }

                @Override
                public void onExecuteSuccess(Music music) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();
                    getPlayService().play(music);
                    ToastUtils.show(getString(R.string.now_play, music.getTitle()));
                }

                @Override
                public void onExecuteFail(Exception e) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();
                    ToastUtils.show(R.string.unable_to_play);
                }
            }.execute();
        } else {
            throw new NullPointerException("onlineMusic is null");
        }
    }

    private void share(final OnlineMusic onlineMusic) {
        new ShareOnlineMusic(this, onlineMusic.getTitle(), onlineMusic.getSong_id()) {
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

    private void artistInfo(OnlineMusic onlineMusic) {
        ArtistInfoActivity.start(this, onlineMusic.getTing_uid());
    }

    private void collectMusic(OnlineMusic onlineMusic, String isSearch) {
        if ("-1".equals(Preferences.getUid())) {
            ToastUtils.show(R.string.no_login_to_collect);
            startActivity(new Intent(OnlineMusicActivity.this, UserInfoActivity.class));
        } else {
            if (onlineMusic != null) {
                MusicUtils.collectMusic(onlineMusic, isSearch);
            } else {
                ToastUtils.show(R.string.collect_fail_null);
            }
        }
    }

    private void download(final OnlineMusic onlineMusic) {
        new DownloadOnlineMusic(this, onlineMusic) {
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