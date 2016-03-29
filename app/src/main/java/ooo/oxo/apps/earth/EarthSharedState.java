package ooo.oxo.apps.earth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import ooo.oxo.apps.earth.account.StubAuthenticator;

public class EarthSharedState {

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
        final Account[] accounts = am.getAccountsByType(StubAuthenticator.ACCOUNT_TYPE);

        if (accounts.length > 0) {
            return accounts[0];
        }

        final Account account = new Account(context.getString(R.string.free_account), StubAuthenticator.ACCOUNT_TYPE);
        am.addAccountExplicitly(account, null, Bundle.EMPTY);

        return account;
    }

}
