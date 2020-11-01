package com.zelenite.bluetemp.livedata;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TitleViewModel extends AndroidViewModel {

    private MutableLiveData<String> title;

    public TitleViewModel(@NonNull Application application) {
        super(application);
        title = new MutableLiveData<>();
    }


    public MutableLiveData<String> getTitle() {
        return title;
    }
}
