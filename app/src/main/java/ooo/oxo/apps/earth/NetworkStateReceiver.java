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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.provider.SettingsContract;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = context.getContentResolver().query(SettingsContract.CONTENT_URI, null, null, null, null);

        if (cursor == null) {
            Log.e(TAG, "no settings, impossible");
            return;
        }

        Settings settings = Settings.fromCursor(cursor);

        cursor.close();

        if (settings == null) {
            Log.e(TAG, "null settings, impossible");
            return;
        }

        if (settings.wifiOnly) {
            if (NetworkStateUtil.isWifiConnected(context)) {
                Log.d(TAG, "network state changed: Wi-Fi on, scheduling fetch...");
                EarthAlarmUtil.schedule(context, settings.interval);
            } else {
                Log.d(TAG, "network state changed: Wi-Fi off, stop fetching");
                EarthAlarmUtil.stop(context);
            }
        }
    }

}
