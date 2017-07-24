package com.tatait.tatamusic.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.adapter.OnMoreClickListener;
import com.tatait.tatamusic.adapter.SearchMusicAdapter;
import com.tatait.tatamusic.enums.LoadStateEnum;
import com.tatait.tatamusic.executor.DownloadSearchedMusic;
import com.tatait.tatamusic.executor.PlaySearchedMusic;
import com.tatait.tatamusic.executor.ShareOnlineMusic;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.model.OnlineMusic;
import com.tatait.tatamusic.model.SearchMusic;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.MusicUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.ViewUtils;
import com.tatait.tatamusic.utils.binding.Bind;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * SearchMusicActivity
 * Created by Lynn on 2015/12/27.
 */
public class SearchMusicActivity extends BaseActivity implements SearchView.OnQueryTextListener
        , AdapterView.OnItemClickListener, OnMoreClickListener {
    @Bind(R.id.lv_search_music_list)
    private ListView lvSearchMusic;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private List<SearchMusic.Song> mSearchMusicList = new ArrayList<>();
    private SearchMusicAdapter mAdapter = new SearchMusicAdapter(mSearchMusicList);
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_music);

        if (!checkServiceAlive()) {
            return;
        }

        lvSearchMusic.setAdapter(mAdapter);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        ((TextView) llLoadFail.findViewById(R.id.tv_load_fail_text)).setText(R.string.search_empty);
    }

    @Override
    protected void setListener() {
        lvSearchMusic.setOnItemClickListener(this);
        mAdapter.setOnMoreClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_music, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setQueryHint(getString(R.string.search_tips));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        try {
            Field field = searchView.getClass().getDeclaredField("mGoButton");
            field.setAccessible(true);
            ImageView mGoButton = (ImageView) field.get(searchView);
            mGoButton.setImageResource(R.drawable.ic_menu_search);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOADING);
        searchMusic(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void searchMusic(String keyword) {
        HttpClient.searchMusic(keyword, new HttpCallback<SearchMusic>() {
            @Override
            public void onSuccess(SearchMusic response) {
                if (response == null || response.getSong() == null) {
                    ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
                    return;
                }
                ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                mSearchMusicList.clear();
                mSearchMusicList.addAll(response.getSong());
                mAdapter.notifyDataSetChanged();
                lvSearchMusic.requestFocus();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        lvSearchMusic.setSelection(0);
                    }
                });
            }

            @Override
            public void onFail(Exception e) {
                ViewUtils.changeViewState(lvSearchMusic, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        new PlaySearchedMusic(this, mSearchMusicList.get(position)) {
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
    }

    @Override
    public void onMoreClick(int position) {
        final SearchMusic.Song song = mSearchMusicList.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(song.getSongname());
        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(song.getArtistname(), song.getSongname());
        File file = new File(path);
        int itemsId = file.exists() ? R.array.search_music_dialog_no_download : R.array.search_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        share(song);
                        break;
                    case 1:// 收藏
                        collectMusic(song, "true");
                    case 2:// 下载
                        download(song);
                        break;
                }
            }
        });
        dialog.show();
    }

    private void share(SearchMusic.Song song) {
        new ShareOnlineMusic(this, song.getSongname(), song.getSongid()) {
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

    private void collectMusic(SearchMusic.Song song, String isSearch) {
        if ("-1".equals(Preferences.getUid())) {
            ToastUtils.show(R.string.no_login_to_collect);
            startActivity(new Intent(SearchMusicActivity.this, UserInfoActivity.class));
        } else {
            OnlineMusic mOnlineMusic = new OnlineMusic();
            if (song != null) {
                mOnlineMusic.setTitle(song.getSongname());
                mOnlineMusic.setAlbum_title(song.getSongname());
                mOnlineMusic.setArtist_name(song.getArtistname());
                mOnlineMusic.setLrclink("");
                mOnlineMusic.setPic_big("");
                mOnlineMusic.setPic_small("");
                mOnlineMusic.setSong_id(song.getSongid());
                mOnlineMusic.setTing_uid(song.getSongid());
                MusicUtils.collectMusic(mOnlineMusic, isSearch);
            } else {
                ToastUtils.show(R.string.collect_fail_null);
            }
        }
    }

    private void download(final SearchMusic.Song song) {
        new DownloadSearchedMusic(this, song) {
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
    }
}