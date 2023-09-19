package com.example.datingapp.Original;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;

import com.example.datingapp.Original.SSGRTCBluetoothManager.State;
import com.example.datingapp.R;

import org.webrtc.ThreadUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SSGRTCAudioManager {
    private static final String SPEAKERPHONE_AUTO = "auto";
    private static final String SPEAKERPHONE_FALSE = "false";
    private static final String SPEAKERPHONE_TRUE = "true";
    private static final String TAG = "RIVCAppRTCAudioManager";
    public static AudioManager audioManager;
    private final com.example.datingapp.Original.SSGRTCBluetoothManager bluetoothManager;
    private final Context np_apprtcContext;
    private final String np_useSpeakerphone;
    public boolean np_hasWiredHeadset = false;
    private AudioManagerEvents audioManagerEvents;
    private AudioManagerState np_amState;
    private Set<AudioDevice> np_audioDevices = new HashSet();
    private OnAudioFocusChangeListener np_audioFocusChangeListener;
    private AudioDevice np_defaultAudioDevice;
    private SSGRTCProximitySensor np_proximitySensor = null;
    private int np_savedAudioMode = -2;
    private boolean np_savedIsMicrophoneMute = false;
    private boolean np_savedIsSpeakerPhoneOn = false;
    private AudioDevice np_selectedAudioDevice;
    private BroadcastReceiver np_wiredHeadsetReceiver;
    private AudioDevice userSelectedAudioDevice;

    private SSGRTCAudioManager(Context context) {
        ThreadUtils.checkIsOnMainThread();
        this.np_apprtcContext = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.bluetoothManager = com.example.datingapp.Original.SSGRTCBluetoothManager.create(context, this);
        this.np_wiredHeadsetReceiver = new np_wiredHeadsetReceiver();
        this.np_amState = AudioManagerState.UNINITIALIZED;
        String string = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_speakerphone_key), context.getString(R.string.pref_speakerphone_default));
        this.np_useSpeakerphone = string;
        if (string.equals(SPEAKERPHONE_FALSE)) {
            this.np_defaultAudioDevice = AudioDevice.EARPIECE;
        } else {
            this.np_defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
        }
        this.np_proximitySensor = SSGRTCProximitySensor.create(context, new Runnable() {
            public void run() {
                SSGRTCAudioManager.this.onProximitySensorChangedState();
            }
        });
    }

    static SSGRTCAudioManager create(Context context) {
        return new SSGRTCAudioManager(context);
    }

    public static void setSpeakerphoneOn(boolean z) {
        if (audioManager.isSpeakerphoneOn() != z) {
            audioManager.setSpeakerphoneOn(z);
        }
    }

    public void onProximitySensorChangedState() {
        if (this.np_useSpeakerphone.equals(SPEAKERPHONE_AUTO) && this.np_audioDevices.size() == 2 && this.np_audioDevices.contains(AudioDevice.EARPIECE) && this.np_audioDevices.contains(AudioDevice.SPEAKER_PHONE)) {
            if (this.np_proximitySensor.sensorReportsNearState()) {
                setAudioDeviceInternal(AudioDevice.EARPIECE);
            } else {
                setAudioDeviceInternal(AudioDevice.SPEAKER_PHONE);
            }
        }
    }

    @SuppressLint("WrongConstant")
    public void start(AudioManagerEvents audioManagerEvents2) {
        ThreadUtils.checkIsOnMainThread();
        if (this.np_amState != AudioManagerState.RUNNING) {
            this.audioManagerEvents = audioManagerEvents2;
            this.np_amState = AudioManagerState.RUNNING;
            this.np_savedAudioMode = audioManager.getMode();
            this.np_savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();
            this.np_savedIsMicrophoneMute = audioManager.isMicrophoneMute();
            this.np_hasWiredHeadset = np_hasWiredHeadset();
            OnAudioFocusChangeListener r0 = new OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int i) {
                }
            };
            this.np_audioFocusChangeListener = r0;
            audioManager.requestAudioFocus(r0, 0, 2);
            audioManager.setMode(3);
            setMicrophoneMute(false);
            this.userSelectedAudioDevice = AudioDevice.NONE;
            this.np_selectedAudioDevice = AudioDevice.NONE;
            this.np_audioDevices.clear();
            this.bluetoothManager.start();
            updateAudioDeviceState();
            registerReceiver(this.np_wiredHeadsetReceiver, new IntentFilter("android.intent.action.HEADSET_PLUG"));
        }
    }

    @SuppressLint("WrongConstant")
    public void stop() {
        ThreadUtils.checkIsOnMainThread();
        if (this.np_amState == AudioManagerState.RUNNING) {
            this.np_amState = AudioManagerState.UNINITIALIZED;
            unregisterReceiver(this.np_wiredHeadsetReceiver);
            this.bluetoothManager.stop();
            setSpeakerphoneOn(this.np_savedIsSpeakerPhoneOn);
            setMicrophoneMute(this.np_savedIsMicrophoneMute);
            audioManager.setMode(this.np_savedAudioMode);
            audioManager.abandonAudioFocus(this.np_audioFocusChangeListener);
            this.np_audioFocusChangeListener = null;
            SSGRTCProximitySensor sSGRTCProximitySensor = this.np_proximitySensor;
            if (sSGRTCProximitySensor != null) {
                sSGRTCProximitySensor.stop();
                this.np_proximitySensor = null;
            }
            this.audioManagerEvents = null;
        }
    }

    private void setAudioDeviceInternal(AudioDevice audioDevice) {
        com.example.datingapp.Original.SSGRTCUtils.assertIsTrue(this.np_audioDevices.contains(audioDevice));
        switch (AnonymousClass3.$SwitchMap$com$videocall$live$chat$online$NewCode$SSGRTCAudioManager$AudioDevice[audioDevice.ordinal()]) {
            case 1:
                setSpeakerphoneOn(true);
                break;
            case 2:
                setSpeakerphoneOn(false);
                break;
            case 3:
                setSpeakerphoneOn(false);
                break;
            case 4:
                setSpeakerphoneOn(false);
                break;
        }
        this.np_selectedAudioDevice = audioDevice;
    }

    public void setDefaultAudioDevice(AudioDevice audioDevice) {
        ThreadUtils.checkIsOnMainThread();
        switch (AnonymousClass3.$SwitchMap$com$videocall$live$chat$online$NewCode$SSGRTCAudioManager$AudioDevice[audioDevice.ordinal()]) {
            case 1:
                this.np_defaultAudioDevice = audioDevice;
                break;
            case 2:
                if (hasEarpiece()) {
                    this.np_defaultAudioDevice = audioDevice;
                    break;
                } else {
                    this.np_defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
                    break;
                }
        }
        updateAudioDeviceState();
    }

    public void selectAudioDevice(AudioDevice audioDevice) {
        ThreadUtils.checkIsOnMainThread();
        this.np_audioDevices.contains(audioDevice);
        this.userSelectedAudioDevice = audioDevice;
        updateAudioDeviceState();
    }

    public Set<AudioDevice> getAudioDevices() {
        ThreadUtils.checkIsOnMainThread();
        return Collections.unmodifiableSet(new HashSet(this.np_audioDevices));
    }

    public AudioDevice getSelectedAudioDevice() {
        ThreadUtils.checkIsOnMainThread();
        return this.np_selectedAudioDevice;
    }

    private void registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        this.np_apprtcContext.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
        this.np_apprtcContext.unregisterReceiver(broadcastReceiver);
    }

    private void setMicrophoneMute(boolean z) {
        if (audioManager.isMicrophoneMute() != z) {
            audioManager.setMicrophoneMute(z);
        }
    }

    private boolean hasEarpiece() {
        return this.np_apprtcContext.getPackageManager().hasSystemFeature("android.hardware.telephony");
    }

    @SuppressLint("WrongConstant")
    @Deprecated
    private boolean np_hasWiredHeadset() {
        if (VERSION.SDK_INT < 23) {
            return audioManager.isWiredHeadsetOn();
        }
        for (AudioDeviceInfo type : audioManager.getDevices(3)) {
            int type2 = type.getType();
            if (type2 == 3 || type2 == 11) {
                return true;
            }
        }
        return false;
    }

    public void updateAudioDeviceState() {
        AudioDevice audioDevice;
        ThreadUtils.checkIsOnMainThread();
        if (this.bluetoothManager.getState() == State.HEADSET_AVAILABLE || this.bluetoothManager.getState() == State.HEADSET_UNAVAILABLE || this.bluetoothManager.getState() == State.SCO_DISCONNECTING) {
            this.bluetoothManager.updateDevice();
        }
        HashSet hashSet = new HashSet();
        if (this.bluetoothManager.getState() == State.SCO_CONNECTED || this.bluetoothManager.getState() == State.SCO_CONNECTING || this.bluetoothManager.getState() == State.HEADSET_AVAILABLE) {
            hashSet.add(AudioDevice.BLUETOOTH);
        }
        if (this.np_hasWiredHeadset) {
            hashSet.add(AudioDevice.WIRED_HEADSET);
        } else {
            hashSet.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                hashSet.add(AudioDevice.EARPIECE);
            }
        }
        boolean z3 = true;
        boolean z = !this.np_audioDevices.equals(hashSet);
        this.np_audioDevices = hashSet;
        if (this.bluetoothManager.getState() == State.HEADSET_UNAVAILABLE && this.userSelectedAudioDevice == AudioDevice.BLUETOOTH) {
            this.userSelectedAudioDevice = AudioDevice.NONE;
        }
        if (this.np_hasWiredHeadset && this.userSelectedAudioDevice == AudioDevice.SPEAKER_PHONE) {
            this.userSelectedAudioDevice = AudioDevice.WIRED_HEADSET;
        }
        if (!this.np_hasWiredHeadset && this.userSelectedAudioDevice == AudioDevice.WIRED_HEADSET) {
            this.userSelectedAudioDevice = AudioDevice.SPEAKER_PHONE;
        }
        boolean z2 = false;
        if (!(this.bluetoothManager.getState() == State.HEADSET_AVAILABLE && (this.userSelectedAudioDevice == AudioDevice.NONE || this.userSelectedAudioDevice == AudioDevice.BLUETOOTH))) {
            z3 = false;
        }
        if (!((this.bluetoothManager.getState() != State.SCO_CONNECTED && this.bluetoothManager.getState() != State.SCO_CONNECTING) || this.userSelectedAudioDevice == AudioDevice.NONE || this.userSelectedAudioDevice == AudioDevice.BLUETOOTH)) {
            z2 = true;
        }
        if (!(this.bluetoothManager.getState() == State.HEADSET_AVAILABLE || this.bluetoothManager.getState() == State.SCO_CONNECTING)) {
            this.bluetoothManager.getState();
            State state = State.SCO_CONNECTED;
        }
        if (z2) {
            this.bluetoothManager.stopScoAudio();
            this.bluetoothManager.updateDevice();
        }
        if (z3 && !z2 && !this.bluetoothManager.startScoAudio()) {
            this.np_audioDevices.remove(AudioDevice.BLUETOOTH);
            z = true;
        }
        AudioDevice audioDevice2 = this.np_selectedAudioDevice;
        if (this.bluetoothManager.getState() == State.SCO_CONNECTED) {
            audioDevice = AudioDevice.BLUETOOTH;
        } else if (this.np_hasWiredHeadset) {
            audioDevice = AudioDevice.WIRED_HEADSET;
        } else {
            audioDevice = this.np_defaultAudioDevice;
        }
        if (audioDevice != this.np_selectedAudioDevice || z) {
            setAudioDeviceInternal(audioDevice);
            AudioManagerEvents audioManagerEvents2 = this.audioManagerEvents;
            if (audioManagerEvents2 != null) {
                audioManagerEvents2.onAudioDeviceChanged(this.np_selectedAudioDevice, this.np_audioDevices);
            }
        }
    }

    public enum AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE,
        BLUETOOTH,
        NONE
    }

    public enum AudioManagerState {
        UNINITIALIZED,
        PREINITIALIZED,
        RUNNING
    }

    public interface AudioManagerEvents {
        void onAudioDeviceChanged(AudioDevice audioDevice, Set<AudioDevice> set);
    }

    /* renamed from: shree.vidtalk.randomchat.videocall.livevideocall.new_video_code.SSGRTCAudioManager$3 reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$videocall$live$chat$online$NewCode$SSGRTCAudioManager$AudioDevice;

        static {
            int[] iArr = new int[AudioDevice.values().length];
            $SwitchMap$com$videocall$live$chat$online$NewCode$SSGRTCAudioManager$AudioDevice = iArr;
            try {
                iArr[AudioDevice.SPEAKER_PHONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$videocall$live$chat$online$NewCode$SSGRTCAudioManager$AudioDevice[AudioDevice.EARPIECE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$videocall$live$chat$online$NewCode$SSGRTCAudioManager$AudioDevice[AudioDevice.WIRED_HEADSET.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$videocall$live$chat$online$NewCode$SSGRTCAudioManager$AudioDevice[AudioDevice.BLUETOOTH.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private class np_wiredHeadsetReceiver extends BroadcastReceiver {
        private static final int HAS_MIC = 1;
        private static final int HAS_NO_MIC = 0;
        private static final int STATE_PLUGGED = 1;
        private static final int STATE_UNPLUGGED = 0;

        private np_wiredHeadsetReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            boolean z = false;
            int intExtra = intent.getIntExtra("state", 0);
            intent.getIntExtra("microphone", 0);
            intent.getStringExtra("name");
            SSGRTCAudioManager appRTCAudioManager = SSGRTCAudioManager.this;
            if (intExtra == 1) {
                z = true;
            }
            appRTCAudioManager.np_hasWiredHeadset = z;
            SSGRTCAudioManager.this.updateAudioDeviceState();
        }
    }
}
