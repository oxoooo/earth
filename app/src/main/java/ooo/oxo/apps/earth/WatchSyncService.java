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

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.atomic.AtomicInteger;

import ooo.oxo.apps.earth.provider.EarthsContract;

/**
 * 用于观察地球变化并触发 {@link WatchTransferService} 同步到手表
 */
@SuppressWarnings("deprecation")
public class WatchSyncService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NodeApi.NodeListener {

    private static final String TAG = "WatchSyncService";

    private final AtomicInteger connected = new AtomicInteger();

    private final ContentObserver observer = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "new earth ready, triggering watch sync...");
            sync();
        }
    };

    private GoogleApiClient client;

    @Override
    public void onCreate() {
        super.onCreate();

        client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        getContentResolver().registerContentObserver(EarthsContract.LATEST_CONTENT_URI, false, observer);

        Log.d(TAG, "watch syncing service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        client.connect();
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "connected to Google API Client");

        Wearable.NodeApi.addListener(client, this);

        Wearable.NodeApi.getConnectedNodes(client).setResultCallback(result -> {
            int count = connected.addAndGet(result.getNodes().size());
            if (count == 0) {
                Log.d(TAG, "no watch connected, stopping...");
                stopSelf();
            } else {
                Log.d(TAG, "connected watch: " + count);
                sendOnWatchConnected();
            }
        });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "connection suspended: " + cause);
        stopSelf();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(TAG, "connection failed: " + result);
        stopSelf();
    }

    @Override
    public void onPeerConnected(Node node) {
        int count = connected.incrementAndGet();
        Log.d(TAG, "new watch connected, total: " + count);
        sendOnWatchConnected();
    }

    @Override
    public void onPeerDisconnected(Node node) {
        if (connected.decrementAndGet() == 0) {
            Log.d(TAG, "all watches disconnected, stop piping service");
            stopSelf();
        }
    }

    private void sync() {
        startService(new Intent(this, WatchTransferService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (client != null && client.isConnected()) {
            Wearable.NodeApi.removeListener(client, this);
            client.disconnect();
        }

        getContentResolver().unregisterContentObserver(observer);

        Log.d(TAG, "watch syncing service stopped");
    }

    private void sendOnWatchConnected() {
        MobclickAgent.onEvent(this, "watch_connected");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
