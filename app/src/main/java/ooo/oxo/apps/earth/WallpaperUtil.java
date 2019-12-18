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

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

class WallpaperUtil {

    static void changeLiveWallPaper(Context context) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);

        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, EarthWallpaperService.class));

        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    static boolean isCurrent(Context context) {
        WallpaperManager wm = WallpaperManager.getInstance(context);
        WallpaperInfo wi = wm.getWallpaperInfo();
        return wi != null && new ComponentName(context, EarthWallpaperService.class)
                .equals(wi.getComponent());
    }

    static boolean isSupported(Context context) {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

}
