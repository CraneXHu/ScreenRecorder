package com.pkhope.screenrecorder.activity;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.pkhope.screenrecorder.R;

/**
 * Created by thinkpad on 2016/1/28.
 */
public class SettingsActivity extends BaseActivity {

    private SettingFragment mSettingFragment;
    private static boolean bStartActivity = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null){
            mSettingFragment = new SettingFragment();
            replaceFragment(R.id.layout_setting_container,mSettingFragment);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void replaceFragment(int viewId, Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).commit();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    static public class SettingFragment extends PreferenceFragment {

//        private SharedPreferences mSharePreference;
        private ListPreference mFormat;
        private ListPreference mResolution;
        private ListPreference mFrameRate;
        private ListPreference mBitRate;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            mFormat = (ListPreference)findPreference("video_format");
            mFormat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (preference instanceof ListPreference) {
                        ListPreference lp = (ListPreference) preference;
                        CharSequence[] entries = lp.getEntries();
                        int index = lp.findIndexOfValue((String) newValue);
                        lp.setSummary(entries[index]);
                    }

                    return true;
                }
            });
            mFormat.setSummary(mFormat.getEntry());

            mResolution = (ListPreference)findPreference("resolution");
            mResolution.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (preference instanceof ListPreference) {
                        ListPreference lp = (ListPreference) preference;
                        CharSequence[] entries = lp.getEntries();
                        int index = lp.findIndexOfValue((String) newValue);
                        lp.setSummary(entries[index]);
                    }

                    return true;
                }
            });
            mResolution.setSummary(mResolution.getEntry());

            mFrameRate = (ListPreference)findPreference("frame_rate");
            mFrameRate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (preference instanceof ListPreference) {
                        ListPreference lp = (ListPreference) preference;
                        CharSequence[] entries = lp.getEntries();
                        int index = lp.findIndexOfValue((String) newValue);
                        lp.setSummary(entries[index]);
                    }

                    return true;
                }
            });
            mFrameRate.setSummary(mFrameRate.getEntry());

            mBitRate = (ListPreference)findPreference("bit_rate");
            mBitRate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if (preference instanceof ListPreference) {
                        ListPreference lp = (ListPreference) preference;
                        CharSequence[] entries = lp.getEntries();
                        int index = lp.findIndexOfValue((String) newValue);
                        lp.setSummary(entries[index]);
                    }

                    return true;
                }
            });
            mBitRate.setSummary(mBitRate.getEntry());

            Preference feedback = findPreference("feedback");
            feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    bStartActivity = true;
                    Intent intent = new Intent(getActivity(), FeedBackActivity.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference about = findPreference("about");
            about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    bStartActivity = true;
                    Intent intent = new Intent(getActivity(),AboutActivity.class);
                    startActivity(intent);

                    return false;
                }
            });

            Preference help = findPreference("help");
            help.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    bStartActivity = true;
                    Intent intent = new Intent(getActivity(),HelpActivity.class);
                    startActivity(intent);

                    return false;
                }
            });

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!bStartActivity){

            finish();
        }
        bStartActivity = false;
    }

}
