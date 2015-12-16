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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class EarthFetchService extends IntentService {

    private static final String TAG = "EarthFetchService";

    private EarthFetcher fetcher;

    private EarthSharedState sharedState;

    private LocalBroadcastManager bm;

    public EarthFetchService() {
        super("Earth");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fetcher = new EarthFetcher(this);
        sharedState = EarthSharedState.getInstance(this);
        bm = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (sharedState.getWifiOnly() && !NetworkStateUtil.isWifiConnected(this)) {
            Log.d(TAG, "stop fetching since Wi-Fi is not enabled");
            EarthAlarmUtil.stop(this);
            return;
        }

        if (!WallpaperUtil.isCurrent(this)) {
            Log.d(TAG, "stop fetching since it is not the current wallpaper");
            EarthAlarmUtil.stop(this);
            return;
        }

        try {
            if (sharedState.setLastEarth(fetcher.fetch().getAbsolutePath())) {
                bm.sendBroadcast(new Intent(EarthIntents.ACTION_NEW_EARTH_READY));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed fetching earth", e);
        }

        HashMap<String, String> event = new HashMap<>();
        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(sharedState.getInterval())));
        event.put("wifi_only", String.valueOf(sharedState.getWifiOnly()));
        MobclickAgent.onEvent(this, "fetch", event);
    }

}
