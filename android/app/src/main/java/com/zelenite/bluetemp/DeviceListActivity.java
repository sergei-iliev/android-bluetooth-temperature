package com.zelenite.bluetemp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class DeviceListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.device_list, new DeviceListActivityFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

//        progress=(ProgressBar)findViewById(R.id.scan_bar);
//
//        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
//        if(bluetoothAdapter==null){
//            Toast.makeText(this, "Unable to detect BluetoothService", Toast.LENGTH_SHORT).show();
//            this.finish();
//        }
//
//        scanButton=(Button)findViewById(R.id.button_scan);
//        scanButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                doDiscovery();
//            }
//        });
//
//        initPairedDeviceList();
//        initNewDeviceList();
//
//        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        this.registerReceiver(reciever,filter);
//
//        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        this.registerReceiver(reciever,filter);
    }
}
