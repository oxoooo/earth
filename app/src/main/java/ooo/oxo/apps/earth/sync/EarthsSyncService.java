package ooo.oxo.apps.earth.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class EarthsSyncService extends Service {

    private static final Object lock = new Object();

    private static EarthsSyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (lock) {
            if (syncAdapter == null) {
                syncAdapter = new EarthsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }

}
