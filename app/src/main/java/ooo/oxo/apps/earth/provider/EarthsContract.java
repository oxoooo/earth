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

package ooo.oxo.apps.earth.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import ooo.oxo.apps.earth.BuildConfig;

public class EarthsContract {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".earths";

    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .path("earths")
            .build();

    public static final Uri LATEST_CONTENT_URI = Uri.withAppendedPath(CONTENT_URI, "latest");

    static final String TABLE = "earths";

    public static class Columns implements BaseColumns {
        public static final String FILE = "file";
        public static final String FETCHED_AT = "fetched_at";
    }

}
