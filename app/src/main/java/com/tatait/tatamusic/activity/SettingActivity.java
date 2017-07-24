package com.tatait.tatamusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.tatait.tatamusic.R;
import com.tatait.tatamusic.utils.ToastUtils;

/**
 * SettingActivity
 * Created by Lynn on 2015/12/27.
 */
public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        if (!checkServiceAlive()) {
            return;
        }

        getFragmentManager().beginTransaction().replace(R.id.ll_fragment_container, new SettingFragment()).commit();
    }

    public static class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private Preference mSoundEffect;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_setting);

            mSoundEffect = findPreference(getString(R.string.setting_key_sound_effect));
            mSoundEffect.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == mSoundEffect) {
                Intent intent = new Intent("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL");
                intent.putExtra("android.media.extra.PACKAGE_NAME", getActivity().getPackageName());
                intent.putExtra("android.media.extra.CONTENT_TYPE", 0);
                intent.putExtra("android.media.extra.AUDIO_SESSION", 0);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.show(R.string.device_not_support);
                }
                return true;
            }
            return false;
        }
    }
}