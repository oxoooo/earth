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
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;

import ooo.oxo.apps.earth.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.main_activity);

        setSupportActionBar(binding.toolbar);

        EarthSharedState sharedState = EarthSharedState.getInstance(this);

        MainViewModel vm = new MainViewModel(sharedState);

        binding.setVm(vm);

        binding.done.setOnClickListener(v -> {
            vm.saveTo(sharedState);

            if (WallpaperUtil.isCurrent(this)) {
                Log.d(TAG, "already set, just refresh everything");
                EarthAlarmUtil.reschedule(this);
            } else if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
                Log.d(TAG, "from launcher, prompt to change wallpaper");
                WallpaperUtil.changeLiveWallPaper(this);
            } else {
                Log.d(TAG, "may be from settings, just refresh");
                EarthAlarmUtil.reschedule(this);
                setResult(RESULT_OK);
            }

            supportFinishAfterTransition();
        });

        String lastEarth = sharedState.getLastEarth();

        if (!TextUtils.isEmpty(lastEarth)) {
            Glide.with(this).load(lastEarth).into(binding.earth);
        }

        UpdateUtil.checkForUpdateAndPrompt(this);
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
