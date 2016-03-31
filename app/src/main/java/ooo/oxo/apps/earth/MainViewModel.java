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

import android.os.Bundle;
import android.widget.CompoundButton;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.adapters.SeekBarBindingAdapter;
import androidx.databinding.library.baseAdapters.BR;

import java.util.concurrent.TimeUnit;

import ooo.oxo.apps.earth.dao.Settings;
import ooo.oxo.apps.earth.widget.ScalingLayout;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MainViewModel extends BaseObservable {

    private static final int ASSUME_SIZE_KB = 35;

    private long interval;

    private int resolution;

    private boolean wifiOnly;

    private float offsetLong;

    private float offsetShort;

    private float scale;

    private boolean debug;

    public MainViewModel() {
    }

    public void loadFrom(Settings settings) {
        setInterval(settings.interval);
        setResolution(settings.resolution);
        setWifiOnly(settings.wifiOnly);
        setScaling(settings.offsetLong,
                settings.offsetShort,
                settings.scale);
        setDebug(settings.debug);
    }

    public void saveTo(Settings settings) {
        settings.interval = getInterval();
        settings.resolution = getResolution();
        settings.wifiOnly = isWifiOnly();
        settings.offsetLong = getOffsetLong();
        settings.offsetShort = getOffsetShort();
        settings.scale = getScale();
        settings.debug = isDebug();
    }

    public void saveState(Bundle state) {
        state.putLong("vm_interval", getInterval());
        state.putInt("vm_resolution", getResolution());
        state.putBoolean("vm_wifi_only", isWifiOnly());
        state.putFloat("vm_offset_l", getOffsetLong());
        state.putFloat("vm_offset_s", getOffsetShort());
        state.putFloat("vm_scale", getScale());
    }

    public void restoreState(Bundle state) {
        setInterval(state.getLong("vm_interval", getInterval()));
        setResolution(state.getInt("vm_resolution", getResolution()));
        setWifiOnly(state.getBoolean("vm_wifi_only", isWifiOnly()));
        setScaling(state.getFloat("vm_offset_l", getOffsetLong()),
                state.getFloat("vm_offset_s", getOffsetShort()),
                state.getFloat("vm_scale", getScale()));
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
        notifyPropertyChanged(BR.intervalMinutes);
        notifyPropertyChanged(BR.intervalProgressValue);
        notifyPropertyChanged(BR.traffic);
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
    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
        notifyPropertyChanged(BR.resolution);
        notifyPropertyChanged(BR.resolutionProgressValue);
        notifyPropertyChanged(BR.traffic);
    }

    @Bindable
    public int getResolutionProgressValue() {
        return Resolutions.findBestResolutionIndex(resolution);
    }

    public void setResolutionProgressValue(int progress) {
        setResolution(Resolutions.RESOLUTIONS[progress]);
    }

    @Bindable
    public int getResolutionProgressMax() {
        return Resolutions.RESOLUTIONS.length - 1;
    }

    @Bindable
    public SeekBarBindingAdapter.OnProgressChanged getResolutionProgressWatcher() {
        return (v, progress, fromUser) -> setResolutionProgressValue(progress);
    }

    @Bindable
    public int getTraffic() {
        return (int) (Resolutions.RESOLUTION_DAILY_TRAFFICS_KB
                [Resolutions.findBestResolutionIndex(resolution)]
                * ((float) TimeUnit.HOURS.toMillis(1) / (float) interval)
                * 30
                / 1024);
    }

    @Bindable
    public boolean isWifiOnly() {
        return wifiOnly;
    }

    public void setWifiOnly(boolean wifiOnly) {
        this.wifiOnly = wifiOnly;
        notifyPropertyChanged(BR.wifiOnly);
    }

    @Bindable
    public CompoundButton.OnCheckedChangeListener getWifiOnlyWatcher() {
        return (v, checked) -> setWifiOnly(checked);
    }

    @Bindable
    public float getOffsetLong() {
        return offsetLong;
    }

    @Bindable
    public float getOffsetShort() {
        return offsetShort;
    }

    @Bindable
    public float getScale() {
        return scale;
    }

    public void setScaling(float offsetLong, float offsetShort, float scale) {
        this.offsetLong = offsetLong;
        this.offsetShort = offsetShort;
        this.scale = scale;
        notifyPropertyChanged(BR.offsetLong);
        notifyPropertyChanged(BR.offsetShort);
        notifyPropertyChanged(BR.scale);
    }

    @Bindable
    public ScalingLayout.OnScalingChangeListener getScalingWatcher() {
        return (v, offsetLong, offsetShort, scale) -> {
            this.offsetLong = offsetLong;
            this.offsetShort = offsetShort;
            this.scale = scale;
        };
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean toggleDebug() {
        this.debug = !this.debug;
        return this.debug;
    }

}
