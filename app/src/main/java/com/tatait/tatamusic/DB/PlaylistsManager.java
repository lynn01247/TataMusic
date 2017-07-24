package com.tatait.tatamusic.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.tatait.tatamusic.model.Music;

import java.util.ArrayList;

/**
 * Created by Lynn on 2016/3/3.
 */
public class PlaylistsManager {
    private static PlaylistsManager sInstance = null;

    private MusicDB mMusicDatabase = null;

    private PlaylistsManager(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static final synchronized PlaylistsManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PlaylistsManager(context.getApplicationContext());
        }
        return sInstance;
    }

    //建立播放列表表设置播放列表id和歌曲id为复合主键
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistsColumns.NAME + " ("
                + PlaylistsColumns.PLAYLIST_ID + " LONG NOT NULL," + PlaylistsColumns.TRACK_ID + " LONG NOT NULL," + PlaylistsColumns.TRACK_ORDER + " LONG,"
                + PlaylistsColumns.TRACK_NAME + " CHAR," + PlaylistsColumns.SONG_ID + " LONG," + PlaylistsColumns.ALBUM_ID + " LONG,"
                + PlaylistsColumns.ALBUM + " CHAR," + PlaylistsColumns.DURATION + " CHAR," + PlaylistsColumns.TITLE + " CHAR,"
                + PlaylistsColumns.ARTIST + " CHAR," + PlaylistsColumns.ARTIST_ID + " LONG," + PlaylistsColumns.DATA + " CHAR,"
                + PlaylistsColumns.PIC_BIG + " CHAR," + PlaylistsColumns.PIC_SMALL + " CHAR," + PlaylistsColumns.SIZE + " LONG,"
                + PlaylistsColumns.PATH + " CHAR," + PlaylistsColumns.LRC_LINK + " CHAR," + PlaylistsColumns.IS_LOCAL + " LONG ,"
                + PlaylistsColumns.IS_COLLECT + " LONG," + PlaylistsColumns.COVER_PATH + " CHAR," + PlaylistsColumns.TING_UID + " CHAR, "
                + "primary key ( " + PlaylistsColumns.PLAYLIST_ID
                + ", " + PlaylistsColumns.TRACK_ID + "));");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistsColumns.NAME);
        onCreate(db);
    }

    public ArrayList<Music> getMusic(long playlistid, String orderTitle) {
        ArrayList<Music> results = new ArrayList<>();
        if ("".equals(orderTitle)) {
            orderTitle = PlaylistsColumns.TRACK_ORDER + " ASC ";
        } else {
            orderTitle = orderTitle + " ASC ";
        }
        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.PLAYLIST_ID + " = " + String.valueOf(playlistid), null, null, null, orderTitle, null);
            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());
                do {
                    Music info = new Music();
                    info.songId = cursor.getLong(4);
                    info.albumId = cursor.getLong(5);
                    info.album = cursor.getString(6);
                    info.duration = cursor.getLong(7);
                    info.title = cursor.getString(8);
                    info.artist = cursor.getString(9);
                    info.artistId = cursor.getLong(10);
                    info.data = cursor.getString(11);
                    info.pic_big = cursor.getString(12);
                    info.pic_small = cursor.getString(13);
                    info.fileSize = cursor.getLong(14);
                    info.path = cursor.getString(15);
                    info.lrclink = cursor.getString(16);
                    info.isLocal = cursor.getLong(17);
                    info.isCollect = cursor.getLong(18);
                    info.CoverPath = cursor.getString(19);
                    info.Ting_uid = cursor.getString(20);
                    results.add(info);
                } while (cursor.moveToNext());
            }
            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public int getMusicSize(long playlistid) {
        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.PLAYLIST_ID + " = " + String.valueOf(playlistid), null, null, null, null, null);
            if (cursor == null) {
                return 0;
            }
            return cursor.getCount();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized void insertMusic(long playlistid, Music info) {
        if (info == null) {
            return;
        }
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(PlaylistsColumns.PLAYLIST_ID, playlistid);
            values.put(PlaylistsColumns.TRACK_ID, info.songId);
            values.put(PlaylistsColumns.TRACK_ORDER, getMusicSize(playlistid));
            values.put(PlaylistsColumns.TRACK_NAME, info.title);
            values.put(PlaylistsColumns.SONG_ID, info.songId);
            values.put(PlaylistsColumns.ALBUM_ID, info.albumId);
            values.put(PlaylistsColumns.ALBUM, info.album);
            values.put(PlaylistsColumns.DURATION, info.duration);
            values.put(PlaylistsColumns.TITLE, info.title);
            values.put(PlaylistsColumns.ARTIST, info.artist);
            values.put(PlaylistsColumns.ARTIST_ID, info.artistId);
            values.put(PlaylistsColumns.DATA, info.data);
            values.put(PlaylistsColumns.PIC_BIG, info.pic_big);
            values.put(PlaylistsColumns.PIC_SMALL, info.pic_small);
            values.put(PlaylistsColumns.SIZE, info.fileSize);
            values.put(PlaylistsColumns.PATH, info.path);
            values.put(PlaylistsColumns.LRC_LINK, info.lrclink);
            values.put(PlaylistsColumns.IS_LOCAL, info.isLocal);
            values.put(PlaylistsColumns.IS_COLLECT, info.isCollect);
            values.put(PlaylistsColumns.COVER_PATH, info.CoverPath);
            values.put(PlaylistsColumns.TING_UID, info.Ting_uid);
            if (!isExist(playlistid, info.songId))
                database.insert(PlaylistsColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void insertLists(long playlistid, ArrayList<Music> musics) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        int len = musics.size();
        try {
            for (int i = 0; i < len; i++) {
                Music info = musics.get(i);
                ContentValues values = new ContentValues();
                values.put(PlaylistsColumns.PLAYLIST_ID, playlistid);
                values.put(PlaylistsColumns.TRACK_ID, info.songId);
                values.put(PlaylistsColumns.TRACK_ORDER, getMusicSize(playlistid));
                values.put(PlaylistsColumns.TRACK_NAME, info.title);
                values.put(PlaylistsColumns.SONG_ID, info.songId);
                values.put(PlaylistsColumns.ALBUM_ID, info.albumId);
                values.put(PlaylistsColumns.ALBUM, info.album);
                values.put(PlaylistsColumns.DURATION, info.duration);
                values.put(PlaylistsColumns.TITLE, info.title);
                values.put(PlaylistsColumns.ARTIST, info.artist);
                values.put(PlaylistsColumns.ARTIST_ID, info.artistId);
                values.put(PlaylistsColumns.DATA, info.data);
                values.put(PlaylistsColumns.PIC_BIG, info.pic_big);
                values.put(PlaylistsColumns.PIC_SMALL, info.pic_small);
                values.put(PlaylistsColumns.SIZE, info.fileSize);
                values.put(PlaylistsColumns.PATH, info.path);
                values.put(PlaylistsColumns.LRC_LINK, info.lrclink);
                values.put(PlaylistsColumns.IS_LOCAL, info.isLocal);
                values.put(PlaylistsColumns.IS_COLLECT, info.isCollect);
                values.put(PlaylistsColumns.COVER_PATH, info.CoverPath);
                values.put(PlaylistsColumns.TING_UID, info.Ting_uid);
                if (!isExist(playlistid, info.songId))
                    database.insert(PlaylistsColumns.NAME, null, values);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    private synchronized boolean isExist(long playlistid, long id) {
        Cursor cursor = null;
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            cursor = database.query(PlaylistsColumns.NAME, null, PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " +
                    PlaylistsColumns.TRACK_ID + " = ?", new String[]{playlistid + "", id + ""}, null, null, null);
            database.setTransactionSuccessful();
            return (cursor != null && cursor.getCount() > 0);
        } finally {
            database.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public void removeItem(long playlistId, long songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " + PlaylistsColumns.TRACK_ID + " = ?", new String[]{
                String.valueOf(playlistId), String.valueOf(songId)
        });
    }

    public void delete(final long PlaylistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?", new String[]
                {String.valueOf(PlaylistId)});
    }

    //删除播放列表中的记录的音乐 ，删除本地文件时调用
    public synchronized void deleteMusic(Context context, final long musicId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.TRACK_ID + " = " + String.valueOf(musicId), null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                long[] deletedPlaylistIds = new long[cursor.getCount()];
                int i = 0;

                do {
                    deletedPlaylistIds[i] = cursor.getLong(0);
                    i++;
                } while (cursor.moveToNext());
                PlaylistInfo.getInstance(context).updatePlaylistMusicCount(deletedPlaylistIds);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.TRACK_ID + " = ?", new String[]
                {String.valueOf(musicId)});
    }


    public void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, null, null);
    }

    private interface PlaylistsColumns {
        /* Table name */
        String NAME = "playlists";
        /* Album IDs column */
        String PLAYLIST_ID = "playlist_id";
        /* Time played column */
        String TRACK_ID = "track_id";
        String TRACK_ORDER = "track_order";
        String TRACK_NAME = "track_name";
        /* Music data */
        String SONG_ID = "song_id";
        String ALBUM_ID = "album_id";
        String ALBUM = "album";
        String DURATION = "duration";
        String TITLE = "title";
        String ARTIST = "artist";
        String ARTIST_ID = "artist_id";
        String DATA = "data";
        String PIC_BIG = "pic_big";
        String PIC_SMALL = "pic_small";
        String SIZE = "size";
        String PATH = "path";
        String LRC_LINK = "lrc_link";
        String IS_LOCAL = "is_local";
        String IS_COLLECT = "is_collect";
        String COVER_PATH = "Cover_Path";
        String TING_UID = "Ting_uid";
    }
}