package com.zelenite.bluetemp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zelenite.bluetemp.livedata.DeviceViewModel;
import com.zelenite.bluetemp.livedata.TemperatureViewModel;
import com.zelenite.bluetemp.livedata.TitleViewModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ProgressBar spinner;
    private TemperatureViewModel temperatureText;
    private TitleViewModel title;


    public MainActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_main, container, false);
        final TextView temperature = (TextView) view.findViewById(R.id.output);
        spinner = (ProgressBar) view.findViewById(R.id.progressBar_cyclic);

        title = new ViewModelProvider(this.requireActivity()).get(TitleViewModel.class);

        temperatureText = new ViewModelProvider(this.requireActivity()).get(TemperatureViewModel.class);

        final FloatingActionButton startButton = (FloatingActionButton) view.findViewById(R.id.fab);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(MainActivityFragment.this.getActivity(), temperatureText.getTemperature().getValue(),
               //         Toast.LENGTH_SHORT).show();
                //BluetoothService bluetoothService= ((BlueTempApplication)MainActivityFragment.this.getActivity().getApplication()).getBluetoothService();
                //if(!bluetoothService.isBusy()) {
                //bluetoothService.read();
                DeviceViewModel deviceViewModel=new ViewModelProvider(MainActivityFragment.this.requireActivity()).get(DeviceViewModel.class);
                if(deviceViewModel.getDevice().getValue()==null){
                     Toast.makeText(MainActivityFragment.this.getActivity(), " No BTLE device selected",
                             Toast.LENGTH_SHORT).show();
                    return;
                }
                temperatureText.read(deviceViewModel.getDevice().getValue());
            }
        });

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        temperatureText.getTemperature().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String value) {
                temperature.setText(value);
            }
        });
        temperatureText.getError().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String value) {
                Toast.makeText(MainActivityFragment.this.getActivity(), value,
                        Toast.LENGTH_SHORT).show();
            }
        });

        temperatureText.getProgress().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable final Boolean value) {
                if(!value){
                    spinner.setVisibility(View.GONE);
                    startButton.setEnabled(true);
                    temperature.setVisibility(View.VISIBLE);
                }else{
                    spinner.setVisibility(View.VISIBLE);
                    temperature.setVisibility(View.GONE);
                    startButton.setEnabled(false);
                }

            }
        });
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }


}
