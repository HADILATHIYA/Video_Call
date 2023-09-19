package com.example.datingapp.Original;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import org.webrtc.ThreadUtils;

import java.util.List;
import java.util.Set;

public class SSGRTCBluetoothManager {
    private static final int BLUETOOTH_SCO_TIMEOUT_MS = 4000;
    private static final int MAX_SCO_CONNECTION_ATTEMPTS = 2;
    private static final String TAG = "RIVCAppRTCBluetoothManager";
    private final SSGRTCAudioManager apprtcAudioManager;
    private final AudioManager audioManager;
    private BluetoothAdapter bluetoothAdapter;
    public BluetoothDevice bluetoothDevice;
    public BluetoothHeadset bluetoothHeadset;
    private final Runnable bluetoothTimeoutRunnable = new Runnable() {
        public void run() {
            SSGRTCBluetoothManager.this.bluetoothTimeout();
        }
    };
    private final Handler handler;
    private final Context np_apprtcContext;
    private final BroadcastReceiver np_bluetoothHeadsetReceiver;
    private final ServiceListener np_bluetoothServiceListener;
    public State np_bluetoothState;
    int scoConnectionAttempts;

    private class BluetoothHeadsetBroadcastReceiver extends BroadcastReceiver {
        private BluetoothHeadsetBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (SSGRTCBluetoothManager.this.np_bluetoothState != State.UNINITIALIZED) {
                String action = intent.getAction();
                String str = "android.bluetooth.profile.extra.STATE";
                if (action.equals("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")) {
                    int intExtra = intent.getIntExtra(str, 0);
                    if (intExtra == 2) {
                        SSGRTCBluetoothManager.this.scoConnectionAttempts = 0;
                        SSGRTCBluetoothManager.this.updateAudioDeviceState();
                    } else if (!(intExtra == 1 || intExtra == 3 || intExtra != 0)) {
                        SSGRTCBluetoothManager.this.stopScoAudio();
                        SSGRTCBluetoothManager.this.updateAudioDeviceState();
                    }
                } else if (action.equals("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")) {
                    int intExtra2 = intent.getIntExtra(str, 10);
                    String str2 = SSGRTCBluetoothManager.TAG;
                    if (intExtra2 == 12) {
                        SSGRTCBluetoothManager.this.cancelTimer();
                        if (SSGRTCBluetoothManager.this.np_bluetoothState == State.SCO_CONNECTING) {
                            SSGRTCBluetoothManager.this.np_bluetoothState = State.SCO_CONNECTED;
                            SSGRTCBluetoothManager.this.scoConnectionAttempts = 0;
                            SSGRTCBluetoothManager.this.updateAudioDeviceState();
                        } else {
                            Log.w(str2, "Unexpected state BluetoothHeadset.STATE_AUDIO_CONNECTED");
                        }
                    } else if (intExtra2 == 11) {
                        Log.d(str2, "+++ Bluetooth audio SCO is now connecting...");
                    } else if (intExtra2 == 10 && !isInitialStickyBroadcast()) {
                        SSGRTCBluetoothManager.this.updateAudioDeviceState();
                    }
                }
            }
        }
    }

    public enum State {
        UNINITIALIZED,
        ERROR,
        HEADSET_UNAVAILABLE,
        HEADSET_AVAILABLE,
        SCO_DISCONNECTING,
        SCO_CONNECTING,
        SCO_CONNECTED
    }

    private class np_bluetoothServiceListener implements ServiceListener {
        private np_bluetoothServiceListener() {
        }

        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            if (i == 1 && SSGRTCBluetoothManager.this.np_bluetoothState != State.UNINITIALIZED) {
                SSGRTCBluetoothManager.this.bluetoothHeadset = (BluetoothHeadset) bluetoothProfile;
                SSGRTCBluetoothManager.this.updateAudioDeviceState();
            }
        }

        public void onServiceDisconnected(int i) {
            if (i == 1 && SSGRTCBluetoothManager.this.np_bluetoothState != State.UNINITIALIZED) {
                SSGRTCBluetoothManager.this.stopScoAudio();
                SSGRTCBluetoothManager.this.bluetoothHeadset = null;
                SSGRTCBluetoothManager.this.bluetoothDevice = null;
                SSGRTCBluetoothManager.this.np_bluetoothState = State.HEADSET_UNAVAILABLE;
                SSGRTCBluetoothManager.this.updateAudioDeviceState();
            }
        }
    }

    public String stateToString(int i) {
        switch (i) {
            case 0:
                return "DISCONNECTED";
            case 1:
                return "CONNECTING";
            case 2:
                return "CONNECTED";
            case 3:
                return "DISCONNECTING";
            default:
                switch (i) {
                    case 10:
                        return "OFF";
                    case 11:
                        return "TURNING_ON";
                    case 12:
                        return "ON";
                    case 13:
                        return "TURNING_OFF";
                    default:
                        return "INVALID";
                }
        }
    }

    static SSGRTCBluetoothManager create(Context context, SSGRTCAudioManager appRTCAudioManager) {
        return new SSGRTCBluetoothManager(context, appRTCAudioManager);
    }

    protected SSGRTCBluetoothManager(Context context, SSGRTCAudioManager appRTCAudioManager) {
        ThreadUtils.checkIsOnMainThread();
        this.np_apprtcContext = context;
        this.apprtcAudioManager = appRTCAudioManager;
        this.audioManager = getAudioManager(context);
        this.np_bluetoothState = State.UNINITIALIZED;
        this.np_bluetoothServiceListener = new np_bluetoothServiceListener();
        this.np_bluetoothHeadsetReceiver = new BluetoothHeadsetBroadcastReceiver();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public State getState() {
        ThreadUtils.checkIsOnMainThread();
        return this.np_bluetoothState;
    }

    public void start() {
        ThreadUtils.checkIsOnMainThread();
        if (!hasPermission(this.np_apprtcContext, "android.permission.BLUETOOTH")) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Process (pid=");
            sb.append(Process.myPid());
            sb.append(") lacks BLUETOOTH permission");
            Log.w(str, sb.toString());
        } else if (this.np_bluetoothState == State.UNINITIALIZED) {
            this.bluetoothHeadset = null;
            this.bluetoothDevice = null;
            this.scoConnectionAttempts = 0;
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            this.bluetoothAdapter = defaultAdapter;
            if (defaultAdapter != null && this.audioManager.isBluetoothScoAvailableOffCall()) {
                logBluetoothAdapterInfo(this.bluetoothAdapter);
                if (getBluetoothProfileProxy(this.np_apprtcContext, this.np_bluetoothServiceListener, 1)) {
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
                    intentFilter.addAction("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
                    registerReceiver(this.np_bluetoothHeadsetReceiver, intentFilter);
                    this.np_bluetoothState = State.HEADSET_UNAVAILABLE;
                }
            }
        }
    }

    public void stop() {
        ThreadUtils.checkIsOnMainThread();
        if (this.bluetoothAdapter != null) {
            stopScoAudio();
            if (this.np_bluetoothState != State.UNINITIALIZED) {
                unregisterReceiver(this.np_bluetoothHeadsetReceiver);
                cancelTimer();
                BluetoothHeadset bluetoothHeadset2 = this.bluetoothHeadset;
                if (bluetoothHeadset2 != null) {
                    this.bluetoothAdapter.closeProfileProxy(1, bluetoothHeadset2);
                    this.bluetoothHeadset = null;
                }
                this.bluetoothAdapter = null;
                this.bluetoothDevice = null;
                this.np_bluetoothState = State.UNINITIALIZED;
            }
        }
    }

    public boolean startScoAudio() {
        ThreadUtils.checkIsOnMainThread();
        if (this.scoConnectionAttempts >= 2 || this.np_bluetoothState != State.HEADSET_AVAILABLE) {
            return false;
        }
        this.np_bluetoothState = State.SCO_CONNECTING;
        this.audioManager.startBluetoothSco();
        this.audioManager.setBluetoothScoOn(true);
        this.scoConnectionAttempts++;
        startTimer();
        return true;
    }

    public void stopScoAudio() {
        ThreadUtils.checkIsOnMainThread();
        if (this.np_bluetoothState == State.SCO_CONNECTING || this.np_bluetoothState == State.SCO_CONNECTED) {
            cancelTimer();
            this.audioManager.stopBluetoothSco();
            this.audioManager.setBluetoothScoOn(false);
            this.np_bluetoothState = State.SCO_DISCONNECTING;
        }
    }

    public void updateDevice() {
        if (this.np_bluetoothState != State.UNINITIALIZED) {
            BluetoothHeadset bluetoothHeadset2 = this.bluetoothHeadset;
            if (bluetoothHeadset2 != null) {
                List connectedDevices = bluetoothHeadset2.getConnectedDevices();
                if (connectedDevices.isEmpty()) {
                    this.bluetoothDevice = null;
                    this.np_bluetoothState = State.HEADSET_UNAVAILABLE;
                    return;
                }
                this.bluetoothDevice = (BluetoothDevice) connectedDevices.get(0);
                this.np_bluetoothState = State.HEADSET_AVAILABLE;
            }
        }
    }

    public AudioManager getAudioManager(Context context) {
        return (AudioManager) context.getSystemService("audio");
    }

    public void registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        this.np_apprtcContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        this.np_apprtcContext.unregisterReceiver(broadcastReceiver);
    }

    public boolean getBluetoothProfileProxy(Context context, ServiceListener serviceListener, int i) {
        return this.bluetoothAdapter.getProfileProxy(context, serviceListener, i);
    }

    public boolean hasPermission(Context context, String str) {
        return this.np_apprtcContext.checkPermission(str, Process.myPid(), Process.myUid()) == 0;
    }

    public void logBluetoothAdapterInfo(BluetoothAdapter bluetoothAdapter2) {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter2.getBondedDevices();
        if (!bondedDevices.isEmpty()) {
            for (BluetoothDevice bluetoothDevice2 : bondedDevices) {
                String str2 = TAG;
                StringBuilder sb2 = new StringBuilder();
                sb2.append(" name=");
                sb2.append(bluetoothDevice2.getName());
                sb2.append(", address=");
                sb2.append(bluetoothDevice2.getAddress());
                Log.d(str2, sb2.toString());
            }
        }
    }

    public void updateAudioDeviceState() {
        ThreadUtils.checkIsOnMainThread();
        this.apprtcAudioManager.updateAudioDeviceState();
    }

    private void startTimer() {
        ThreadUtils.checkIsOnMainThread();
        this.handler.postDelayed(this.bluetoothTimeoutRunnable, 4000);
    }

    public void cancelTimer() {
        ThreadUtils.checkIsOnMainThread();
        this.handler.removeCallbacks(this.bluetoothTimeoutRunnable);
    }

    public void bluetoothTimeout() {
        ThreadUtils.checkIsOnMainThread();
        if (this.np_bluetoothState != State.UNINITIALIZED && this.bluetoothHeadset != null && this.np_bluetoothState == State.SCO_CONNECTING) {
            List connectedDevices = this.bluetoothHeadset.getConnectedDevices();
            if (connectedDevices.size() > 0) {
                BluetoothDevice bluetoothDevice2 = (BluetoothDevice) connectedDevices.get(0);
                this.bluetoothDevice = bluetoothDevice2;
                if (this.bluetoothHeadset.isAudioConnected(bluetoothDevice2)) {
                    if (1 == 0) {
                        this.np_bluetoothState = State.SCO_CONNECTED;
                        this.scoConnectionAttempts = 0;
                    } else {
                        stopScoAudio();
                    }
                    updateAudioDeviceState();
                }
            }
            updateAudioDeviceState();
        }
    }

    private boolean isScoOn() {
        return this.audioManager.isBluetoothScoOn();
    }
}
