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

package ooo.oxo.apps.earth.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ooo.oxo.apps.earth.LegacyEarthSharedState;
import ooo.oxo.apps.earth.provider.SettingsContract;

public class Settings {

    public long interval;

    public int resolution;

    public boolean wifiOnly;

    public static Settings fromLegacySharedState(Context context) {
        LegacyEarthSharedState sharedState = new LegacyEarthSharedState(context);

        Settings settings = new Settings();

        settings.interval = sharedState.getInterval();
        settings.resolution = sharedState.getResolution();
        settings.wifiOnly = sharedState.getWifiOnly();

        return settings;
    }

    @Nullable
    public static Settings fromCursor(@NonNull Cursor cursor) {
        if (!cursor.moveToNext()) {
            return null;
        }

        Settings settings = new Settings();
        settings.interval = cursor.getLong(cursor.getColumnIndexOrThrow(SettingsContract.Columns.INTERVAL));
        settings.resolution = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.Columns.RESOLUTION));
        settings.wifiOnly = cursor.getInt(cursor.getColumnIndexOrThrow(SettingsContract.Columns.WIFI_ONLY)) != 0;

        return settings;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(SettingsContract.Columns.INTERVAL, interval);
        values.put(SettingsContract.Columns.RESOLUTION, resolution);
        values.put(SettingsContract.Columns.WIFI_ONLY, wifiOnly ? 1 : 0);
        return values;
    }

}
