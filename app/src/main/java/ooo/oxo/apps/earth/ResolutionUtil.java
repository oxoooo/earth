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

import android.util.DisplayMetrics;

public class ResolutionUtil {

    public static final int[] RESOLUTIONS = new int[]{
            550, 720, 1080, 1440, 2200
    };

    public static final int[] RESOLUTION_DAILY_TRAFFICS_KB = new int[]{
            532, 857, 1727, 2823, 5818
    };

    public static int findBestResolutionIndex(int size) {
        int closest = 0;

        for (int i = 0; i < ResolutionUtil.RESOLUTIONS.length; i++) {
            if (Math.abs(ResolutionUtil.RESOLUTIONS[i] - size) <
                    Math.abs(ResolutionUtil.RESOLUTIONS[closest] - size)) {
                closest = i;
            }
        }

        return closest;
    }

    public static int findBestResolution(int size) {
        return RESOLUTIONS[findBestResolutionIndex(size)];
    }

    public static int findBestResolution(DisplayMetrics metrics) {
        return findBestResolution(Math.min(metrics.widthPixels, metrics.heightPixels));
    }

}
