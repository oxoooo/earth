package ooo.oxo.apps.earth.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.net.ConnectivityManagerCompat;
import android.util.Log;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.EarthFetcher;
import ooo.oxo.apps.earth.NetworkStateUtil;
import ooo.oxo.apps.earth.dao.Earth;
import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.provider.EarthsContract;
import ooo.oxo.apps.earth.provider.SettingsContract;

public class EarthsSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "EarthsSyncAdapter";

    private final Context context;

    private final PowerManager pm;
    private final ConnectivityManager cm;
    private final ContentResolver resolver;

    private final EarthFetcher fetcher;

    public EarthsSyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public EarthsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        this.context = context;

        this.pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.resolver = context.getContentResolver();

        this.fetcher = new EarthFetcher(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult result) {
        final Settings settings = loadSettings();

        if (settings == null) {
            result.databaseError = true;
            Log.e(TAG, "skipped sync since impossible null settings");
            return;
        }

        final boolean manual = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);
        final boolean metered = ConnectivityManagerCompat.isActiveNetworkMetered(cm);

        if (!manual && settings.wifiOnly && metered) {
            Log.d(TAG, "skipped sync until in non-metered connection");
            return;
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

        final Earth latest = loadLatestEarth();
        if (latest != null) {
            final long syncUntil = (latest.fetchedAt.getTime() + settings.interval) - System.currentTimeMillis();
            if (syncUntil > 0) {
                result.delayUntil = TimeUnit.MILLISECONDS.toSeconds(syncUntil);
                Log.d(TAG, "delayed sync until " + TimeUnit.MILLISECONDS.toMinutes(syncUntil) + " minutes later");
                return;
            }
        }

        File fetched = null;

        try {
            fetched = fetcher.fetch(settings.resolution);

            final Earth earth = new Earth(fetched.getAbsolutePath());
            provider.insert(EarthsContract.CONTENT_URI, earth.toContentValues());

            final int cleaned = provider.delete(EarthsContract.OUTDATED_CONTENT_URI, null, null);

            sendOnTraffic(settings, fetched);

            result.stats.numInserts++;
            result.stats.numDeletes = cleaned;

            result.delayUntil = TimeUnit.MILLISECONDS.toSeconds(settings.interval);

            Log.d(TAG, "done fetching earth, with " + cleaned + " outdated cleaned");
        } catch (Exception e) {
            result.stats.numIoExceptions++;
            Log.e(TAG, "failed fetching earth", e);
        }

        sendOnFetch(settings, fetched != null);
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        fetcher.clean();
    }

    @Nullable
    private Settings loadSettings() {
        final Cursor cursor = resolver.query(SettingsContract.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            return null;
        }

        final Settings settings = Settings.fromCursor(cursor);

        cursor.close();

        return settings;
    }

    @Nullable
    private Earth loadLatestEarth() {
        final Cursor cursor = resolver.query(EarthsContract.LATEST_CONTENT_URI, null, null, null, null);

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

        //noinspection deprecation
        event.put("screen_on", String.valueOf(pm.isScreenOn()));

        event.put("success", String.valueOf(success));

        MobclickAgent.onEvent(context, "fetch", event);
    }

    private void sendOnTraffic(Settings settings, File fetched) {
        HashMap<String, String> event = new HashMap<>();

        event.put("interval", String.valueOf(TimeUnit.MILLISECONDS.toMinutes(settings.interval)));
        event.put("resolution", String.valueOf(settings.resolution));
        event.put("wifi_only", String.valueOf(settings.wifiOnly));

        //noinspection deprecation
        event.put("screen_on", String.valueOf(pm.isScreenOn()));

        MobclickAgent.onEventValue(context, "traffic", event, (int) fetched.length());
    }

}
