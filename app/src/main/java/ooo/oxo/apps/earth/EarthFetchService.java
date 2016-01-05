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

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.dao.Earth;
import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.provider.EarthsContract;
import ooo.oxo.apps.earth.provider.SettingsContract;

public class EarthFetchService extends IntentService {

    private static final String TAG = "EarthFetchService";

    private EarthFetcher fetcher;

    private PowerManager powerManager;

    public EarthFetchService() {
        super("Earth");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fetcher = new EarthFetcher(this);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Cursor cursor = getContentResolver().query(SettingsContract.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            throw new IllegalStateException("no settings, impossible");
        }

        Settings settings = Settings.fromCursor(cursor);

        cursor.close();

        if (settings == null) {
            throw new IllegalStateException("null settings, impossible");
        }

        if (settings.wifiOnly && !NetworkStateUtil.isWifiConnected(this)) {
            Log.d(TAG, "stop fetching since Wi-Fi is not enabled");
            EarthAlarmUtil.stop(this);
            return;
        }

        if (WallpaperUtil.isSupported(this) && !WallpaperUtil.isCurrent(this)) {
            Log.d(TAG, "stop fetching since it is not the current wallpaper");
            EarthAlarmUtil.stop(this);
            return;
        }

        File fetched = null;

        try {
            fetched = fetcher.fetch(settings.resolution);

            Earth earth = new Earth(fetched.getAbsolutePath());
            getContentResolver().insert(EarthsContract.CONTENT_URI, earth.toContentValues());

            sendOnTraffic(settings, fetched);

            Log.d(TAG, "done fetching earth");
        } catch (Exception e) {
            Log.e(TAG, "failed fetching earth", e);
        }

        sendOnFetch(settings, fetched != null);
    }

    private void sendOnFetch(Settings settings, boolean success) {
        HashMap<String, String> event = new HashMap<>();

        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(settings.interval)));
        event.put("resolution", String.valueOf(settings.resolution));
        event.put("wifi_only", String.valueOf(settings.wifiOnly));

        //noinspection deprecation
        event.put("screen_on", String.valueOf(powerManager.isScreenOn()));

        event.put("success", String.valueOf(success));

        MobclickAgent.onEvent(this, "fetch", event);
    }

    private void sendOnTraffic(Settings settings, File fetched) {
        HashMap<String, String> event = new HashMap<>();

        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(settings.interval)));
        event.put("resolution", String.valueOf(settings.resolution));
        event.put("wifi_only", String.valueOf(settings.wifiOnly));

        //noinspection deprecation
        event.put("screen_on", String.valueOf(powerManager.isScreenOn()));

        MobclickAgent.onEventValue(this, "traffic", event, (int) fetched.length());
    }

}
