package com.zelenite.bluetemp;

import android.app.Application;

import com.zelenite.bluetemp.livedata.DeviceViewModel;
import com.zelenite.bluetemp.livedata.TemperatureViewModel;
import com.zelenite.bluetemp.livedata.TitleViewModel;

public class BlueTempApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
