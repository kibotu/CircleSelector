package net.kibotu.circleselector.app;

import android.app.Application;

import com.common.android.utils.ContextHelper;

import net.kibotu.android.deviceinfo.library.Device;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ContextHelper.with(this);
        Device.with(this);
    }
}