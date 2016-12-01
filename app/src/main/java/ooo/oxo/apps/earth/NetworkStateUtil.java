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

import android.net.ConnectivityManager;
import android.os.Build;

public class NetworkStateUtil {

    public static boolean shouldConsiderSavingData(ConnectivityManager cm) {
        // Do not trust ConnectivityManagerCompat, since it will always return ENABLED in API < N
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                cm.getRestrictBackgroundStatus() == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED;
    }

}
