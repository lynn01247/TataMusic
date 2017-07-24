package com.tatait.tatamusic.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tatait.tatamusic.R;
import com.tatait.tatamusic.constants.Extras;
import com.tatait.tatamusic.enums.LoadStateEnum;
import com.tatait.tatamusic.http.HttpCallback;
import com.tatait.tatamusic.http.HttpClient;
import com.tatait.tatamusic.model.ArtistInfo;
import com.tatait.tatamusic.utils.ViewUtils;
import com.tatait.tatamusic.utils.binding.Bind;

/**
 * ArtistInfoActivity
 * Created by Lynn on 2015/12/27.
 */
public class ArtistInfoActivity extends BaseActivity {
    @Bind(R.id.sv_artist_info)
    private ScrollView svArtistInfo;
    @Bind(R.id.ll_artist_info_container)
    private LinearLayout llArtistInfoContainer;
    @Bind(R.id.ll_loading)
    private LinearLayout llLoading;
    @Bind(R.id.ll_load_fail)
    private LinearLayout llLoadFail;

    public static void start(Context context, String tingUid) {
        Intent intent = new Intent(context, ArtistInfoActivity.class);
        intent.putExtra(Extras.TING_UID, tingUid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_info);

        if (!checkServiceAlive()) {
            return;
        }

        String tingUid = getIntent().getStringExtra(Extras.TING_UID);
        getArtistInfo(tingUid);
        ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOADING);
    }

    @Override
    protected void setListener() {
    }

    private void getArtistInfo(String tingUid) {
        HttpClient.getArtistInfo(tingUid, new HttpCallback<ArtistInfo>() {
            @Override
            public void onSuccess(ArtistInfo response) {
                if (response == null) {
                    onFail(null);
                    return;
                }
                ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_SUCCESS);
                setData(response);
            }

            @Override
            public void onFail(Exception e) {
                ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            }
        });
    }

    private void setData(ArtistInfo artistInfo) {
        String name = artistInfo.getName();
        String avatarUri = artistInfo.getAvatar_s1000();
        String country = artistInfo.getCountry();
        String constellation = artistInfo.getConstellation();
        float stature = artistInfo.getStature();
        float weight = artistInfo.getWeight();
        String birth = artistInfo.getBirth();
        String intro = artistInfo.getIntro();
        String url = artistInfo.getUrl();
        if (!TextUtils.isEmpty(avatarUri)) {
            ImageView ivAvatar = new ImageView(this);
            ivAvatar.setImageResource(R.drawable.default_artist);
            ivAvatar.setScaleType(ImageView.ScaleType.FIT_START);
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.default_artist)
                    .showImageForEmptyUri(R.drawable.default_artist)
                    .showImageOnFail(R.drawable.default_artist)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(avatarUri, ivAvatar, options);
            llArtistInfoContainer.addView(ivAvatar);
        }
        if (!TextUtils.isEmpty(name)) {
            setTitle(name);
            TextView tvName = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvName.setText(getString(R.string.artist_info_name, name));
            llArtistInfoContainer.addView(tvName);
        }
        if (!TextUtils.isEmpty(country)) {
            TextView tvCountry = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvCountry.setText(getString(R.string.artist_info_country, country));
            llArtistInfoContainer.addView(tvCountry);
        }
        if (!TextUtils.isEmpty(constellation) && !constellation.equals("未知")) {
            TextView tvConstellation = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvConstellation.setText(getString(R.string.artist_info_constellation, constellation));
            llArtistInfoContainer.addView(tvConstellation);
        }
        if (stature != 0f) {
            TextView tvStature = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvStature.setText(getString(R.string.artist_info_stature, String.valueOf(stature)));
            llArtistInfoContainer.addView(tvStature);
        }
        if (weight != 0f) {
            TextView tvWeight = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvWeight.setText(getString(R.string.artist_info_weight, String.valueOf(weight)));
            llArtistInfoContainer.addView(tvWeight);
        }
        if (!TextUtils.isEmpty(birth) && !birth.equals("0000-00-00")) {
            TextView tvBirth = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvBirth.setText(getString(R.string.artist_info_birth, birth));
            llArtistInfoContainer.addView(tvBirth);
        }
        if (!TextUtils.isEmpty(intro)) {
            TextView tvIntro = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            tvIntro.setText(getString(R.string.artist_info_intro, intro));
            llArtistInfoContainer.addView(tvIntro);
        }
        if (!TextUtils.isEmpty(url)) {
            TextView tvUrl = (TextView) LayoutInflater.from(this).inflate(R.layout.item_artist_info, llArtistInfoContainer, false);
            String html = "<font color='#2196F3'><a href='%s'>查看更多信息</a></font>";
            tvUrl.setText(Html.fromHtml(String.format(html, url)));
            tvUrl.setMovementMethod(LinkMovementMethod.getInstance());
            tvUrl.setGravity(Gravity.CENTER);
            llArtistInfoContainer.addView(tvUrl);
        }

        if (llArtistInfoContainer.getChildCount() == 0) {
            ViewUtils.changeViewState(svArtistInfo, llLoading, llLoadFail, LoadStateEnum.LOAD_FAIL);
            ((TextView) llLoadFail.findViewById(R.id.tv_load_fail_text)).setText(R.string.artist_info_empty);
        }
    }
}