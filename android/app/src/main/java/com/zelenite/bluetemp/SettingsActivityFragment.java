package com.zelenite.bluetemp;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences , rootKey);
    }
}
