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

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemAsset;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.dao.Earth;
import ooo.oxo.apps.earth.provider.EarthsContract;

public class WatchSyncService extends WearableListenerService {

    private static final String TAG = "WatchSyncService";

    @Override
    public void onPeerConnected(Node peer) {
        Log.v(TAG, "phone connected");
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.v(TAG, "phone disconnected");
    }

    @Override
    public void onDataChanged(DataEventBuffer events) {
        Log.v(TAG, "data incoming from phone");

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/earth")) {
                handleAsset(event.getDataItem().getAssets().get("earth"));
            }
        }
    }

    private void handleAsset(DataItemAsset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("asset must not be null");
        }

        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connection = client.blockingConnect(10, TimeUnit.SECONDS);

        if (!connection.isSuccess()) {
            Log.e(TAG, "failed to connect to GoogleApiClient: " + connection.getErrorCode());
            return;
        }

        InputStream input = Wearable.DataApi.getFdForAsset(client, asset)
                .await()
                .getInputStream();

        client.disconnect();

        if (input == null) {
            Log.e(TAG, "failed to get asset");
            return;
        }

        File file = new File(getCacheDir(), String.valueOf("earth_" + System.currentTimeMillis()));

        try {
            FileUtils.copyInputStreamToFile(input, file);
        } catch (IOException e) {
            Log.e(TAG, "failed to save asset", e);
            return;
        }

        Earth earth = new Earth(file.getAbsolutePath());
        getContentResolver().insert(EarthsContract.CONTENT_URI, earth.toContentValues());

        Log.d(TAG, "done syncing earth");
    }

}
