package com.tatait.tatamusic.fragment;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.activity.MusicActivity;
import com.tatait.tatamusic.adapter.LocalMusicAdapter;
import com.tatait.tatamusic.adapter.OnMoreClickListener;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.SystemUtils;
import com.tatait.tatamusic.utils.ToastUtils;
import com.tatait.tatamusic.utils.binding.Bind;

import java.io.File;

/**
 * 本地音乐列表
 * Created by Lynn on 2015/11/26.
 */
public class LocalMusicFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnMoreClickListener {
    private static final int REQUEST_WRITE_SETTINGS = 1;
    @Bind(R.id.lv_local_music)
    private ListView lvLocalMusic;
    @Bind(R.id.tv_empty)
    private TextView tvEmpty;
    private LocalMusicAdapter mAdapter;
    private View view;

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
        view = inflater.inflate(R.layout.fragment_local_music, container, false);
        return view;
    }

    @Override
    protected void init() {
        if (mAdapter == null) {
            mAdapter = new LocalMusicAdapter();
        }
        mAdapter.setOnMoreClickListener(this);
        updateView();
        lvLocalMusic.setAdapter(mAdapter);
        if (getPlayService().getPlayingMusic() != null && getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            lvLocalMusic.setSelection(getPlayService().getPlayingPosition());
        }
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getContext().registerReceiver(mDownloadReceiver, filter);
    }

    @Override
    protected void setListener() {
        lvLocalMusic.setOnItemClickListener(this);
    }

    private void updateView() {
        if (AppCache.getMusicList().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.updatePlayingPosition(getPlayService());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        if (Actions.LIST_TYPE_LOCAL.equals(Preferences.getListType())) { //判断是否是本地类型
            if (getPlayService() != null) {
                // 还未播放则正常播放;当前歌曲不是已经在播放了的歌曲则正常播放
                if ((getPlayService().getPlayingPosition() != -1 && position != getPlayService().getPlayingPosition()) || !getPlayService().isPlaying()) {
                    ((MusicActivity) getActivity()).updateApdater();

                    getPlayService().play(position);
                }
            } else {
                throw new NullPointerException("getPlayService() is null");
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(R.string.confirm_change_list);
            dialog.setPositiveButton(R.string.change_list, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Preferences.saveListType(Actions.LIST_TYPE_LOCAL);
                    Preferences.saveListNeedClean(true);
                    ((MusicActivity) getActivity()).resetPlayListFragment();
                    if (getPlayService() != null) {
                        getPlayService().play(position);
                    } else {
                        throw new NullPointerException("getPlayService() is null");
                    }
                }
            });
            dialog.setNegativeButton(R.string.cancel, null);
            dialog.show();
        }
    }

    @Override
    public void onMoreClick(final int position) {
        final Music music = AppCache.getMusicList().get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        int itemsId = (position == getPlayService().getPlayingPosition()) ? R.array.local_music_dialog_without_delete : R.array.local_music_dialog;
        dialog.setItems(itemsId, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:// 分享
                        shareMusic(music);
                        break;
                    case 1:// 设为铃声
                        requestSetRingtone(music);
                        break;
                    case 2:// 查看歌曲信息
                        musicInfo(music);
                        break;
                    case 3:// 删除
                        deleteMusic(music,position);
                        break;
                }
            }
        });
        dialog.show();
    }

    public void onItemPlay() {
        updateView();
        if (getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            lvLocalMusic.smoothScrollToPosition(getPlayService().getPlayingPosition());
        }
    }

    /**
     * 分享音乐
     */
    private void shareMusic(Music music) {
        File file = new File(music.getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void requestSetRingtone(final Music music) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getContext())) {
            ToastUtils.show(R.string.no_ring_permission);
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
        } else {
            setRingtone(music);
        }
    }

    /**
     * 设置铃声
     */
    private void setRingtone(Music music) {
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getPath());
        // 查询音乐文件在媒体库是否存在
        Cursor cursor = getContext().getContentResolver().query(uri, null,
                MediaStore.MediaColumns.DATA + "=?", new String[]{music.getPath()}, null);
        if (cursor == null) {
            return;
        }
        if (cursor.moveToFirst() && cursor.getCount() > 0) {
            String _id = cursor.getString(0);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.IS_MUSIC, true);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_PODCAST, false);

            getContext().getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA + "=?",
                    new String[]{music.getPath()});
            Uri newUri = ContentUris.withAppendedId(uri, Long.valueOf(_id));
            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, newUri);
            ToastUtils.show(R.string.setting_ringtone_success);
        }
        cursor.close();
    }

    private void musicInfo(Music music) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(music.getTitle());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(R.string.artist)
                .append(music.getArtist())
                .append("\n\n")
                .append(R.string.album)
                .append(music.getAlbum())
                .append("\n\n")
                .append(R.string.play_time)
                .append(SystemUtils.formatTime("mm:ss", music.getDuration()))
                .append("\n\n")
                .append(R.string.file_name)
                .append(music.getTitle())
                .append("\n\n")
                .append(R.string.file_size)
                .append(FileUtils.b2mb((int) music.getFileSize()))
                .append(R.string.size_mb)
                .append("\n\n")
                .append(R.string.file_path)
                .append(new File(music.getPath()).getParent());
        dialog.setMessage(stringBuilder.toString());
        dialog.show();
    }

    /**
     * 删除音乐
     */
    private void deleteMusic(final Music music,final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        String title = music.getTitle();
        String msg = getString(R.string.delete_music, title);
        dialog.setMessage(msg);
        dialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(music.getPath());
                if (file.delete()) {
                    getPlayService().updatePlayingPosition();
                    updateView();
                    // 刷新媒体库
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + music.getPath()));
                    getContext().sendBroadcast(intent);
                }
                AppCache.getMusicList().remove(position);
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(getContext())) {
                ToastUtils.show(R.string.ring_permission_success);
            }
        }
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(mDownloadReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            String title = AppCache.getDownloadList().get(id);
            if (TextUtils.isEmpty(title)) {
                return;
            }
            // 由于系统扫描音乐是异步执行，因此延迟刷新音乐列表
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }
                    getPlayService().updateMusicList();
                    updateView();
                }
            }, 1000);
        }
    };
}