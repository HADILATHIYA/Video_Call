package com.example.datingapp.Original;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.webrtc.ThreadUtils.ThreadChecker;

public class SSGRTCProximitySensor implements SensorEventListener {
    private static final String TAG = "RIVCAppRTCProximitySensor";
    private boolean np_lastStateReportIsNear;
    private final Runnable np_onSensorStateListener;
    private Sensor np_proximitySensor;
    private final SensorManager sensorManager;
    private final ThreadChecker threadChecker = new ThreadChecker();

    static SSGRTCProximitySensor create(Context context, Runnable runnable) {
        return new SSGRTCProximitySensor(context, runnable);
    }

    private SSGRTCProximitySensor(Context context, Runnable runnable) {
        this.np_onSensorStateListener = runnable;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public boolean start() {
        this.threadChecker.checkIsOnValidThread();
        if (!initDefaultSensor()) {
            return false;
        }
        this.sensorManager.registerListener(this, this.np_proximitySensor, 3);
        return true;
    }

    public void stop() {
        this.threadChecker.checkIsOnValidThread();
        Sensor sensor = this.np_proximitySensor;
        if (sensor != null) {
            this.sensorManager.unregisterListener(this, sensor);
        }
    }

    public boolean sensorReportsNearState() {
        this.threadChecker.checkIsOnValidThread();
        return this.np_lastStateReportIsNear;
    }

    @SuppressLint("LongLogTag")
    public final void onAccuracyChanged(Sensor sensor, int i) {
        this.threadChecker.checkIsOnValidThread();
        com.example.datingapp.Original.SSGRTCUtils.assertIsTrue(sensor.getType() == 8);
        if (i == 0) {
            Log.e(TAG, "The values returned by this sensor cannot be trusted");
        }
    }

    public final void onSensorChanged(SensorEvent sensorEvent) {
        this.threadChecker.checkIsOnValidThread();
        com.example.datingapp.Original.SSGRTCUtils.assertIsTrue(sensorEvent.sensor.getType() == 8);
        if (sensorEvent.values[0] < this.np_proximitySensor.getMaximumRange()) {
            this.np_lastStateReportIsNear = true;
        } else {
            this.np_lastStateReportIsNear = false;
        }
        Runnable runnable = this.np_onSensorStateListener;
        if (runnable != null) {
            runnable.run();
        }
    }

    private boolean initDefaultSensor() {
        if (this.np_proximitySensor != null) {
            return true;
        }
        Sensor defaultSensor = this.sensorManager.getDefaultSensor(8);
        this.np_proximitySensor = defaultSensor;
        if (defaultSensor == null) {
            return false;
        }
        return true;
    }
}
