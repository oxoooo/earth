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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.io.FileNotFoundException;

import ooo.oxo.apps.earth.provider.EarthsContract;

public class EarthWatchFaceService extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private static final String TAG = "EarthWatchFaceEngine";

        private final ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, "new earth ready, drawing...");
                invalidate();
            }
        };

        private Rect region;

        private Paint paint;

        private int inset;

        private boolean ambient;

        private ColorMatrixColorFilter ambientFilter;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            region = new Rect();

            paint = new Paint();
            paint.setFilterBitmap(true);

            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);

            ambientFilter = new ColorMatrixColorFilter(cm);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            if (insets.isRound()) {
                inset = -2;
                setWatchFaceStyle(new WatchFaceStyle.Builder(EarthWatchFaceService.this)
                        .setHotwordIndicatorGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)
                        .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                        .setShowSystemUiTime(true)
                        .setShowUnreadCountIndicator(true)
                        .setStatusBarGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP)
                        .setViewProtectionMode(WatchFaceStyle.PROTECT_HOTWORD_INDICATOR
                                | WatchFaceStyle.PROTECT_STATUS_BAR)
                        .build());
            } else {
                inset = getResources().getDimensionPixelOffset(R.dimen.padding_square);
                setWatchFaceStyle(new WatchFaceStyle.Builder(EarthWatchFaceService.this)
                        .setHotwordIndicatorGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)
                        .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                        .setShowSystemUiTime(true)
                        .setShowUnreadCountIndicator(true)
                        .setStatusBarGravity(Gravity.END | Gravity.TOP)
                        .setViewProtectionMode(WatchFaceStyle.PROTECT_HOTWORD_INDICATOR
                                | WatchFaceStyle.PROTECT_STATUS_BAR)
                        .build());
            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            if (ambient != inAmbientMode) {
                ambient = inAmbientMode;
                paint.setColorFilter(ambient ? ambientFilter : null);
                invalidate();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                invalidate();
                getContentResolver().registerContentObserver(
                        EarthsContract.LATEST_CONTENT_URI, false, observer);
            } else {
                getContentResolver().unregisterContentObserver(observer);
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Bitmap earth = loadLatestEarth();

            if (earth == null) {
                Log.d(TAG, "earth not ready, fallback to preview");
                earth = loadPreview();
            }

            if (earth == null) {
                Log.e(TAG, "earth preview not ready, impossible!");
                return;
            }

            region.set(bounds);
            region.bottom = region.width();
            region.inset(inset, inset);

            canvas.drawBitmap(earth, null, region, paint);

            earth.recycle();
        }

        @Nullable
        private Bitmap loadLatestEarth() {
            try {
                return BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(EarthsContract.LATEST_CONTENT_URI));
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
