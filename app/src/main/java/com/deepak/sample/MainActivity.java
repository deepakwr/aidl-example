package com.deepak.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.deepak.sensorsdk.IOrientationCallback;
import com.deepak.sensorsdk.IOrientationInterface;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity  {

    public static final String TAG = "MainActivity";

    IOrientationInterface service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initService();
    }

    private void initService()
    {
        Intent i = new Intent();
        i.setClassName("com.deepak.sample", "com.deepak.sensorsdk.OrientationService");
        boolean ret = getApplicationContext().bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.d(TAG,"Service return : " + ret);
    }

    private ServiceConnection connection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = IOrientationInterface.Stub.asInterface(boundService);
            Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_LONG).show();

            ((ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1)).scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        service.getOrientationValues(callback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 8, 8, TimeUnit.SECONDS);
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Toast.makeText(MainActivity.this, "disconnected", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unbindService(connection);
        connection = null;
    }

    IOrientationCallback callback = new IOrientationCallback.Stub() {
        @Override
        public void orientationCallback(float[] rotationValues) throws RemoteException {

            Log.d(TAG,"This is via callback " + Arrays.toString(rotationValues));

            final float[] values = Arrays.copyOf(rotationValues,5);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.xAxis)).setText(String.valueOf(values[0]));
                    ((TextView) findViewById(R.id.yAxis)).setText(String.valueOf(values[1]));
                    ((TextView) findViewById(R.id.zAxis)).setText(String.valueOf(values[2]));
                    ((TextView) findViewById(R.id.scalarComponent)).setText(String.valueOf(values[3]));
                    ((TextView) findViewById(R.id.directionAccuracy)).setText(String.valueOf(values[4]));
                }
            });


        }
    };

}
