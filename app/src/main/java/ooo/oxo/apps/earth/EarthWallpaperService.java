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

import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.FileNotFoundException;

import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.provider.EarthsContract;
import ooo.oxo.apps.earth.provider.SettingsContract;

public class EarthWallpaperService extends WallpaperService {

    @Override
    public void onCreate() {
        super.onCreate();
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

        private final ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, "new earth ready, drawing...");
                draw();
            }
        };

        public EarthWallpaperEngine() {
            region = new Rect();

            paint = new Paint();
            paint.setFilterBitmap(true);

            padding = getResources().getDimensionPixelOffset(R.dimen.default_padding);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            scheduleIfNeeded();
        }

        private void scheduleIfNeeded() {
            Cursor cursor = getContentResolver().query(SettingsContract.CONTENT_URI, null, null, null, null);

            if (cursor == null) {
                return;
            }

            Settings settings = Settings.fromCursor(cursor);

            if (settings == null) {
                return;
            }

            cursor.close();

            if (!settings.wifiOnly || NetworkStateUtil.isWifiConnected(EarthWallpaperService.this)) {
                EarthAlarmUtil.schedule(EarthWallpaperService.this, settings.interval);
            }
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
                getContentResolver().registerContentObserver(EarthsContract.LATEST_CONTENT_URI, false, observer);
            } else {
                getContentResolver().unregisterContentObserver(observer);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (isVisible()) {
                draw();
            }
        }

        private void draw() {
            Bitmap earth = loadLatestEarth();

            if (earth == null) {
                Log.d(TAG, "earth not ready, fallback to preview");
                earth = loadPreview();
            }

            if (earth == null) {
                Log.e(TAG, "earth preview not ready, impossible!");
                return;
            }

            Canvas canvas = getSurfaceHolder().lockCanvas();

            if (canvas == null) {
                Log.e(TAG, "canvas not ready, why?");
                return;
            }

            region.set(0, 0, canvas.getWidth(), canvas.getHeight());

            if (region.width() > region.height()) {
                region.inset((region.width() - region.height()) / 2, 0);
            } else {
                region.inset(0, (region.height() - region.width()) / 2);
            }

            region.inset(padding, padding);

            canvas.drawBitmap(earth, null, region, paint);

            getSurfaceHolder().unlockCanvasAndPost(canvas);

            earth.recycle();
        }

        @Nullable
        private Bitmap loadLatestEarth() {
            try {
                return BitmapFactory.decodeStream(getContentResolver().openInputStream(EarthsContract.LATEST_CONTENT_URI));
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        @Nullable
        private Bitmap loadPreview() {
            return BitmapFactory.decodeResource(getResources(), R.drawable.preview);
        }

    }

}
