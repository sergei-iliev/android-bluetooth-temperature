package com.zelenite.bluetemp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.zelenite.bluetemp.livedata.DeviceViewModel;
import com.zelenite.bluetemp.livedata.TemperatureViewModel;
import com.zelenite.bluetemp.livedata.TitleViewModel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.preference.PreferenceManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int DEVICE_LIST_ACTIVITY=1;


    private Toolbar toolbar;
    private TitleViewModel titleViewModel;
    //private BluetoothService bluetoothService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        titleViewModel = new ViewModelProvider(this).get(TitleViewModel.class);

        TemperatureViewModel temperatureText = new ViewModelProvider(this).get(TemperatureViewModel.class);

        DeviceViewModel deviceViewModel=new ViewModelProvider(this).get(DeviceViewModel.class);
        //register model only once
        //((BlueTempApplication)this.getApplication()).createBluetoothService(temperatureText,title,deviceViewModel);

        //bluetoothService=((BlueTempApplication)this.getApplication()).getBluetoothService();

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        titleViewModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                toolbar.setSubtitle(newName);
            }
        });
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(this, "BluetoothService is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        //set initial state
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if(menu==null){
            return super.onMenuOpened(featureId,menu);
        }

        //if (bluetoothService.isBusy()){
//            MenuItem item = menu.findItem(R.id.action_disconnect);
//            item.setEnabled(true);
//            item = menu.findItem(R.id.action_scan);
//            item.setEnabled(false);
//            item=menu.findItem(R.id.action_settings);
//            item.setEnabled(true);
//        }else{
            MenuItem item = menu.findItem(R.id.action_disconnect);
            item.setEnabled(false);
            item = menu.findItem(R.id.action_scan);
            item.setEnabled(true);
            item=menu.findItem(R.id.action_settings);
            item.setEnabled(true);
//        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id==R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_scan) {
            Intent intent=new Intent(this,DeviceListActivity.class);
            startActivityForResult(intent,DEVICE_LIST_ACTIVITY);
        }
        if (id == R.id.action_disconnect) {
//          if(bluetoothService.isBusy()) {
//              bluetoothService.disconnect();
//          }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        TitleViewModel title = new ViewModelProvider(this).get(TitleViewModel.class);
        switch(requestCode){
            case DEVICE_LIST_ACTIVITY:
                //clean subtitle
                if(resultCode== Activity.RESULT_OK) {
                    title.getTitle().setValue("");
                    //bluetooth.disconnect();
                    BluetoothDevice device=BluetoothAdapter.getDefaultAdapter().getRemoteDevice(data.getStringExtra(DeviceListActivityFragment.DEVICE_ADDRESS));

                    DeviceViewModel deviceViewModel=new ViewModelProvider(this).get(DeviceViewModel.class);
                    deviceViewModel.getDevice().setValue(device);


                    title.getTitle().setValue(deviceViewModel.getDevice().getValue().getName()+"/"+deviceViewModel.getDevice().getValue().getAddress());
                    //bluetoothService.clear();
                    // ((MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment)).connect(device);
                }
                break;
        }

    }
}
