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

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.databinding.MainActivityBinding;
import ooo.oxo.apps.earth.provider.EarthsContract;
import ooo.oxo.apps.earth.provider.SettingsContract;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private MainActivityBinding binding;

    private MainViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.main_activity);

        setSupportActionBar(binding.toolbar);

        Cursor cursor = getContentResolver().query(SettingsContract.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            throw new IllegalStateException();
        }

        Settings settings = Settings.fromCursor(cursor);

        cursor.close();

        if (settings == null) {
            throw new IllegalStateException();
        }

        vm = new MainViewModel(settings);

        binding.setVm(vm);
        binding.setAccelerated(BuildConfig.USE_OXO_SERVER);

        binding.done.setOnClickListener(v -> save());

        loadEarth();

        UpdateUtil.checkForUpdateAndPrompt(this);
    }

    private void save() {
        Settings settings = new Settings();
        vm.saveTo(settings);

        getContentResolver().update(SettingsContract.CONTENT_URI, settings.toContentValues(), null, null);

        if (WallpaperUtil.isCurrent(this)) {
            Log.d(TAG, "already set, just refresh everything");
            EarthAlarmUtil.reschedule(this, vm.getInterval());
        } else if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            Log.d(TAG, "from launcher, prompt to change wallpaper");
            WallpaperUtil.changeLiveWallPaper(this);
        } else {
            Log.d(TAG, "may be from settings, just refresh");
            EarthAlarmUtil.reschedule(this, vm.getInterval());
            setResult(RESULT_OK);
        }

        sendOnSet();

        supportFinishAfterTransition();
    }

    private void sendOnSet() {
        HashMap<String, String> event = new HashMap<>();

        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(vm.getInterval())));
        event.put("resolution", String.valueOf(vm.getResolution()));
        event.put("wifi_only", String.valueOf(vm.isWifiOnly()));

        MobclickAgent.onEvent(this, "set", event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void loadEarth() {
        Glide.with(this).load(EarthsContract.LATEST_CONTENT_URI)
                .error(R.drawable.preview)
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .into(binding.earth);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        vm.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        vm.restoreState(savedInstanceState);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
