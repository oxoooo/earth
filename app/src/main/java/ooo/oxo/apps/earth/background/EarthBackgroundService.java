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

package ooo.oxo.apps.earth.background;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.EarthSyncImpl;
import ooo.oxo.apps.earth.R;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;
import static ooo.oxo.apps.earth.EarthSyncImpl.RESULT_ERROR;
import static ooo.oxo.apps.earth.EarthSyncImpl.RESULT_INSERTS;

public class EarthBackgroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL = "background";

    public static void start(Context context) {
        final Intent intent = new Intent(context, EarthBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private EarthSyncImpl syncer;

    private NotificationManagerCompat nm;
    private PowerManager.WakeLock wakeLock;

    private HandlerThread handlerThread;
    private Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressLint("WakelockTimeout")
    public void onCreate() {
        super.onCreate();

        syncer = new EarthSyncImpl(this);

        nm = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    getString(R.string.background_channel), NotificationManager.IMPORTANCE_MIN);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            nm.createNotificationChannel(channel);
        }

        startForeground(NOTIFICATION_ID, createNotification().build());

        final PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PARTIAL_WAKE_LOCK, "earth:background");
            wakeLock.acquire();
        }

        handlerThread = new HandlerThread("sync");
        handlerThread.start();

        handler = new Handler(handlerThread.getLooper());

        handler.post(this::run);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (wakeLock != null) {
            wakeLock.release();
        }

        handler.removeCallbacksAndMessages(null);
        handlerThread.quit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @SuppressWarnings("ConstantConditions")
    private void run() {
        final Map<String, Long> synced = syncer.sync(false);

        if (synced.containsKey(RESULT_ERROR)) {
            schedule();
            return;
        }

        String lastUpdate = null;

        if (synced.containsKey(RESULT_INSERTS)) {
            final long insets = synced.get(RESULT_INSERTS);
            if (insets > 0) {
                lastUpdate = getString(R.string.background_summary,
                        DateFormat.getInstance().format(new Date()));
            }
        }

        nm.notify(NOTIFICATION_ID, createNotification()
                .setContentText(lastUpdate)
                .build());
    }

    private void schedule() {
        handler.postDelayed(this::run, TimeUnit.MINUTES.toMillis(10));
    }

    private NotificationCompat.Builder createNotification() {
        return new NotificationCompat.Builder(
                this, NOTIFICATION_CHANNEL)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_earth_24dp)
                .setColor(getResources().getColor(R.color.primary))
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle(getString(R.string.background_title));
    }

}
