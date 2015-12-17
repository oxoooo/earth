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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import java.util.concurrent.TimeUnit;

public class EarthSharedState {

    private static EarthSharedState instance;

    private final Context context;
    private final SharedPreferences preferences;

    private EarthSharedState(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("earth", Context.MODE_PRIVATE);
    }

    public static EarthSharedState getInstance(Context context) {
        if (instance == null) {
            instance = new EarthSharedState(context);
        }

        return instance;
    }

    @Nullable
    public String getLastEarth() {
        return preferences.getString("last_earth", null);
    }

    public boolean setLastEarth(@NonNull String earth) {
        return preferences.edit().putString("last_earth", earth).commit();
    }

    public long getInterval() {
        return preferences.getLong("interval", TimeUnit.MINUTES.toMillis(10));
    }

    public boolean setInterval(long interval) {
        return preferences.edit().putLong("interval", interval).commit();
    }

    public int getResolution() {
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.USE_OXO_SERVER) {
            return 550;
        }

        int resolution = preferences.getInt("resolution", 0);

        if (resolution == 0) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            resolution = ResolutionUtil.findBestResolution(metrics);
            setResolution(resolution);
        }

        return resolution;
    }

    public boolean setResolution(int resolution) {
        return preferences.edit().putInt("resolution", resolution).commit();
    }

    public boolean getWifiOnly() {
        return preferences.getBoolean("wifi_only", true);
    }

    public boolean setWifiOnly(boolean wifiOnly) {
        return preferences.edit().putBoolean("wifi_only", wifiOnly).commit();
    }

}
