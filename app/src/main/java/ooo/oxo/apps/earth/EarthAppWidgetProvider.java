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

package ooo.oxo.apps.earth;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.AppWidgetTarget;

import jp.wasabeef.glide.transformations.MaskTransformation;
import ooo.oxo.apps.earth.provider.EarthsContract;

public class EarthAppWidgetProvider extends AppWidgetProvider {

    private static int[] getAppWidgetIds(Context context) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        final ComponentName provider = new ComponentName(context, EarthAppWidgetProvider.class);
        return widgetManager.getAppWidgetIds(provider);
    }

    public static void triggerUpdate(Context context) {
        final Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, getAppWidgetIds(context));
        context.sendBroadcast(intent);
    }

    private static Uri getLatestEarth() {
        return EarthsContract.LATEST_CONTENT_URI
                .buildUpon()
                .appendQueryParameter("t", String.valueOf(System.currentTimeMillis()))
                .build();
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        onUpdate(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        onUpdate(context, appWidgetManager, new int[]{appWidgetId});
    }

    private void onUpdate(Context context) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        final int[] widgetIds = getAppWidgetIds(context);
        onUpdate(context, widgetManager, widgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final Intent intent = new Intent(context, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setOnClickPendingIntent(R.id.earth, pendingIntent);

        final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int size = Math.min(metrics.widthPixels, metrics.heightPixels);

        Glide.with(context)
                .load(getLatestEarth())
                .asBitmap()
                .error(R.drawable.preview)
                .transform(new MaskTransformation(context, R.drawable.mask))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new AppWidgetTarget(context, views, R.id.earth, size, size, appWidgetIds));
    }

}
