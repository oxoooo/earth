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

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.provider.EarthsContract;

/**
 * 将最新的图片同步到手表上
 */
public class WatchTransferService extends IntentService {

    private static final String TAG = "WatchTransferService";

    public WatchTransferService() {
        super("WatchTransfer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "transferring earth from phone to watch...");

        GoogleApiClient client = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        if (!client.blockingConnect(10, TimeUnit.SECONDS).isSuccess()) {
            Log.e(TAG, "failed to connect to GoogleApiClient");
            return;
        }

        Bitmap image;

        try {
            image = Glide.with(this).load(EarthsContract.LATEST_CONTENT_URI
                    .buildUpon()
                    .appendQueryParameter("t", String.valueOf(System.currentTimeMillis()))
                    .build())
                    .asBitmap()
                    .into(360, 360)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "failed to prepare earth image");
            return;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.WEBP, 75, output);

        Asset asset = Asset.createFromBytes(output.toByteArray());

        PutDataRequest request = PutDataRequest.create("/earth");
        request.putAsset("earth", asset);

        Wearable.DataApi.putDataItem(client, request);

        client.disconnect();

        Log.d(TAG, "done syncing");

        MobclickAgent.onEvent(this, "watch_synced");
    }

}
