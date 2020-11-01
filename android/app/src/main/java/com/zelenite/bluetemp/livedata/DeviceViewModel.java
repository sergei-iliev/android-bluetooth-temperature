package com.zelenite.bluetemp.livedata;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class DeviceViewModel extends AndroidViewModel {

    private MutableLiveData<BluetoothDevice> device=new MutableLiveData<>();

    public DeviceViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<BluetoothDevice> getDevice() {
        return device;
    }
}
