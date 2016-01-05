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

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;
import com.umeng.analytics.MobclickAgent;

/**
 * 用于当 Android Wear 设备连接上时启动 {@link WatchSyncService}，后者在没有设备连接时会自动退出以节省电量
 */
public class WatchStateService extends WearableListenerService {

    private static final String TAG = "WatchStateService";

    @Override
    public void onPeerConnected(Node peer) {
        Log.v(TAG, "watch connected");

        startService(new Intent(this, WatchSyncService.class));
        startService(new Intent(this, WatchTransferService.class));

        MobclickAgent.onEvent(this, "watch_launched");
    }

}
