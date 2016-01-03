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

package ooo.oxo.apps.earth.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ImmersiveUtil {

    private static final int FLAG_KEEP_SCREEN_ON = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

    public static void enter(Activity activity) {
        activity.getWindow().addFlags(FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= 19) {
            ImmersiveUtil19.enter(activity.getWindow().getDecorView());
        } else {
            ImmersiveUtilBase.enter(activity.getWindow());
        }
    }

    public static void exit(Activity activity) {
        activity.getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= 19) {
            ImmersiveUtil19.exit(activity.getWindow().getDecorView());
        } else {
            ImmersiveUtilBase.exit(activity.getWindow());
        }
    }

    public static boolean isEntered(Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            return ImmersiveUtil19.isEntered(activity.getWindow().getDecorView());
        } else {
            return ImmersiveUtilBase.isEntered(activity.getWindow());
        }
    }

    private static class ImmersiveUtilBase {

        private static final int FLAG_FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        public static void enter(Window window) {
            window.addFlags(FLAG_FULLSCREEN);
        }

        public static void exit(Window window) {
            window.clearFlags(FLAG_FULLSCREEN);
        }

        public static boolean isEntered(Window window) {
            return (window.getAttributes().flags & FLAG_FULLSCREEN) == FLAG_FULLSCREEN;
        }

    }

    @TargetApi(19)
    private static class ImmersiveUtil19 {

        private static final int FLAG_IMMERSIVE = View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        public static void enter(View decor) {
            SystemUiVisibilityUtil.addFlags(decor, FLAG_IMMERSIVE);
        }

        public static void exit(View decor) {
            SystemUiVisibilityUtil.clearFlags(decor, FLAG_IMMERSIVE);
        }

        public static boolean isEntered(View decor) {
            return SystemUiVisibilityUtil.hasFlags(decor, FLAG_IMMERSIVE);
        }

    }

}
