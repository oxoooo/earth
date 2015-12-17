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

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class EarthFetcher {

    private static final String TAG = "EarthFetcher";

    private final RequestManager rm;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HHmmss", Locale.US);

    private final GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    private FutureTarget<File> request;

    public EarthFetcher(Context context) {
        this.rm = Glide.with(context);
    }

    public File fetch(int resolution) throws ExecutionException, InterruptedException {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 40);
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) / 10 * 10);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        dateFormat.setCalendar(calendar);

        String path = dateFormat.format(new Date(calendar.getTimeInMillis()));

        //noinspection ConstantConditions
        Log.d(TAG, "fetching " + path + (BuildConfig.USE_OXO_SERVER ? " accelerated" : ""));

        request = rm.load(getUrl(path, resolution))
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

        return request.get();
    }

    private String getUrl(String path, int resolution) {
        if (BuildConfig.USE_OXO_SERVER) {
            return String.format(BuildConfig.SERVER_OXO, path, resolution);
        } else {
            return String.format(BuildConfig.SERVER_NICT, path);
        }
    }

    public void clean() {
        if (request != null) {
            request.cancel(true);
        }
    }

}
