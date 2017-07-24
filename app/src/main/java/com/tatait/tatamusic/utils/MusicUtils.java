package com.tatait.tatamusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.CollectMusic;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.model.OnlineMusic;

import java.util.List;

/**
 * 歌曲工具类
 * Created by Lynn on 2015/11/27.
 */
public class MusicUtils {

    /**
     * 扫描歌曲
     */
    public static void scanMusic(Context context, List<Music> musicList) {
        musicList.clear();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            // 是否为音乐
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            if (isMusic == 0) {
                continue;
            }
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String unknown = context.getString(R.string.unknown);
            artist = artist.equals("<unknown>") ? unknown : artist;
            String album = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String coverPath = getCoverPath(context, albumId);
            String fileName = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            long fileSize = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            Music music = new Music();
            music.setId(id);
            music.setType(Music.Type.LOCAL);
            music.setTitle(title);
            music.setArtist(artist);
            music.setAlbum(album);
            music.setDuration(duration);
            music.setPath(path);
            music.setCoverPath(coverPath);
            music.setFileName(fileName);
            music.setFileSize(fileSize);
            CoverLoader.getInstance().loadThumbnail(music);
            musicList.add(music);
        }
        cursor.close();
    }

    private static String getCoverPath(Context context, long albumId) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://media/external/audio/albums/" + albumId),
                new String[]{"album_art"}, null, null, null);
        if (cursor != null) {
            cursor.moveToNext();
            path = cursor.getString(0);
            cursor.close();
        }
        return path;
    }

    public static void collectMusic(OnlineMusic onlineMusic, String isSearch) {
        HttpClient.collectMusic(onlineMusic, isSearch, new HttpCallback<CollectMusic>() {
            @Override
            public void onSuccess(CollectMusic response) {
                if (response == null) {
                    onFail(null);
                } else {
                    ToastUtils.show(response.getInfo());
                }
            }

            @Override
            public void onFail(Exception e) {
                ToastUtils.show(R.string.get_fail);
            }
        });
    }

    public static void unCollectMusic(String song_id, final Handler handler) {
        HttpClient.unCollectMusic(song_id, new HttpCallback<CollectMusic>() {
            @Override
            public void onSuccess(CollectMusic response) {
                if (response == null) {
                    onFail(null);
                } else {
                    Message message = new Message();
                    message.obj = response.getInfo();
                    message.what = 0x109;
                    handler.handleMessage(message);
                }
            }

            @Override
            public void onFail(Exception e) {
                ToastUtils.show(R.string.get_fail);
            }
        });
    }
}