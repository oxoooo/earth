/*
 * Mantou Earth - Live your wallpaper with live earth
 * Copyright (C) 2015-2019 XiNGRZ <xxx@oxo.ooo>
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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

public class LegacyEarthSharedState {

    @SuppressWarnings("unused")
    private static final String TAG = "LegacyEarthSharedState";

    private final Context context;
    private final SharedPreferences preferences;

    public LegacyEarthSharedState(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("earth", Context.MODE_PRIVATE);
    }

    @Nullable
    public String getLastEarth() {
        return preferences.getString("last_earth", null);
    }

    public long getInterval() {
        return preferences.getLong("interval", TimeUnit.MINUTES.toMillis(10));
    }

    public int getResolution() {
        if (!BuildConfig.USE_OXO_SERVER) {
            return 550;
        }

        int resolution = preferences.getInt("resolution", 0);

        if (resolution == 0) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            resolution = Resolutions.findBestResolution(metrics);
        }

        return resolution;
    }

    public boolean getWifiOnly() {
        return preferences.getBoolean("wifi_only", true);
    }

}
