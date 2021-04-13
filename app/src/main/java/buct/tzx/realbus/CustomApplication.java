package buct.tzx.realbus;

import android.app.Application;

import buct.tzx.buctbus.BuctBus;

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new BuctBus.builder()
                .setDebugMode(true)
                .setEnablePostSticky(true)
                .setEnableThread(true)
                .build();
    }
}
