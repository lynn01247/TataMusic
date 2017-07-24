package com.tatait.tatamusic.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tatait.tatamusic.DB.PlaylistsManager;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.adapter.OnMoreClickListener;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.executor.DownloadOnlineMusic;
import com.tatait.tatamusic.executor.PlayOnlineMusic;
import com.tatait.tatamusic.model.CollectMusic;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.model.OnlineMusic;
import com.tatait.tatamusic.service.DividerItemDecoration;
import com.tatait.tatamusic.service.HandlerUtil;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.ScreenUtils;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.binding.Bind;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Lynn on 2016/2/4.
 */
public class PlayListFragment extends BaseFragment implements View.OnClickListener {
    @Bind(R.id.ll_content)
    private LinearLayout llContent;
    @Bind(R.id.iv_back)
    private ImageView iv_back;
    @Bind(R.id.playlist_local)
    private TextView playlist_local;
    @Bind(R.id.play_list_number)
    private TextView playlistNumber;
    @Bind(R.id.playlist_clear_all)
    private TextView clearAll;
    @Bind(R.id.play_list)
    private RecyclerView recyclerView;

    private PlaylistAdapter adapter;
    private List<Music> mMusicList;
    private List<CollectMusic.Collect> mCollectMusicList;
    private ArrayList<Music> playlist;
    private PlaylistsManager playlistsManager;
    private Handler mHandler;
    private Context mContext;
    private View view;
    private ProgressDialog mProgressDialog;

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

        view = inflater.inflate(R.layout.fragment_queue, container, false);
        return view;
    }

    @Override
    protected void init() {
        initSystemBar();
        initView();
        new loadSongs().execute();
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

    private void initView() {
        mContext = getActivity().getApplicationContext();
        mHandler = HandlerUtil.getInstance(mContext);
        mMusicList = AppCache.getMusicList();
        playlistsManager = PlaylistsManager.getInstance(mContext);
        if (Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())) {
            mCollectMusicList = AppCache.getCollectMusicList();
        }
        if (mMusicList != null && !mMusicList.isEmpty()) {
            playlist_local.setText("本地(" + mMusicList.size() + ")");
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
    }

    public void updateApdater() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void setListener() {
        iv_back.setOnClickListener(this);
        clearAll.setOnClickListener(this);
        playlist_local.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playlist_clear_all:
                AlertDialog.Builder cleanDialog = new AlertDialog.Builder(getContext());
                cleanDialog.setMessage(R.string.confirm_clear_list);
                cleanDialog.setPositiveButton(R.string.clean, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (playlist != null && playlist.isEmpty()) {
                            ToastUtils.show(R.string.clear_list_success);
                            return;
                        }
                        if (playlist != null) playlist.clear();
                        playlistsManager.delete(Actions.DB_PLAY_LIST_COLLECT);
                        if (adapter != null) adapter.notifyDataSetChanged();
                        if (playlistNumber != null) playlistNumber.setText(R.string.music_list);
                    }
                });
                cleanDialog.setNegativeButton(R.string.cancel, null);
                cleanDialog.show();
                break;
            case R.id.playlist_local:
                if (mMusicList.isEmpty()) {
                    ToastUtils.show(R.string.local_music_list);
                } else if (!Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) {
                    AlertDialog.Builder changeDialog = new AlertDialog.Builder(getContext());
                    changeDialog.setMessage(R.string.confirm_change_local_music_list);
                    changeDialog.setPositiveButton(R.string.change_local, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastUtils.show(R.string.list_is_local);
                            Preferences.saveListType(Actions.LIST_TYPE_LOCAL);
                            Preferences.saveListNeedClean(true);
                            new loadSongs().execute();
                            Preferences.saveCollectChangeToLocal(true);
                            Preferences.saveListChangeToLocal(true);
                        }
                    });
                    changeDialog.setNegativeButton(R.string.cancel, null);
                    changeDialog.show();
                }
                break;
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    private void onBackPressed() {
        getActivity().onBackPressed();
        iv_back.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                iv_back.setEnabled(true);
            }
        }, 300);
    }

    //异步加载recyclerview界面
    private class loadSongs extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (mContext != null) {
                try {
                    if (Preferences.isListNeedClean()) { // 重置播放列表数据
                        playlistsManager.delete(Actions.DB_PLAY_LIST_COLLECT);
                        Preferences.saveListNeedClean(false);
                    }
                    playlist = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT, "");
                    if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) { // 判断播放来源
                        if (mMusicList != null && !mMusicList.isEmpty() && playlist.isEmpty()) {
                            for (int i = 0; i < mMusicList.size(); i++) {
                                playlist.add(mMusicList.get(i));
                            }
                            playlistsManager.insertLists(Actions.DB_PLAY_LIST_COLLECT, playlist);
                        }
                    } else if (Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())) {
                        if (mCollectMusicList != null && !mCollectMusicList.isEmpty() && playlist.isEmpty()) {
                            for (int i = 0; i < mCollectMusicList.size(); i++) {
                                Music loadMusic = new Music();
                                CollectMusic.Collect collect = mCollectMusicList.get(i);
                                loadMusic.setTitle(collect.getTitle());
                                loadMusic.setTing_uid(collect.getTing_uid());
                                loadMusic.setPic_small(collect.getPic_small());
                                loadMusic.setPic_big(collect.getPic_big());
                                loadMusic.setLrclink(collect.getLrclink());
                                loadMusic.setSongId(Long.parseLong(collect.getSong_id()));
                                loadMusic.setTitle(collect.getTitle());
                                loadMusic.setArtist(collect.getArtist_name());
                                loadMusic.setType(Music.Type.ONLINE);
                                playlist.add(loadMusic);
                            }
                            playlistsManager.insertLists(Actions.DB_PLAY_LIST_COLLECT, playlist);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (playlist != null && playlist.size() > 0) {
                adapter = new PlaylistAdapter(playlist);
                adapter.setOnMoreClickListener(new OnMoreClickListener() {
                    @Override
                    public void onMoreClick(final int position) {
                        AlertDialog.Builder delDialog = new AlertDialog.Builder(getContext());
                        delDialog.setTitle(playlist.get(position).getTitle());
                        final Music removeMusic = playlist.get(position);
                        String path = FileUtils.getMusicDir() + FileUtils.getMp3FileName(removeMusic.getArtist(), removeMusic.getTitle());
                        final File file = new File(path);
                        int itemsId = file.exists() ? R.array.delete_download : R.array.delete_no_download;
                        delDialog.setItems(itemsId, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:// 从列表中删除
                                        if (playlist != null && playlist.get(position) != null) {
                                            playlistsManager.removeItem(Actions.DB_PLAY_LIST_COLLECT, playlist.get(position).getSongId());
                                        }
                                        playlist.remove(position);
                                        playlistNumber.setText("播放列表(" + (playlist.size() - 1) + ")");
                                        adapter.notifyDataSetChanged();
                                        ToastUtils.show(R.string.delete_from_list);
                                        break;
                                    case 1:// 从本地中删除或者下载
                                        if (file.exists()) {
                                            AlertDialog.Builder removeDialog = new AlertDialog.Builder(getContext());
                                            String msg = getString(R.string.delete_music, removeMusic.getTitle());
                                            removeDialog.setMessage(msg);
                                            removeDialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    File file = new File(removeMusic.getPath());
                                                    if (file.delete()) {
                                                        if (playlist != null && playlist.get(position) != null) {
                                                            playlistsManager.removeItem(Actions.DB_PLAY_LIST_COLLECT, playlist.get(position).getSongId());
                                                        }
                                                        playlist.remove(position);
                                                        if (getPlayService() != null) {
                                                            getPlayService().updatePlayingPosition();
                                                            getPlayService().updateMusicList();
                                                        }
                                                        if (adapter != null)
                                                            adapter.notifyDataSetChanged();
                                                        // 刷新媒体库
                                                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + removeMusic.getPath()));
                                                        getContext().sendBroadcast(intent);
                                                    }
                                                    AppCache.getMusicList().remove(position);
                                                }
                                            });
                                            removeDialog.setNegativeButton(R.string.cancel, null);
                                            removeDialog.show();
                                        } else {
                                            final OnlineMusic onlineMusic = new OnlineMusic();
                                            onlineMusic.setPic_small(removeMusic.getPic_small());
                                            onlineMusic.setTing_uid(removeMusic.getTing_uid());
                                            onlineMusic.setLrclink(removeMusic.getLrclink());
                                            onlineMusic.setPic_big(removeMusic.getPic_big());
                                            onlineMusic.setTitle(removeMusic.getTitle());
                                            onlineMusic.setArtist_name(removeMusic.getArtist());
                                            onlineMusic.setAlbum_title(removeMusic.getAlbum());
                                            onlineMusic.setSong_id(Long.valueOf(removeMusic.getSongId()).toString());
                                            new DownloadOnlineMusic(getActivity(), onlineMusic) {
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
                                        break;

                                }
                            }
                        });
                        delDialog.show();
                    }
                });
                recyclerView.setAdapter(adapter);
                RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
                recyclerView.addItemDecoration(itemDecoration);
                playlistNumber.setText("播放列表(" + playlist.size() + ")");
                if (getPlayService() != null && getPlayService().getPlayingPosition() != -1) {
                    recyclerView.scrollToPosition(getPlayService().getPlayingPosition());
                }
            }
        }
    }

    private class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<Music> playlist = new ArrayList<>();
        private OnMoreClickListener mListener;

        private PlaylistAdapter(ArrayList<Music> list) {
            playlist = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.fragment_playqueue_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            ((ItemViewHolder) holder).MusicName.setText(playlist.get(position).getTitle());
            ((ItemViewHolder) holder).Artist.setText(playlist.get(position).getArtist());
            if (getPlayService() != null) {
                //判断该条目音乐是否在播放
                if (getPlayService().getPlayingPosition() == position) {
                    ((ItemViewHolder) holder).playstate.setVisibility(View.VISIBLE);
                    ((ItemViewHolder) holder).playstate.setImageResource(R.drawable.song_play_icon);
                    if (Preferences.isListChangeToLocal()) {
                        ((ItemViewHolder) holder).playstate.setVisibility(View.GONE);
                        Preferences.saveListChangeToLocal(false);
                    }
                } else {
                    ((ItemViewHolder) holder).playstate.setVisibility(View.GONE);
                }
                ((ItemViewHolder) holder).delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onMoreClick(holder.getAdapterPosition());
                        }
                    }
                });
            } else {
                throw new NullPointerException("getPlayService() is null");
            }
        }

        @Override
        public int getItemCount() {
            return playlist == null ? 0 : playlist.size();
        }

        private void setOnMoreClickListener(OnMoreClickListener listener) {
            mListener = listener;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView delete;
            TextView MusicName, Artist;
            ImageView playstate;

            private ItemViewHolder(View itemView) {
                super(itemView);
                this.playstate = (ImageView) itemView.findViewById(R.id.play_state);
                this.delete = (ImageView) itemView.findViewById(R.id.play_list_delete);
                this.MusicName = (TextView) itemView.findViewById(R.id.play_list_musicname);
                this.Artist = (TextView) itemView.findViewById(R.id.play_list_artist);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                final int firstPosition = getPlayService().getPlayingPosition();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final int currentlyPlayingPosition = getAdapterPosition();
                        if (currentlyPlayingPosition == -1) {
                            return;
                        }
                        if (getPlayService() != null) {
                            if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) { // 判断播放来源
                                getPlayService().play(currentlyPlayingPosition);

                                Preferences.saveLastPosition(Integer.valueOf(currentlyPlayingPosition).toString());
                                if (firstPosition != -1) notifyItemChanged(firstPosition);
                                if (Integer.parseInt(Preferences.getLastPosition()) != -1)
                                    notifyItemChanged(Integer.parseInt(Preferences.getLastPosition()));
                                if (Integer.parseInt(Preferences.getFinalPosition()) != -1)
                                    notifyItemChanged(Integer.parseInt(Preferences.getFinalPosition()));
                                notifyItemChanged(currentlyPlayingPosition);
                            } else if (Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType())) {
                                CollectMusic.Collect collect = mCollectMusicList.get(currentlyPlayingPosition);
                                OnlineMusic song = new OnlineMusic();
                                song.setTing_uid(collect.getTing_uid());
                                song.setArtist_name(collect.getArtist_name());
                                song.setAlbum_title(collect.getAlbum_title());
                                song.setTitle(collect.getTitle());
                                song.setPic_big(collect.getPic_big());
                                song.setSong_id(collect.getSong_id());
                                song.setLrclink(collect.getLrclink());
                                song.setPic_small(collect.getPic_small());
                                if (mProgressDialog == null) {
                                    mProgressDialog = new ProgressDialog(getActivity());
                                    mProgressDialog.setMessage(getString(R.string.get_music_date));
                                }
                                new PlayOnlineMusic(getActivity(), song) {
                                    @Override
                                    public void onPrepare() {
                                        if (mProgressDialog != null && !mProgressDialog.isShowing())
                                            mProgressDialog.show();
                                    }

                                    @Override
                                    public void onExecuteSuccess(Music pMusic) {
                                        if (mProgressDialog != null)
                                            mProgressDialog.cancel();
                                        getPlayService().playPosition(pMusic, currentlyPlayingPosition);
                                        ToastUtils.show(getString(R.string.now_play, pMusic.getTitle()));

                                        Preferences.saveLastPosition(Integer.valueOf(currentlyPlayingPosition).toString());
                                        if (firstPosition != -1) notifyItemChanged(firstPosition);
                                        if (Integer.parseInt(Preferences.getLastPosition()) != -1)
                                            notifyItemChanged(Integer.parseInt(Preferences.getLastPosition()));
                                        if (Integer.parseInt(Preferences.getFinalPosition()) != -1)
                                            notifyItemChanged(Integer.parseInt(Preferences.getFinalPosition()));
                                        notifyItemChanged(currentlyPlayingPosition);
                                    }

                                    @Override
                                    public void onExecuteFail(Exception e) {
                                        if (mProgressDialog != null)
                                            mProgressDialog.cancel();
                                        ToastUtils.show(R.string.unable_to_play);
                                    }
                                }.execute();
                            } else {
                                ArrayList<Music> arrayList = playlistsManager.getMusic(Actions.DB_PLAY_LIST_COLLECT, "");
                                Music music = arrayList.get(currentlyPlayingPosition);
                                OnlineMusic song = new OnlineMusic();
                                song.setTing_uid(music.getTing_uid());
                                song.setArtist_name(music.getArtist());
                                song.setAlbum_title(music.getAlbum());
                                song.setTitle(music.getTitle());
                                song.setPic_big(music.getPic_big());
                                song.setSong_id(Long.valueOf(music.getSongId()).toString());
                                song.setLrclink(music.getLrclink());
                                song.setPic_small(music.getPic_small());
                                if (mProgressDialog == null) {
                                    mProgressDialog = new ProgressDialog(getActivity());
                                    mProgressDialog.setMessage(getString(R.string.get_music_date));
                                }
                                new PlayOnlineMusic(getActivity(), song) {
                                    @Override
                                    public void onPrepare() {
                                        if (mProgressDialog != null && !mProgressDialog.isShowing())
                                            mProgressDialog.show();
                                    }

                                    @Override
                                    public void onExecuteSuccess(Music pMusic) {
                                        if (mProgressDialog != null)
                                            mProgressDialog.cancel();
                                        getPlayService().playPosition(pMusic, currentlyPlayingPosition);
                                        ToastUtils.show(getString(R.string.now_play, pMusic.getTitle()));

                                        Preferences.saveLastPosition(Integer.valueOf(currentlyPlayingPosition).toString());
                                        if (firstPosition != -1) notifyItemChanged(firstPosition);
                                        if (Integer.parseInt(Preferences.getLastPosition()) != -1)
                                            notifyItemChanged(Integer.parseInt(Preferences.getLastPosition()));
                                        if (Integer.parseInt(Preferences.getFinalPosition()) != -1)
                                            notifyItemChanged(Integer.parseInt(Preferences.getFinalPosition()));
                                        notifyItemChanged(currentlyPlayingPosition);
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
                            throw new NullPointerException("getPlayService() is null");
                        }
                    }
                }, 70);
            }
        }
    }
}