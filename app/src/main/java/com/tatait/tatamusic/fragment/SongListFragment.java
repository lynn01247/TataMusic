package com.tatait.tatamusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.activity.MusicActivity;
import com.tatait.tatamusic.activity.OnlineMusicActivity;
import com.tatait.tatamusic.adapter.SongListAdapter;
import com.tatait.tatamusic.application.AppCache;
import com.tatait.tatamusic.constants.Extras;
import com.tatait.tatamusic.enums.LoadStateEnum;
import com.tatait.tatamusic.model.SongListInfo;
import com.tatait.tatamusic.utils.NetworkUtils;
import com.tatait.tatamusic.utils.ViewUtils;
import com.tatait.tatamusic.utils.binding.Bind;

import java.util.List;

/**
 * 在线音乐
 * Created by Lynn on 2015/11/26.
 */
public class SongListFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @Bind(R.id.lv_song_list)
    private ListView lvSongList;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;
    private List<SongListInfo> mSongLists;
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
        view = inflater.inflate(R.layout.fragment_song_list, container, false);
        return view;
    }

    @Override
    protected void init() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            ViewUtils.changeViewState(lvSongList, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            return;
        }
        mSongLists = AppCache.getSongListInfos();
        if (mSongLists.isEmpty()) {
            String[] titles = getResources().getStringArray(R.array.online_music_list_title);
            String[] types = getResources().getStringArray(R.array.online_music_list_type);
            for (int i = 0; i < titles.length; i++) {
                SongListInfo info = new SongListInfo();
                info.setTitle(titles[i]);
                info.setType(types[i]);
                mSongLists.add(info);
            }
        }
        SongListAdapter adapter = new SongListAdapter(mSongLists);
        lvSongList.setAdapter(adapter);
    }

    @Override
    protected void setListener() {
        lvSongList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SongListInfo songListInfo = mSongLists.get(position);
        ((MusicActivity) getActivity()).resetPlayListFragment();// 重置fragment，为了加入列表
        Intent intent = new Intent(getContext(), OnlineMusicActivity.class);
        intent.putExtra(Extras.MUSIC_LIST_TYPE, songListInfo);
        startActivity(intent);
    }
}