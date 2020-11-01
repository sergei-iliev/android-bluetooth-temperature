package com.zelenite.bluetemp.livedata;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TemperatureViewModel extends AndroidViewModel{
    private MutableLiveData<String> temperature;
    private MutableLiveData<Boolean> progress;
    private MutableLiveData<String> error;

    private TemperatureReadTask temperatureReadTask;

    public TemperatureViewModel(@NonNull Application application) {
        super(application);
        temperature = new MutableLiveData<>();
        error = new MutableLiveData<>();
        progress = new MutableLiveData<>();
        progress.setValue(false);
    }


    public MutableLiveData<String> getTemperature() {
        return temperature;
    }

    public void read(BluetoothDevice bluetoothDevice){
       if(temperatureReadTask!=null &&temperatureReadTask.getStatus()!=AsyncTask.Status.FINISHED){
           return;  //still running
       }
       temperatureReadTask=new TemperatureReadTask(this,bluetoothDevice);
       temperatureReadTask.execute();

    }
    public MutableLiveData<String> getError() {
        return error;
    }

    public MutableLiveData<Boolean> getProgress() {
        return progress;
    }

    private static class TemperatureReadTask extends AsyncTask<Void,Void, CommandResult<String>> {

        private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        private final TemperatureViewModel temperatureViewModel;
        private final BluetoothDevice bluetoothDevice;
        private BluetoothSocket socket;

        public TemperatureReadTask(TemperatureViewModel temperatureViewModel, BluetoothDevice bluetoothDevice){
            this.temperatureViewModel=temperatureViewModel;
            this.bluetoothDevice=bluetoothDevice;
        }
        @Override
        protected CommandResult<String> doInBackground(Void... voids) {
            try{
                socket = bluetoothDevice.createRfcommSocketToServiceRecord(SerialPortServiceClass_UUID);
                socket.connect();
            } catch (Exception e) {
                temperatureViewModel.getError().postValue(e.getMessage());
                return new CommandResult<>(e);
            }


            InputStream in = null;
            OutputStream out = null;

            // Get the BluetoothSocket input and output streams
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                this.close();
                return new CommandResult<>(e);
            }

            try {
                //send command
                byte[] command = new byte[1];
                command[0]=(byte)44;
                out.write(command,0,1);
                out.flush();





                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line="";
                int timeout=0;
                while (true) {
                    if(reader.ready()) {
                        line = reader.readLine();
                    }else{
                        try {
                            Thread.currentThread().sleep(200);
                        }catch(InterruptedException ie){}
                        timeout++;
                    }

                    if(timeout==10){
                        break;
                    }
                }

                this.close();
                return new CommandResult<>(line+"\u00B0");
            }catch(IOException e){
                this.close();
                return new CommandResult<>(e);
            }

        }

        private void close(){
            if(socket!=null){
                try {
                    socket.close();
                }catch (Exception e){
                }
            }
        }
        @Override
        protected void onPreExecute() {
            temperatureViewModel.getProgress().setValue(Boolean.TRUE);
        }

        @Override
        protected void onPostExecute(CommandResult<String> value) {
            temperatureViewModel.getProgress().setValue(Boolean.FALSE);
            if(value.getError()!=null){
                temperatureViewModel.getError().setValue(value.getError().getMessage());
            }
            if(value.getResult()!=null){
                temperatureViewModel.getTemperature().setValue(value.getResult());
            }
        }
    }

}
