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

package ooo.oxo.apps.earth.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import java.util.Map;

import ooo.oxo.apps.earth.EarthSyncImpl;

import static ooo.oxo.apps.earth.EarthSyncImpl.ERROR_DB;
import static ooo.oxo.apps.earth.EarthSyncImpl.ERROR_IO;
import static ooo.oxo.apps.earth.EarthSyncImpl.RESULT_DELAY_UNTIL;
import static ooo.oxo.apps.earth.EarthSyncImpl.RESULT_DELETES;
import static ooo.oxo.apps.earth.EarthSyncImpl.RESULT_ERROR;
import static ooo.oxo.apps.earth.EarthSyncImpl.RESULT_INSERTS;

public class EarthsSyncAdapter extends AbstractThreadedSyncAdapter {

    private final EarthSyncImpl syncer;

    EarthsSyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    private EarthsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.syncer = new EarthSyncImpl(context);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult result) {
        final boolean manual = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL);
        final Map<String, Long> synced = syncer.sync(provider, manual);

        if (synced.containsKey(RESULT_ERROR)) {
            final long error = synced.get(RESULT_ERROR);
            if (error == ERROR_DB) {
                result.databaseError = true;
            } else if (error == ERROR_IO) {
                result.stats.numIoExceptions++;
            }
            return;
        }

        if (synced.containsKey(RESULT_INSERTS) && synced.containsKey(RESULT_DELETES)) {
            result.stats.numInserts = synced.get(RESULT_INSERTS);
            result.stats.numDeletes = synced.get(RESULT_DELETES);
        }

        if (synced.containsKey(RESULT_DELAY_UNTIL)) {
            result.delayUntil = synced.get(RESULT_DELAY_UNTIL);
        }
    }

    @Override
    public void onSyncCanceled() {
        super.onSyncCanceled();
        syncer.cancel();
    }

}
