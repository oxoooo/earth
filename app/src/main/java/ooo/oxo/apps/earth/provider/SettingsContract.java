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

package ooo.oxo.apps.earth.provider;

import android.content.ContentResolver;
import android.net.Uri;

import ooo.oxo.apps.earth.BuildConfig;

@SuppressWarnings("WeakerAccess")
public class SettingsContract {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".settings";

    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .path("settings")
            .build();

    static final String TABLE = "settings";

    public static class Columns {
        public static final String INTERVAL = "interval";
        public static final String RESOLUTION = "resolution";
        public static final String WIFI_ONLY = "wifi_only";
        public static final String DEBUG = "debug";
        public static final String OFFSET_L = "offset_l";
        public static final String OFFSET_S = "offset_s";
        public static final String SCALE = "scale";
    }

}
