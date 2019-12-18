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

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.provider.EarthsContract;

class SyncUtil {

    static void ensure() {
        final Account account = EarthSharedState.getInstance().getAccount();

        ContentResolver.setIsSyncable(account, EarthsContract.AUTHORITY, 1);

        ContentResolver.addPeriodicSync(account, EarthsContract.AUTHORITY,
                Bundle.EMPTY, TimeUnit.MINUTES.toSeconds(10));

        ContentResolver.setSyncAutomatically(account, EarthsContract.AUTHORITY, true);
    }

}
