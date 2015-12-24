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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import ooo.oxo.apps.earth.BuildConfig;
import ooo.oxo.apps.earth.dao.Settings;

public class SettingsProvider extends ContentProvider {

    private SettingsDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new SettingsDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.item/vnd." + BuildConfig.APPLICATION_ID + ".settings";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(SettingsContract.TABLE);

        Cursor cursor = builder.query(db, projection, null, null, null, null, null, "1");

        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int affected = db.update(SettingsContract.TABLE, values, null, null);

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null, false);

        return affected;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private class SettingsDatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = "SettingsDatabaseHelper";

        private static final String DATABASE_NAME = "settings.db";
        private static final int DATABASE_VERSION = 1;

        public SettingsDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + SettingsContract.TABLE +
                    " (" + SettingsContract.Columns.INTERVAL + " INTEGER" +
                    ", " + SettingsContract.Columns.RESOLUTION + " INTEGER" +
                    ", " + SettingsContract.Columns.WIFI_ONLY + " INTEGER" +
                    ")");

            db.insertOrThrow(SettingsContract.TABLE, null,
                    Settings.fromLegacySharedState(getContext()).toContentValues());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.e(TAG, "no need to upgrade currently");
        }

    }

}
