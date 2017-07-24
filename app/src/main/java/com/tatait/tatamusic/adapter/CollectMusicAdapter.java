package com.tatait.tatamusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.constants.Actions;
import com.tatait.tatamusic.model.CollectMusic;
import com.tatait.tatamusic.model.Music;
import com.tatait.tatamusic.service.PlayService;
import com.tatait.tatamusic.utils.FileUtils;
import com.tatait.tatamusic.utils.ImageUtils;
import com.tatait.tatamusic.utils.Preferences;
import com.tatait.tatamusic.utils.binding.Bind;
import com.tatait.tatamusic.utils.binding.ViewBinder;

import java.util.List;

/**
 * 收藏音乐列表适配器
 * Created by Lynn on 2015/12/22.
 */
public class CollectMusicAdapter extends BaseAdapter {
    private List<CollectMusic.Collect> mData;
    private OnMoreClickListener mListener;
    private int mPlayingPosition;

    public CollectMusicAdapter(List<CollectMusic.Collect> data) {
        this.mData = data;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_music, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (Actions.LIST_TYPE_COLLECT.equals(Preferences.getListType()) && position == mPlayingPosition) { //判断是否是收藏类型
            holder.vPlaying.setVisibility(View.VISIBLE);
            if (Preferences.isCollectChangeToLocal()) {
                holder.vPlaying.setVisibility(View.INVISIBLE);
                Preferences.saveCollectChangeToLocal(false);
            }
        } else {
            holder.vPlaying.setVisibility(View.INVISIBLE);
        }
        CollectMusic.Collect onlineMusic = mData.get(position);
        ImageLoader.getInstance().displayImage(onlineMusic.getPic_small(), holder.ivCover, ImageUtils.getCoverDisplayOptions());
        holder.tvTitle.setText(onlineMusic.getTitle());
        String artist = FileUtils.getArtistAndAlbum(onlineMusic.getArtist_name(), onlineMusic.getAlbum_title());
        holder.tvArtist.setText(artist);
        holder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMoreClick(position);
            }
        });
        holder.vDivider.setVisibility(isShowDivider(position) ? View.VISIBLE : View.GONE);
        return convertView;
    }

    private boolean isShowDivider(int position) {
        return position != mData.size() - 1;
    }

    public void updatePlayingPosition(PlayService playService) {
        if (playService.getPlayingMusic() != null && playService.getPlayingMusic().getType() == Music.Type.ONLINE) {
            if (!"-1".equals(Preferences.getFinalPosition()) && playService.getPlayingPosition() == 0) {
                mPlayingPosition = Integer.parseInt(Preferences.getFinalPosition());
            } else {
                mPlayingPosition = playService.getPlayingPosition();
            }
        } else {
            mPlayingPosition = -1;
        }
    }

    public void setOnMoreClickListener(OnMoreClickListener listener) {
        mListener = listener;
    }

    private static class ViewHolder {
        @Bind(R.id.v_playing)
        View vPlaying;
        @Bind(R.id.iv_cover)
        ImageView ivCover;
        @Bind(R.id.tv_title)
        TextView tvTitle;
        @Bind(R.id.tv_artist)
        TextView tvArtist;
        @Bind(R.id.iv_more)
        ImageView ivMore;
        @Bind(R.id.v_divider)
        View vDivider;

        private ViewHolder(View view) {
            ViewBinder.bind(this, view);
        }
    }
}