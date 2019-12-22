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

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.net.ConnectivityManagerCompat;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.dao.Earth;
import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.provider.EarthsContract;
import ooo.oxo.apps.earth.provider.SettingsContract;

public class EarthSyncImpl {

    private static final String TAG = "EarthSyncImpl";

    public static final String RESULT_ERROR = "error";
    public static final String RESULT_DELAY_UNTIL = "delay_until";
    public static final String RESULT_INSERTS = "inserts";
    public static final String RESULT_DELETES = "deletes";

    public static final long ERROR_DB = 1L;
    public static final long ERROR_IO = 2L;

    private final Context context;

    private final PowerManager pm;
    private final ConnectivityManager cm;
    private final ContentResolver resolver;

    private final EarthFetcher fetcher;

    public EarthSyncImpl(Context context) {
        this.context = context;

        this.pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.resolver = context.getContentResolver();

        this.fetcher = new EarthFetcher(context);
    }

    public Map<String, Long> sync(boolean manual) {
        return sync(ContentInterface.from(context.getContentResolver()), manual);
    }

    public Map<String, Long> sync(ContentProviderClient client, boolean manual) {
        return sync(ContentInterface.from(client), manual);
    }

    private Map<String, Long> sync(ContentInterface content, boolean manual) {
        final Map<String, Long> result = new HashMap<>();
        final Settings settings = loadSettings();

        if (settings == null) {
            result.put(RESULT_ERROR, ERROR_DB);
            Log.e(TAG, "skipped sync since impossible null settings");
            return result;
        }

        final boolean metered = ConnectivityManagerCompat.isActiveNetworkMetered(cm);

        if (!manual && settings.wifiOnly && metered) {
            Log.d(TAG, "skipped sync until in non-metered connection");
            return result;
        }

        if (manual) {
            // Override interval limit for manual refresh
            settings.interval = TimeUnit.MINUTES.toMillis(10);
        }

        if (NetworkStateUtil.shouldConsiderSavingData(cm)) {
            // Override to lowest settings for data saver
            settings.interval = TimeUnit.MINUTES.toMillis(120);
            settings.resolution = 550;
        }

        final Earth latest = loadLatestEarth(content);
        if (latest != null) {
            final long syncUntil = latest.fetchedAt.getTime() + settings.interval;
            if (syncUntil > System.currentTimeMillis()) {
                result.put(RESULT_DELAY_UNTIL, TimeUnit.MILLISECONDS.toSeconds(syncUntil));
                Log.d(TAG, "delayed sync until " + new Date(syncUntil));
                return result;
            }
        }

        File fetched = null;

        try {
            fetched = fetcher.fetch(settings.resolution);

            final Earth earth = new Earth(fetched.getAbsolutePath());
            content.insert(EarthsContract.CONTENT_URI, earth.toContentValues());

            final int cleaned = content.delete(EarthsContract.OUTDATED_CONTENT_URI);

            sendOnTraffic(settings, fetched);

            result.put(RESULT_INSERTS, 1L);
            result.put(RESULT_DELETES, (long) cleaned);

            final long syncUntil = System.currentTimeMillis() + settings.interval;
            result.put(RESULT_DELAY_UNTIL, TimeUnit.MILLISECONDS.toSeconds(syncUntil));

            EarthAppWidgetProvider.triggerUpdate(context);

            Log.d(TAG, "done fetching earth, with " + cleaned + " outdated cleaned. next sync: "
                    + new Date(syncUntil));
        } catch (Exception e) {
            result.put(RESULT_ERROR, ERROR_IO);
            Log.e(TAG, "failed fetching earth", e);
        }

        sendOnFetch(settings, fetched != null);
        return result;
    }

    public void cancel() {
        fetcher.clean();
    }

    @Nullable
    private Settings loadSettings() {
        final Cursor cursor = resolver.query(SettingsContract.CONTENT_URI,
                null, null, null, null);

        if (cursor == null) {
            return null;
        }

        final Settings settings = Settings.fromCursor(cursor);

        cursor.close();

        return settings;
    }

    @Nullable
    private Earth loadLatestEarth(ContentInterface content) {
        final Cursor cursor = content.query(EarthsContract.LATEST_CONTENT_URI);

        if (cursor == null) {
            return null;
        }

        final Earth earth = Earth.fromCursor(cursor);

        cursor.close();

        return earth;
    }

    private void sendOnFetch(Settings settings, boolean success) {
        HashMap<String, String> event = new HashMap<>();

        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(settings.interval)));
        event.put("resolution", String.valueOf(settings.resolution));
        event.put("wifi_only", String.valueOf(settings.wifiOnly));

        event.put("screen_on", String.valueOf(pm.isScreenOn()));

        event.put("success", String.valueOf(success));

        MobclickAgent.onEvent(context, "fetch", event);
    }

    private void sendOnTraffic(Settings settings, File fetched) {
        HashMap<String, String> event = new HashMap<>();

        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(settings.interval)));
        event.put("resolution", String.valueOf(settings.resolution));
        event.put("wifi_only", String.valueOf(settings.wifiOnly));

        event.put("screen_on", String.valueOf(pm.isScreenOn()));

        MobclickAgent.onEventValue(context, "traffic", event, (int) fetched.length());
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    static abstract class ContentInterface {

        abstract Cursor query(Uri uri);

        abstract Uri insert(Uri uri, ContentValues values);

        abstract int delete(Uri uri);

        static ContentInterface from(ContentResolver resolver) {
            return new ContentInterface() {
                @Override
                Cursor query(Uri uri) {
                    return resolver.query(uri, null, null, null,
                            null);
                }

                @Override
                Uri insert(Uri uri, ContentValues values) {
                    return resolver.insert(uri, values);
                }

                @Override
                int delete(Uri uri) {
                    return resolver.delete(uri, null, null);
                }
            };
        }

        static ContentInterface from(ContentProviderClient client) {
            return new ContentInterface() {
                @Override
                Cursor query(Uri uri) {
                    try {
                        return client.query(uri, null, null, null,
                                null);
                    } catch (RemoteException e) {
                        return null;
                    }
                }

                @Override
                Uri insert(Uri uri, ContentValues values) {
                    try {
                        return client.insert(uri, values);
                    } catch (RemoteException e) {
                        return null;
                    }
                }

                @Override
                int delete(Uri uri) {
                    try {
                        return client.delete(uri, null, null);
                    } catch (RemoteException e) {
                        return 0;
                    }
                }
            };
        }

    }

}
