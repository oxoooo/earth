package ooo.oxo.apps.earth;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.provider.EarthsContract;

public class SyncScheduleUtil {

    public static void ensure() {
        final Account account = EarthSharedState.getInstance().getAccount();

        ContentResolver.setIsSyncable(account, EarthsContract.AUTHORITY, 1);

        ContentResolver.addPeriodicSync(account, EarthsContract.AUTHORITY,
                Bundle.EMPTY, TimeUnit.MINUTES.toSeconds(10));

        ContentResolver.setSyncAutomatically(account, EarthsContract.AUTHORITY, true);
    }

}
