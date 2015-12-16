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
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.service.wallpaper.WallpaperService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceHolder;

public class EarthWallpaperService extends WallpaperService {

    private EarthSharedState sharedState;

    private LocalBroadcastManager bm;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedState = EarthSharedState.getInstance(this);
        bm = LocalBroadcastManager.getInstance(this);

        if (!sharedState.getWifiOnly() || NetworkStateUtil.isWifiConnected(this)) {
            EarthAlarmUtil.schedule(this);
        }
    }

    @Override
    public Engine onCreateEngine() {
        return new EarthWallpaperEngine();
    }

    private class EarthWallpaperEngine extends Engine {

        private static final String TAG = "EarthWallpaperEngine";

        private Rect region;

        private Paint paint;

        private int padding;

        private BroadcastReceiver receiver;

        public EarthWallpaperEngine() {
            region = new Rect();

            paint = new Paint();
            paint.setFilterBitmap(true);

            padding = getResources().getDimensionPixelOffset(R.dimen.default_padding);

            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (isVisible()) {
                        draw();
                    }
                }
            };
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            draw();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                draw();
                bm.registerReceiver(receiver, new IntentFilter(
                        EarthIntents.ACTION_NEW_EARTH_READY));
            } else {
                bm.unregisterReceiver(receiver);
            }
        }

        private void draw() {
            Bitmap earth = loadEarth();

            Canvas canvas = getSurfaceHolder().lockCanvas();

            region.set(0, 0, canvas.getWidth(), canvas.getHeight());

            if (region.width() > region.height()) {
                region.inset((region.width() - region.height()) / 2, 0);
            } else {
                region.inset(0, (region.height() - region.width()) / 2);
            }

            region.inset(padding, padding);

            canvas.drawBitmap(earth, null, region, paint);

            getSurfaceHolder().unlockCanvasAndPost(canvas);
        }

        private Bitmap loadEarth() {
            String lastEarth = sharedState.getLastEarth();

            if (lastEarth == null) {
                Log.d(TAG, "earth not ready, fallback to preview");
                return loadPreview();
            }

            Bitmap earth = BitmapFactory.decodeFile(lastEarth);

            if (earth == null) {
                Log.e(TAG, "failed to decode fetched earth, fallback to preview");
                return loadPreview();
            }

            return earth;
        }

        private Bitmap loadPreview() {
            return BitmapFactory.decodeResource(getResources(), R.drawable.preview);
        }

    }

}
