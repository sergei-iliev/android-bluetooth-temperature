package com.zelenite.bluetemp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DeviceListActivityFragment extends Fragment {



    private static final int REQUEST_ENABLE_BT=1;

    public static final String DEVICE_ADDRESS="device.address";
    public static final String DEVICE_NAME="device.name";

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_device_list, container, false);
        getActivity().setResult(Activity.RESULT_CANCELED);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if(bluetoothAdapter==null){
            Toast.makeText(this.getContext(), "Unable to detect BluetoothService", Toast.LENGTH_SHORT).show();
            this.getActivity().finish();
        }

        pairedDevicesArrayAdapter=new ArrayAdapter<>(this.getContext(),R.layout.device_name);
        ListView pairedListView=(ListView)view.findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(itemClickListener);

        this.resetPairedDevices();
        return view;
    }
    private void resetPairedDevices(){
        if(!enableBluetooth()){
            return;
        }
        Set<BluetoothDevice> pairedDevices=bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size()>0) {
            for (BluetoothDevice device:pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName()+"\n"+device.getAddress());
            }
            pairedDevicesArrayAdapter.notifyDataSetChanged();
        }
    }
    private boolean enableBluetooth(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode== Activity.RESULT_OK){
                resetPairedDevices();
            }
        }
    }

    private AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bluetoothAdapter.cancelDiscovery();
            String item=((TextView)view).getText().toString();
            String lines[] = item.split("\\r?\\n");

            Intent intent=new Intent();
            intent.putExtra(DEVICE_NAME,lines[0]);
            intent.putExtra(DEVICE_ADDRESS,lines[1]);

            getActivity().setResult(Activity.RESULT_OK,intent);
            getActivity().finish();
        }
    };
}
