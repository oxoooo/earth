package ooo.oxo.apps.earth.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class StubAuthenticatorService extends Service {

    private StubAuthenticator authenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        authenticator = new StubAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }

}
