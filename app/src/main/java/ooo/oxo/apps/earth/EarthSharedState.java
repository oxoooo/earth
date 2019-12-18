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
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import ooo.oxo.apps.earth.account.StubAuthenticator;

@SuppressWarnings("WeakerAccess")
public class EarthSharedState {

    @SuppressLint("StaticFieldLeak")
    private static EarthSharedState instance;

    private Context context;

    private AccountManager am;

    private EarthSharedState(Context context) {
        this.context = context;
        this.am = AccountManager.get(context);
    }

    static void init(Context context) {
        instance = new EarthSharedState(context);
    }

    public static EarthSharedState getInstance() {
        return instance;
    }

    public Account getAccount() {
        final Account account = new Account(context.getString(R.string.free_account), StubAuthenticator.ACCOUNT_TYPE);
        am.addAccountExplicitly(account, null, Bundle.EMPTY);

        return account;
    }

}
