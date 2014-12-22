package com.tatait.tatamusic.adapter;

import java.util.ArrayList;
import java.util.List;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.R.id;
import com.tatait.tatamusic.R.layout;
import com.tatait.tatamusic.pojo.MusicInfo;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicAdapter extends BaseAdapter {
	// 用来获得ContentProvider(共享数据库)
	public ContentResolver cr;
	// 用来装查询到的音乐文件数据
	public Cursor cur;
	// 歌曲信息列表
	public List<MusicInfo> musicList;
	// 歌曲详细信息属性类
	public MusicInfo mInfo;

	public Context context;

	// 音乐信息
	/* 1、歌曲名，2、歌手，3、歌曲时间，4、专辑（专辑图片，专辑名称，专辑ID[用来获取图片])，5、歌曲大小 */

	public MusicAdapter(Context context) {

		this.context = context;

		// 取得数据库对象
		cr = context.getContentResolver();

		musicList = new ArrayList<MusicInfo>();

		String[] mString = new String[] {

		MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DATA };

		// 查询所有音乐信息
		cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mString,
				null, null, MediaStore.Audio.AudioColumns.TITLE);

		if (cur != null) {
			// 移动游标到第一个
			cur.moveToFirst();

			for (int i = 0; i < cur.getCount(); i++) {
				if (cur.getString(0).endsWith(".mp3")) {
					mInfo = new MusicInfo();
					mInfo.setMusicName(cur.getString(0));
					mInfo.setMusicAlubm(cur.getString(1));
					mInfo.setMusicSinger(cur.getString(2));
					mInfo.setMusicTime(cur.getInt(3));
					mInfo.setMusicSize(cur.getInt(4));
					mInfo.setMusicId(cur.getInt(5));
					mInfo.setMusicPath(cur.getString(6));
					musicList.add(mInfo);
				}
				cur.moveToNext();
			}

		}

	}

	public int getCount() {
		return musicList.size();
	}

	public Object getItem(int arg0) {
		return musicList.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater mlistLayout = LayoutInflater.from(context);
		View mlistView = mlistLayout.inflate(R.layout.music_list_item, null);
		TextView songNum = (TextView) mlistView.findViewById(R.id.songNum);
		TextView singer = (TextView) mlistView.findViewById(R.id.singer);
		TextView songName = (TextView) mlistView.findViewById(R.id.songName);
		TextView songTime = (TextView) mlistView.findViewById(R.id.songTime);
		int mNUm = arg0 + 1;
		songNum.setText("" + mNUm);
		singer.setText(musicList.get(arg0).getMusicSinger());
		songName.setText(toMp3(musicList.get(arg0).getMusicName()));
		songTime.setText(toTime(musicList.get(arg0).getMusicTime()));
		return mlistView;
	}

	public String toMp3(String name) {
		int search = name.indexOf(".mp3");
		String newName = name.substring(0, search);
		return newName;

	}

	/**
	 * 播放器进度条时间处理方法
	 * 
	 * @param time
	 * @return
	 */
	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 歌曲专辑图片显示,如果有歌曲图片，才会返回，否则为null，要注意判断
	 * 
	 * @param trackId
	 * @return 返回类型是String 类型的图片地址，也就是uri
	 */
	public String getAlbumArt(int trackId) {// trackId是音乐的id
		String mUriTrack = "content://media/external/audio/media/#";
		String[] projection = new String[] { "album_id" };
		String selection = "_id = ?";
		String[] selectionArgs = new String[] { Integer.toString(trackId) };
		Cursor cur = context.getContentResolver().query(Uri.parse(mUriTrack),
				projection, selection, selectionArgs, null);
		int album_id = 0;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_id = cur.getInt(0);
		}
		cur.close();
		cur = null;

		if (album_id < 0) {
			return null;
		}
		String mUriAlbums = "content://media/external/audio/albums";
		projection = new String[] { "album_art" };
		cur = context.getContentResolver().query(
				Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
				projection, null, null, null);

		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;

		return album_art;
	}
}
