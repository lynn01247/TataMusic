package com.tatait.tatamusic.service;

import java.util.Random;

import com.tatait.tatamusic.MusicPlay;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.pojo.MusicInfo;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class MusicService extends Service implements OnCompletionListener {
	public static MediaPlayer mplayer;
	public static int playing_id = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initMediaSource(initMusicUri(0));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		String playFlag = intent.getExtras().getString("control");
		if ("play".equals(playFlag)) {
			playMusic();
		} else if ("next".equals(playFlag)) {
			offHandler();
			playNext();
		} else if ("previous".equals(playFlag)) {
			offHandler();
			playPre();
		} else if ("listClick".equals(playFlag)) {
			offHandler();
			playing_id = intent.getExtras().getInt("musicId");
			initMediaSource(initMusicUri(playing_id));
			playMusic();
		}
	}

	public void offHandler() {
		playFlag = false;
	}

	/**
	 * 初始化媒体对象
	 * 
	 * @param mp3Path
	 */
	public void initMediaSource(String mp3Path) {
		Uri mp3Uri = Uri.parse(mp3Path);
		if (mplayer != null) {
			mplayer.stop();
			mplayer.reset();
			mplayer = null;
		}
		mplayer = MediaPlayer.create(this, mp3Uri);
		mplayer.setOnCompletionListener(this);
	}

	/**
	 * 返回列表第几行的歌曲路径
	 * 
	 * @param _id
	 *            表示歌曲序号，从0开始
	 * @return
	 */
	public String initMusicUri(int _id) {
		playing_id = _id;
		return MusicPlay.mAdapter.musicList.get(playing_id).getMusicPath();
	}

	/**
	 * 音乐播放方法，并且带有暂停方法
	 */
	public Thread thread;
	public boolean playFlag = true;

	public void playMusic() {

		if (mplayer != null) {
			if (mplayer.isPlaying()) {
				MusicPlay.play_button
						.setImageResource(R.drawable.play_button_xml);
				mplayer.pause();
			} else {
				setInfo();
				MusicPlay.play_button
						.setImageResource(R.drawable.pause_button_xml);
				mplayer.start();
			}
			mHandler.post(mRunnable);
			playFlag = true;
			thread = new Thread() {
				@Override
				public void run() {
					while (playFlag) {
						MusicPlay.playingTime = mplayer.getCurrentPosition();
						MusicPlay.seekbar.setProgress(MusicPlay.playingTime);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			};
			thread.start();
		}
		mplayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				if (MusicPlay.play_mode == MusicInfo.LISTREPEAT) {
					playNext();
				} else if (MusicPlay.play_mode == MusicInfo.SINGLEREPEAT) {
					initMediaSource(initMusicUri(playing_id));
					playMusic();
				} else {
					Random rand = new Random();
					int i = rand.nextInt(); // int范围类的随机数
					int size = MusicPlay.mAdapter.musicList.size();
					i = rand.nextInt(size); // 生成0-100以内的随机数
					initMediaSource(initMusicUri(i));
					playMusic();
				}

			}
		});
	}

	public void setInfo() {
		// 获得歌曲时间
		MusicPlay.songTime = MusicPlay.mAdapter.musicList.get(
				MusicService.playing_id).getMusicTime();
		MusicPlay.seekbar.setMax(MusicPlay.songTime);
		MusicPlay.mName.setText(MusicPlay.mAdapter
				.toMp3(MusicPlay.mAdapter.musicList
						.get(MusicService.playing_id).getMusicName()));
		String url = MusicPlay.mAdapter
				.getAlbumArt(MusicPlay.mAdapter.musicList.get(
						MusicService.playing_id).getMusicId());
		if (url != null) {
			MusicPlay.mAlbum.setImageURI(Uri.parse(url));
		} else {
			MusicPlay.mAlbum.setImageResource(R.drawable.album);
		}
	}

	// 上一首
	public void playPre() {
		if (MusicPlay.play_mode == MusicInfo.RANDOM) {
			Random rand = new Random();
			int i = rand.nextInt(); // int范围类的随机数
			int size = MusicPlay.mAdapter.musicList.size();
			i = rand.nextInt(size); // 生成0-100以内的随机数
			initMediaSource(initMusicUri(i));
		} else {
			if (playing_id == 0) {
				playing_id = MusicPlay.mAdapter.musicList.size() - 1;
				initMediaSource(initMusicUri(playing_id));
			} else {
				initMediaSource(initMusicUri(--playing_id));
			}
		}
		playMusic();
	}

	// 下一首
	public void playNext() {
		if (MusicPlay.play_mode == MusicInfo.RANDOM) {
			Random rand = new Random();
			int i = rand.nextInt(); // int范围类的随机数
			int size = MusicPlay.mAdapter.musicList.size();
			i = rand.nextInt(size); // 生成0-100以内的随机数
			initMediaSource(initMusicUri(i));
		} else {
			if (playing_id == MusicPlay.mAdapter.musicList.size() - 1) {
				initMediaSource(initMusicUri(0));
			} else {
				initMediaSource(initMusicUri(++playing_id));
			}
		}
		playMusic();
	}

	public void onCompletion(MediaPlayer arg0) {
		Toast.makeText(this, "onCompletion", 1).show();
	}

	Handler mHandler = new Handler();

	Runnable mRunnable = new Runnable() {
		public void run() {
			mHandler.postDelayed(mRunnable, 100);
		}
	};
}