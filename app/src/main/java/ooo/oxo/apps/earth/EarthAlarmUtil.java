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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class EarthAlarmUtil {

    private static final String TAG = "EarthAlarmUtil";

    public static void schedule(Context context) {
        long interval = EarthSharedState.getInstance(context).getInterval();

        Log.d(TAG, "schedule fetching service repeating every "
                + TimeUnit.MILLISECONDS.toMinutes(interval) + " minutes");

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                interval, makePendingIntent(context));
    }

    public static void stop(Context context) {
        Log.d(TAG, "stop fetching service");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(makePendingIntent(context));
    }

    public static void reschedule(Context context) {
        stop(context);
        schedule(context);
    }

    private static PendingIntent makePendingIntent(Context context) {
        return PendingIntent.getService(
                context, 0, new Intent(context, EarthFetchService.class), 0);
    }

}
