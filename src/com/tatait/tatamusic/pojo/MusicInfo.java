package com.tatait.tatamusic.pojo;

/**
 * 1、歌曲名， 2、歌手， 3、歌曲时间， 4、专辑（专辑图片，专辑名称，专辑ID[用来获取图片])， 5、歌曲大小 6.歌曲路径
 */
public class MusicInfo {
	private int musicId;
	private String musicName;
	private String musicSinger;
	private int musicTime;
	private String musicAlubm;
	private int musicSize;
	private String musicPath;

	/**
	 * 播放模式
	 */
	public final static int LISTREPEAT = 1;
	public final static int SINGLEREPEAT = 2;
	public final static int RANDOM = 3;
	
	public int getMusicId() {
		return musicId;
	}

	public void setMusicId(int musicId) {
		this.musicId = musicId;
	}

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getMusicSinger() {
		return musicSinger;
	}

	public void setMusicSinger(String musicSinger) {
		this.musicSinger = musicSinger;
	}

	public int getMusicTime() {
		return musicTime;
	}

	public void setMusicTime(int musicTime) {
		this.musicTime = musicTime;
	}

	public String getMusicAlubm() {
		return musicAlubm;
	}

	public void setMusicAlubm(String musicAlubm) {
		this.musicAlubm = musicAlubm;
	}

	public int getMusicSize() {
		return musicSize;
	}

	public void setMusicSize(int musicSize) {
		this.musicSize = musicSize;
	}

	public String getMusicPath() {
		return musicPath;
	}

	public void setMusicPath(String musicPath) {
		this.musicPath = musicPath;
	}

}
