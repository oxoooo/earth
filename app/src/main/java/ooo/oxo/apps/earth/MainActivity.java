/*
 * Mantou Earth - Live your wallpaper with live earth
 * Copyright (C) 2015  XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.apps.earth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.databinding.MainActivityBinding;
import ooo.oxo.apps.earth.provider.EarthsContract;
import ooo.oxo.apps.earth.provider.SettingsContract;
import ooo.oxo.apps.earth.view.InOutAnimationUtils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MainActivityBinding binding;

    private MainViewModel vm = new MainViewModel();

    private ContentObserver observer = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            runOnUiThread(() -> loadEarth());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);

        setTitle(null);
        setSupportActionBar(binding.toolbar.toolbar);

        binding.setVm(vm);
        binding.setAccelerated(BuildConfig.USE_OXO_SERVER);

        binding.action.done.setOnClickListener(v -> saveSettings());

        loadSettings();

        loadEarth();

        UpdateUtil.checkForUpdateAndPrompt(this);
    }

    private void loadSettings() {
        Cursor cursor = getContentResolver().query(
                SettingsContract.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            throw new IllegalStateException();
        }

        Settings settings = Settings.fromCursor(cursor);

        cursor.close();

        if (settings == null) {
            throw new IllegalStateException();
        }

        vm.loadFrom(settings);

        if (isFromSettings() || shouldPromptToChangeWallpaper()) {
            showSettings();
        }
    }

    private void saveSettings() {
        Settings settings = new Settings();

        vm.saveTo(settings);

        getContentResolver().update(SettingsContract.CONTENT_URI,
                settings.toContentValues(), null, null);

        EarthAlarmUtil.reschedule(this, settings.interval);

        sendOnSet(settings);

        if (isFromSettings()) {
            if (isCurrentWallpaper()) {
                setResult(RESULT_OK);
                supportFinishAfterTransition();
            } else {
                setResult(RESULT_OK);
                supportFinishAfterTransition();
                WallpaperUtil.changeLiveWallPaper(this);
            }
        } else if (shouldPromptToChangeWallpaper()) {
            setResult(RESULT_OK);
            supportFinishAfterTransition();
            WallpaperUtil.changeLiveWallPaper(this);
        } else {
            animateOutSettings();
        }
    }

    private void showSettings() {
        binding.toolbar.getRoot().setVisibility(View.INVISIBLE);
        binding.settings.setVisibility(View.VISIBLE);
        binding.action.done.setVisibility(View.VISIBLE);
        binding.action.doneHint.setVisibility(View.VISIBLE);
    }

    private void hideSettings() {
        binding.settings.setVisibility(View.INVISIBLE);
        binding.action.done.setVisibility(View.INVISIBLE);
        binding.action.doneHint.setVisibility(View.INVISIBLE);
        binding.toolbar.getRoot().setVisibility(View.VISIBLE);
    }

    private boolean isSettingsShown() {
        return binding.settings.getVisibility() == View.VISIBLE;
    }

    private void animateInSettings() {
        InOutAnimationUtils.animateOut(binding.toolbar.getRoot(), R.anim.main_toolbar_out);

        binding.action.done.postDelayed(() -> binding.action.done.show(), 300L);

        InOutAnimationUtils.animateIn(binding.action.doneHint, R.anim.main_action_hint_in);
        InOutAnimationUtils.animateIn(binding.settings, R.anim.main_settings_in);

        if (Build.VERSION.SDK_INT >= 21) {
            int width = binding.settings.getWidth();
            int height = binding.settings.getHeight();

            int revealOffset = getResources().getDimensionPixelOffset(R.dimen.settings_reveal_offset);

            Animator animator = ViewAnimationUtils.createCircularReveal(
                    binding.settings,
                    width - revealOffset, height - revealOffset,
                    0, Math.max(width, height));

            binding.settings.setVisibility(View.VISIBLE);

            animator.setDuration(300L);

            animator.start();
        }
    }

    private void animateOutSettings() {
        InOutAnimationUtils.animateIn(binding.toolbar.getRoot(), R.anim.main_toolbar_in);

        binding.action.done.hide();

        InOutAnimationUtils.animateOut(binding.action.doneHint, R.anim.main_action_hint_out);
        InOutAnimationUtils.animateOut(binding.settings, R.anim.main_settings_out);

        if (Build.VERSION.SDK_INT >= 21) {
            int width = binding.settings.getWidth();
            int height = binding.settings.getHeight();

            int revealOffset = getResources().getDimensionPixelOffset(R.dimen.settings_reveal_offset);

            Animator animator = ViewAnimationUtils.createCircularReveal(
                    binding.settings,
                    width - revealOffset, height - revealOffset,
                    Math.max(width, height), 0);

            animator.setDuration(300L);

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    binding.settings.setVisibility(View.INVISIBLE);
                }
            });

            animator.start();
        }
    }

    @Override
    public void onBackPressed() {
        if (isSettingsShown() && !(isFromSettings() || shouldPromptToChangeWallpaper())) {
            animateOutSettings();
        } else {
            super.onBackPressed();
        }
    }

    private void sendOnSet(Settings settings) {
        HashMap<String, String> event = new HashMap<>();

        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(settings.interval)));
        event.put("resolution", String.valueOf(settings.resolution));
        event.put("wifi_only", String.valueOf(settings.wifiOnly));

        MobclickAgent.onEvent(this, "set", event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        getContentResolver().registerContentObserver(
                EarthsContract.LATEST_CONTENT_URI, false, observer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        getContentResolver().unregisterContentObserver(observer);
    }

    private void loadEarth() {
        Glide.with(this).load(EarthsContract.LATEST_CONTENT_URI
                .buildUpon()
                .appendQueryParameter("t", String.valueOf(System.currentTimeMillis()))
                .build())
                .error(R.drawable.preview)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.earth.earth);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.saveState(outState);
        outState.putBoolean("settings_shown", isSettingsShown());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        vm.restoreState(savedInstanceState);
        if (savedInstanceState.getBoolean("settings_shown", false)) {
            showSettings();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.settings:
                animateInSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isCurrentWallpaper() {
        return WallpaperUtil.isCurrent(this);
    }

    private boolean isWallpaperSupported() {
        return WallpaperUtil.isSupported(this);
    }

    private boolean shouldPromptToChangeWallpaper() {
        return isWallpaperSupported() && !isCurrentWallpaper();
    }

    private boolean isFromSettings() {
        return !Intent.ACTION_MAIN.equals(getIntent().getAction());
    }

}
