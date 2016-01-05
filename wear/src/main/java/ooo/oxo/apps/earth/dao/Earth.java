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
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Date;

import ooo.oxo.apps.earth.provider.EarthsContract;

public class Earth {

    public String file;

    public Date fetchedAt = new Date();

    public Earth() {
    }

    public Earth(String file) {
        this.file = file;
    }

    @Nullable
    public static Earth fromCursor(@NonNull Cursor cursor) {
        if (!cursor.moveToNext()) {
            return null;
        }

        Earth earth = new Earth();
        earth.file = cursor.getString(cursor.getColumnIndexOrThrow(EarthsContract.Columns.FILE));
        earth.fetchedAt = new Date(cursor.getLong(cursor.getColumnIndexOrThrow(EarthsContract.Columns.FETCHED_AT)));

        if (TextUtils.isEmpty(earth.file)) {
            return null;
        }

        return earth;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(EarthsContract.Columns.FILE, file);
        values.put(EarthsContract.Columns.FETCHED_AT, fetchedAt.getTime());
        return values;
    }

}
