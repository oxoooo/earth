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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.BuildConfig;
import ooo.oxo.apps.earth.LegacyEarthSharedState;
import ooo.oxo.apps.earth.dao.Earth;

public class EarthsProvider extends ContentProvider {

    private static final String TAG = "EarthsProvider";

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        matcher.addURI(EarthsContract.AUTHORITY, "earths", 1);
        matcher.addURI(EarthsContract.AUTHORITY, "earths/latest", 2);
        matcher.addURI(EarthsContract.AUTHORITY, "earths/#", 3);
    }

    private EarthsDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new EarthsDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (matcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/vnd." + BuildConfig.APPLICATION_ID + ".earths";
            case 2:
            case 3:
                return "vnd.android.cursor.item/vnd." + BuildConfig.APPLICATION_ID + ".earths";
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (matcher.match(uri)) {
            case 1:
                return queryEarths(db, uri, projection, selection, selectionArgs);
            case 2:
                return queryEarthLatest(db, uri, projection);
            case 3:
                return queryEarthItem(db, uri, projection);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private Cursor queryEarths(SQLiteDatabase db, @NonNull Uri uri, String[] projection, String selection, String[] selectionArgs) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(EarthsContract.TABLE);

        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, EarthsContract.Columns.FETCHED_AT + " DESC", null);

        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private Cursor queryEarthLatest(SQLiteDatabase db, @NonNull Uri uri, String[] projection) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(EarthsContract.TABLE);

        Cursor cursor = builder.query(db, projection, null, null, null, null, EarthsContract.Columns.FETCHED_AT + " DESC", "1");

        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private Cursor queryEarthItem(SQLiteDatabase db, @NonNull Uri uri, String[] projection) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(EarthsContract.TABLE);

        long id = ContentUris.parseId(uri);

        Cursor cursor = builder.query(db, projection, EarthsContract.Columns._ID + " = ?", new String[]{
                String.valueOf(id)
        }, null, null, null);

        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (matcher.match(uri) != 1) {
            throw new UnsupportedOperationException();
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        long row;

        try {
            row = db.insertOrThrow(EarthsContract.TABLE, null, values);

            Log.d(TAG, "inserted new earth " + row + " into " + uri);

            int deleted = db.delete(EarthsContract.TABLE, EarthsContract.Columns.FETCHED_AT + " < ?", new String[]{
                    String.valueOf(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2))
            });

            Log.d(TAG, "cleaned " + deleted + " outdated earths");

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "failed inserting new earth", e);
            return null;
        } finally {
            db.endTransaction();
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null, false);

        return ContentUris.withAppendedId(uri, row);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new UnsupportedOperationException();
        }

        int type = matcher.match(uri);

        if (type != 2 && type != 3) {
            throw new UnsupportedOperationException();
        }

        Cursor cursor = query(uri, null, null, null, null);

        if (cursor == null) {
            throw new FileNotFoundException();
        }

        Earth earth = Earth.fromCursor(cursor);

        cursor.close();

        if (earth == null) {
            throw new FileNotFoundException();
        }

        File file = new File(earth.file);

        if (!file.isFile()) {
            throw new FileNotFoundException();
        }

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    private class EarthsDatabaseHelper extends SQLiteOpenHelper {

        private static final String TAG = "EarthsDatabaseHelper";

        private static final String DATABASE_NAME = "earths.db";
        private static final int DATABASE_VERSION = 1;

        public EarthsDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + EarthsContract.TABLE +
                    " (" + EarthsContract.Columns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                    ", " + EarthsContract.Columns.FILE + " TEXT" +
                    ", " + EarthsContract.Columns.FETCHED_AT + " INTEGER" +
                    ")");

            LegacyEarthSharedState state = new LegacyEarthSharedState(getContext());

            ContentValues values = new ContentValues();

            values.put(EarthsContract.Columns.FILE, state.getLastEarth());
            values.put(EarthsContract.Columns.FETCHED_AT, System.currentTimeMillis());

            db.insertOrThrow(EarthsContract.TABLE, null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.e(TAG, "no need to upgrade currently");
        }

    }

}
