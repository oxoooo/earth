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
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.account.StubAuthenticator;
import ooo.oxo.apps.earth.background.EarthBackgroundInitializer;
import ooo.oxo.apps.earth.background.EarthBackgroundService;
import ooo.oxo.apps.earth.provider.EarthsContract;
import ooo.oxo.apps.earth.sync.EarthsSyncService;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

class SyncUtil {

    static void enableSync(Context context) {
        setComponentEnabled(context, EarthsSyncService.class, true);

        final AccountManager am = AccountManager.get(context);

        final Account account = makeAccount(context);
        am.addAccountExplicitly(account, null, Bundle.EMPTY);

        ContentResolver.setIsSyncable(account, EarthsContract.AUTHORITY, 1);

        ContentResolver.addPeriodicSync(account, EarthsContract.AUTHORITY,
                Bundle.EMPTY, TimeUnit.MINUTES.toSeconds(10));

        ContentResolver.setSyncAutomatically(account, EarthsContract.AUTHORITY, true);
    }

    static void disableSync(Context context) {
        setComponentEnabled(context, EarthsSyncService.class, false);
    }

    static void enableBackground(Context context) {
        setComponentEnabled(context, EarthBackgroundService.class, true);
        setComponentEnabled(context, EarthBackgroundInitializer.class, true);
        EarthBackgroundService.start(context);
    }

    static void disableBackground(Context context) {
        setComponentEnabled(context, EarthBackgroundService.class, false);
        setComponentEnabled(context, EarthBackgroundInitializer.class, false);
    }

    private static Account makeAccount(Context context) {
        return new Account(context.getString(R.string.free_account), StubAuthenticator.ACCOUNT_TYPE);
    }

    private static void setComponentEnabled(Context context, Class<?> cls, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, cls),
                enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP);
    }

}
