package com.tatait.tatamusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.Toast;

public class MusicPlayerActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.load);

		ImageView loadImage = (ImageView) findViewById(R.id.loadImage);
		// 设置透明度渐变效果(0.0-1.0)
		AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
		// 设置动画持续时间
		animation.setDuration(3000);
		// 将组件与动画进行关联
		loadImage.setAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {
			// 动画开始
			public void onAnimationStart(Animation animation) {
				Toast.makeText(MusicPlayerActivity.this, R.string.welcome,
						Toast.LENGTH_SHORT).show();
			}

			// 动画结束
			public void onAnimationEnd(Animation animation) {
				Intent first = new Intent(getBaseContext(), MusicPlay.class);
				startActivity(first);
				finish();
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}
		});
	}
}