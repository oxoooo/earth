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

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.adapters.SeekBarBindingAdapter;
import android.widget.CompoundButton;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class MainViewModel extends BaseObservable {

    private static final int ASSUME_SIZE_KB = 35;

    private long interval;

    private boolean wifiOnly;

    public MainViewModel(EarthSharedState sharedState) {
        setInterval(sharedState.getInterval());
        setWifiOnly(sharedState.getWifiOnly());
    }

    public void saveTo(EarthSharedState sharedState) {
        sharedState.setInterval(getInterval());
        sharedState.setWifiOnly(isWifiOnly());
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
        notifyPropertyChanged(ooo.oxo.apps.earth.BR.intervalMinutes);
        notifyPropertyChanged(ooo.oxo.apps.earth.BR.intervalProgressValue);
        notifyPropertyChanged(ooo.oxo.apps.earth.BR.traffic);
    }

    @Bindable
    public int getIntervalMinutes() {
        return (int) TimeUnit.MILLISECONDS.toMinutes(interval);
    }

    public void setIntervalMinutes(int minutes) {
        setInterval(TimeUnit.MINUTES.toMillis(minutes));
    }

    @Bindable
    public int getIntervalProgressValue() {
        return getIntervalMinutes() / 10 - 1;
    }

    public void setIntervalProgressValue(int progress) {
        setIntervalMinutes((progress + 1) * 10);
    }

    @Bindable
    public int getIntervalProgressMax() {
        return 11;
    }

    @Bindable
    public SeekBarBindingAdapter.OnProgressChanged getIntervalProgressWatcher() {
        return (v, progress, fromUser) -> setIntervalProgressValue(progress);
    }

    @Bindable
    public int getTraffic() {
        return (int) (TimeUnit.DAYS.toMillis(1) * 30 / interval * ASSUME_SIZE_KB / 1024);
    }

    @Bindable
    public boolean isWifiOnly() {
        return wifiOnly;
    }

    public void setWifiOnly(boolean wifiOnly) {
        this.wifiOnly = wifiOnly;
        notifyPropertyChanged(ooo.oxo.apps.earth.BR.wifiOnly);
    }

    @Bindable
    public CompoundButton.OnCheckedChangeListener getWifiOnlyWatcher() {
        return (v, checked) -> setWifiOnly(checked);
    }

}
