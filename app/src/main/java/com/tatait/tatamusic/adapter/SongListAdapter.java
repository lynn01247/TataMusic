package com.tatait.tatamusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.OnlineMusic;
import com.tatait.tatamusic.model.OnlineMusicList;
import com.tatait.tatamusic.model.SongListInfo;
import com.tatait.tatamusic.utils.ImageUtils;
import com.tatait.tatamusic.utils.binding.Bind;
import com.tatait.tatamusic.utils.binding.ViewBinder;

import java.util.List;

/**
 * 歌单列表适配器
 * Created by Lynn on 2015/12/19.
 */
public class SongListAdapter extends BaseAdapter {
    private static final int TYPE_PROFILE = 0;
    private static final int TYPE_MUSIC_LIST = 1;
    private Context mContext;
    private List<SongListInfo> mData;

    public SongListAdapter(List<SongListInfo> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_MUSIC_LIST;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).getType().equals("#")) {
            return TYPE_PROFILE;
        } else {
            return TYPE_MUSIC_LIST;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mContext = parent.getContext();
        ViewHolderProfile holderProfile;
        ViewHolderMusicList holderMusicList;
        SongListInfo songListInfo = mData.get(position);
        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case TYPE_PROFILE:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.view_holder_song_list_profile, parent, false);
                    holderProfile = new ViewHolderProfile(convertView);
                    convertView.setTag(holderProfile);
                } else {
                    holderProfile = (ViewHolderProfile) convertView.getTag();
                }
                holderProfile.tvProfile.setText(songListInfo.getTitle());
                break;
            case TYPE_MUSIC_LIST:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.view_holder_song_list, parent, false);
                    holderMusicList = new ViewHolderMusicList(convertView);
                    convertView.setTag(holderMusicList);
                } else {
                    holderMusicList = (ViewHolderMusicList) convertView.getTag();
                }
                getMusicListInfo(songListInfo, holderMusicList);
                holderMusicList.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
                break;
        }
        return convertView;
    }

    private boolean isShowDivider(int position) {
        return position != mData.size() - 1;
    }

    private void getMusicListInfo(final SongListInfo songListInfo, final ViewHolderMusicList holderMusicList) {
        if (songListInfo.getCoverUrl() == null) {
            holderMusicList.ivCover.setTag(songListInfo.getTitle());
            holderMusicList.ivCover.setImageResource(R.drawable.default_cover);
            holderMusicList.tvMusic1.setText(R.string.one_loading);
            holderMusicList.tvMusic2.setText(R.string.two_loading);
            holderMusicList.tvMusic3.setText(R.string.three_loading);
            HttpClient.getSongListInfo(songListInfo.getType(), 3, 0, new HttpCallback<OnlineMusicList>() {
                @Override
                public void onSuccess(OnlineMusicList response) {
                    if (response == null || response.getSong_list() == null) {
                        return;
                    }
                    if (!songListInfo.getTitle().equals(holderMusicList.ivCover.getTag())) {
                        return;
                    }
                    parse(response, songListInfo);
                    setData(songListInfo, holderMusicList);
                }

                @Override
                public void onFail(Exception e) {
                }
            });
        } else {
            holderMusicList.ivCover.setTag(null);
            setData(songListInfo, holderMusicList);
        }
    }

    private void parse(OnlineMusicList response, SongListInfo songListInfo) {
        List<OnlineMusic> onlineMusics = response.getSong_list();
        songListInfo.setCoverUrl(response.getBillboard().getPic_s260());
        if (onlineMusics.size() >= 1) {
            songListInfo.setMusic1(mContext.getString(R.string.song_list_item_title_1,
                    onlineMusics.get(0).getTitle(), onlineMusics.get(0).getArtist_name()));
        } else {
            songListInfo.setMusic1("");
        }
        if (onlineMusics.size() >= 2) {
            songListInfo.setMusic2(mContext.getString(R.string.song_list_item_title_2,
                    onlineMusics.get(1).getTitle(), onlineMusics.get(1).getArtist_name()));
        } else {
            songListInfo.setMusic2("");
        }
        if (onlineMusics.size() >= 3) {
            songListInfo.setMusic3(mContext.getString(R.string.song_list_item_title_3,
                    onlineMusics.get(2).getTitle(), onlineMusics.get(2).getArtist_name()));
        } else {
            songListInfo.setMusic3("");
        }
    }

    private void setData(SongListInfo songListInfo, ViewHolderMusicList holderMusicList) {
        ImageLoader.getInstance().displayImage(songListInfo.getCoverUrl(), holderMusicList.ivCover, ImageUtils.getCoverDisplayOptions());
        holderMusicList.tvMusic1.setText(songListInfo.getMusic1());
        holderMusicList.tvMusic2.setText(songListInfo.getMusic2());
        holderMusicList.tvMusic3.setText(songListInfo.getMusic3());
    }

    private static class ViewHolderProfile {
        @Bind(R.id.tv_profile)
        TextView tvProfile;

        private ViewHolderProfile(View view) {
            ViewBinder.bind(this, view);
        }
    }

    private static class ViewHolderMusicList {
        @Bind(R.id.iv_cover)
        ImageView ivCover;
        @Bind(R.id.tv_music_1)
        TextView tvMusic1;
        @Bind(R.id.tv_music_2)
        TextView tvMusic2;
        @Bind(R.id.tv_music_3)
        TextView tvMusic3;
        @Bind(R.id.v_divider)
        View vDivider;

        private ViewHolderMusicList(View view) {
            ViewBinder.bind(this, view);
        }
    }
}