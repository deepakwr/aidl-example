package com.deepak.sensorsdk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.Nullable;

public class OrientationService extends Service implements SensorEventListener {

    private static final String TAG = "OrientationService";

    public static final int SENSOR_TIME_INTERVAL = 8000;

    public long eventTime;

    private SensorManager sensorManager;

    private Sensor rotationSensor;

    private int lastAccuracy;

    float[] rotationValues = new float[5];

    private ArrayList<IOrientationCallback> mRemoteCallbacks = new ArrayList<>();

    private ServiceHandler handler = null;

    private static final int DEVICE_ROTATION_INFO = 53;

    HandlerThread handlerThread = new HandlerThread("AidlServiceThread");

    IBinder orientationInterface = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager= (SensorManager) getBaseContext().getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        handlerThread.start();
        handler = new ServiceHandler(handlerThread.getLooper());


        orientationInterface = new IOrientationInterface.Stub() {

            @Override
            public float[] getOrientationData() throws RemoteException {
                return rotationValues;
            }

            @Override
            public void getOrientationValues(IOrientationCallback callback) throws RemoteException {
                sendMsgToHandler(callback,DEVICE_ROTATION_INFO);
            }
        };

        return orientationInterface;
    }

    void sendMsgToHandler(IOrientationCallback callback, int flag) {

        mRemoteCallbacks.add(callback);

        Message message = handler.obtainMessage();
        message.arg1 = mRemoteCallbacks.size() - 1;

        message.what = flag;
        handler.sendMessage(message);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(Math.abs(eventTime-System.currentTimeMillis())>SENSOR_TIME_INTERVAL)
            eventTime = System.currentTimeMillis();
        else
            return;

        if (lastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == rotationSensor) {
            rotationValues = Arrays.copyOf(event.values,5);
            Log.d("OrientationService", "update rotationValues now : " + Arrays.toString(rotationValues));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (lastAccuracy != accuracy) {
            lastAccuracy = accuracy;
        }
    }


    class ServiceHandler extends Handler {
        int callbackIndex = 0;

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            callbackIndex = msg.arg1;

            switch (msg.what) {

                case DEVICE_ROTATION_INFO:

                    try {
                        mRemoteCallbacks.get(callbackIndex).orientationCallback(rotationValues);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
