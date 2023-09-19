package com.example.datingapp.Original;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection.Callback;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.datingapp.Original.SSGCallFragment.OnCallEvents;
import com.example.datingapp.Original.SSGPeerConnectionClient.DataChannelParameters;
import com.example.datingapp.Original.SSGPeerConnectionClient.PeerConnectionEvents;
import com.example.datingapp.Original.SSGPeerConnectionClient.PeerConnectionParameters;
import com.example.datingapp.Original.SSGRTCAudioManager.AudioDevice;
import com.example.datingapp.Original.SSGRTCAudioManager.AudioManagerEvents;
import com.example.datingapp.Original.SSGRTCClient.RoomConnectionParameters;
import com.example.datingapp.Original.SSGRTCClient.SignalingEvents;
import com.example.datingapp.Original.SSGRTCClient.SignalingParameters;
import com.example.datingapp.R;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory.Options;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.VideoRenderer.I420Frame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SSGCallActivity extends Activity implements SignalingEvents, PeerConnectionEvents, OnCallEvents {
    public static final String EXTRA_AECDUMP_ENABLED = "org.appspot.apprtc.AECDUMP";
    public static final String EXTRA_AUDIOCODEC = "org.appspot.apprtc.AUDIOCODEC";
    public static final String EXTRA_AUDIO_BITRATE = "org.appspot.apprtc.AUDIO_BITRATE";
    public static final String EXTRA_CAMERA2 = "org.appspot.apprtc.CAMERA2";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "org.appspot.apprtc.CAPTURETOTEXTURE";
    public static final String EXTRA_CMDLINE = "org.appspot.apprtc.CMDLINE";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "org.appspot.apprtc.DATA_CHANNEL_ENABLED";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "org.appspot.apprtc.DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "org.appspot.apprtc.DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "org.appspot.apprtc.DISABLE_BUILT_IN_NS";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF = "org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "org.appspot.apprtc.DISPLAY_HUD";
    public static final String EXTRA_ENABLE_LEVEL_CONTROL = "org.appspot.apprtc.ENABLE_LEVEL_CONTROL";
    public static final String EXTRA_FLEXFEC_ENABLED = "org.appspot.apprtc.FLEXFEC";
    public static final String EXTRA_HWCODEC_ENABLED = "org.appspot.apprtc.HWCODEC";
    public static final String EXTRA_ID = "org.appspot.apprtc.ID";
    public static final String EXTRA_LOOPBACK = "org.appspot.apprtc.LOOPBACK";
    public static final String EXTRA_MAX_RETRANSMITS = "org.appspot.apprtc.MAX_RETRANSMITS";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "org.appspot.apprtc.MAX_RETRANSMITS_MS";
    public static final String EXTRA_NEGOTIATED = "org.appspot.apprtc.NEGOTIATED";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED = "org.appspot.apprtc.NOAUDIOPROCESSING";
    public static final String EXTRA_OPENSLES_ENABLED = "org.appspot.apprtc.OPENSLES";
    public static final String EXTRA_ORDERED = "org.appspot.apprtc.ORDERED";
    public static final String EXTRA_PROTOCOL = "org.appspot.apprtc.PROTOCOL";
    public static final String EXTRA_ROOMID = "org.appspot.apprtc.ROOMID";
    public static final String EXTRA_RUNTIME = "org.appspot.apprtc.RUNTIME";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT";
    public static final String EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH";
    public static final String EXTRA_SCREENCAPTURE = "org.appspot.apprtc.SCREENCAPTURE";
    public static final String EXTRA_TRACING = "org.appspot.apprtc.TRACING";
    public static final String EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS";
    public static final String EXTRA_VIDEOCODEC = "org.appspot.apprtc.VIDEOCODEC";
    public static final String EXTRA_VIDEO_BITRATE = "org.appspot.apprtc.VIDEO_BITRATE";
    public static final String EXTRA_VIDEO_CALL = "org.appspot.apprtc.VIDEO_CALL";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED = "org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_VIDEO_FPS = "org.appspot.apprtc.VIDEO_FPS";
    public static final String EXTRA_VIDEO_HEIGHT = "org.appspot.apprtc.VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_WIDTH = "org.appspot.apprtc.VIDEO_WIDTH";
    private static final String[] MANDATORY_PERMISSIONS = {Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.RECORD_AUDIO};
    private static final int STAT_CALLBACK_PERIOD = 1000;
    private static final String TAG = "CallRTCClient";
    private static int mediaProjectionPermissionResultCode;
    private static Intent mediaProjectionPermissionResultData;
    private boolean activityRunning;
    public com.example.datingapp.Original.SSGRTCClient appRtcClient;
    private com.example.datingapp.Original.SSGRTCAudioManager audioManager;
    private boolean callControlFragmentVisible = true;
    public com.example.datingapp.Original.SSGCallFragment callFragment;
    private long callStartedTimeMs;
    private boolean commandLineRun;
    private com.example.datingapp.Original.SSGCpuMonitor cpuMonitor;
    private SurfaceViewRenderer fullscreenRenderer;
    public SSGHudFragment hudFragment;
    public boolean iceConnected;
    public boolean isError;
    private boolean isSwappedFeeds;
    private final ProxyRenderer localProxyRenderer = new ProxyRenderer();
    private Toast logToast;
    private boolean micEnabled = true;
    public com.example.datingapp.Original.SSGPeerConnectionClient peerConnectionClient;
    public PeerConnectionParameters peerConnectionParameters;
    private SurfaceViewRenderer pipRenderer;
    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final List<Callbacks> remoteRenderers = new ArrayList();
    private RoomConnectionParameters roomConnectionParameters;
    private EglBase rootEglBase;
    private boolean screencaptureEnabled;
    private SignalingParameters signalingParameters;
    private VideoFileRenderer videoFileRenderer;

    private class ProxyRenderer implements Callbacks {
        private Callbacks target;

        private ProxyRenderer() {
        }

        public synchronized void renderFrame(I420Frame frame) {
            Callbacks callbacks = this.target;
            if (callbacks == null) {
                VideoRenderer.renderFrameDone(frame);
                return;
            }
            callbacks.renderFrame(frame);
        }

        public synchronized void setTarget(Callbacks target2) {
            this.target = target2;
        }
    }

    public void onIceDisconnected() {
    }

    public void onPeerConnectionClosed() {
    }

    public void onCreate(Bundle bundle) {
        String[] strArr;
        int i;
        int i2;
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().addFlags(2622592);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.abc_activity_call);


        this.iceConnected = false;
        DataChannelParameters dataChannelParameters = null;
        this.signalingParameters = null;
        com.example.datingapp.Original.SSGStaticVar.incall = 0;
        this.pipRenderer = findViewById(R.id.np_pip_video_view);
        this.fullscreenRenderer = findViewById(R.id.np_fullscreen_video_view);
        this.callFragment = new com.example.datingapp.Original.SSGCallFragment();
        this.hudFragment = new com.example.datingapp.Original.SSGHudFragment();
        this.pipRenderer.setOnClickListener(view -> {
        });
        this.fullscreenRenderer.setOnClickListener(view -> {
        });
        this.remoteRenderers.add(this.remoteProxyRenderer);
        Intent intent = getIntent();
        EglBase create = EglBase.create();
        this.rootEglBase = create;
        this.pipRenderer.init(create.getEglBaseContext(), null);
        this.pipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        String stringExtra = intent.getStringExtra("org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE");
        if (stringExtra != null) {
            try {
                VideoFileRenderer videoFileRenderer2 = new VideoFileRenderer(stringExtra, intent.getIntExtra("org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH", 0), intent.getIntExtra("org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT", 0), this.rootEglBase.getEglBaseContext());
                this.videoFileRenderer = videoFileRenderer2;
                this.remoteRenderers.add(videoFileRenderer2);
            } catch (IOException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to open video file for output: ");
                sb.append(stringExtra);
                throw new RuntimeException(sb.toString(), e);
            }
        }
        this.fullscreenRenderer.init(this.rootEglBase.getEglBaseContext(), null);
        this.fullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL);
        this.pipRenderer.setZOrderMediaOverlay(true);
        this.pipRenderer.setEnableHardwareScaler(true);
        this.fullscreenRenderer.setEnableHardwareScaler(false);
        setSwappedFeeds(false);
        for (String str : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Permission ");
                sb2.append(str);
                sb2.append(" is not granted");
                logAndToast(sb2.toString());
                setResult(0);
                finish();
                return;
            }
        }
        Uri data = intent.getData();
        if (data == null) {
            logAndToast(getString(R.string.missing_url));
            Log.e(TAG, "Didn't get any URL in intent!");
            setResult(0);
            finish();
            return;
        }
        String stringExtra2 = intent.getStringExtra("org.appspot.apprtc.ROOMID");
        if (stringExtra2 == null || stringExtra2.length() == 0) {
            logAndToast(getString(R.string.missing_url));
            setResult(0);
            finish();
            return;
        }
        boolean booleanExtra = intent.getBooleanExtra("org.appspot.apprtc.LOOPBACK", false);
        boolean booleanExtra2 = intent.getBooleanExtra("org.appspot.apprtc.TRACING", false);
        int intExtra = intent.getIntExtra("org.appspot.apprtc.VIDEO_WIDTH", 0);
        int intExtra2 = intent.getIntExtra("org.appspot.apprtc.VIDEO_HEIGHT", 0);
        boolean booleanExtra3 = intent.getBooleanExtra("org.appspot.apprtc.SCREENCAPTURE", false);
        this.screencaptureEnabled = booleanExtra3;
        if (booleanExtra3 && intExtra == 0 && intExtra2 == 0) {
            DisplayMetrics displayMetrics = getDisplayMetrics();
            i2 = displayMetrics.widthPixels;
            i = displayMetrics.heightPixels;
        } else {
            i2 = intExtra;
            i = intExtra2;
        }
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            DataChannelParameters dataChannelParameters2 = new DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true), intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1), intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL), intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
            dataChannelParameters = dataChannelParameters2;
        }
        PeerConnectionParameters peerConnectionParameters2 = new PeerConnectionParameters(intent.getBooleanExtra("org.appspot.apprtc.VIDEO_CALL", true), booleanExtra, booleanExtra2, i2, i, intent.getIntExtra("org.appspot.apprtc.VIDEO_FPS", 0), intent.getIntExtra("org.appspot.apprtc.VIDEO_BITRATE", 0), intent.getStringExtra("org.appspot.apprtc.VIDEOCODEC"), intent.getBooleanExtra("org.appspot.apprtc.HWCODEC", true), intent.getBooleanExtra("org.appspot.apprtc.FLEXFEC", false), intent.getIntExtra("org.appspot.apprtc.AUDIO_BITRATE", 0), intent.getStringExtra("org.appspot.apprtc.AUDIOCODEC"), intent.getBooleanExtra("org.appspot.apprtc.NOAUDIOPROCESSING", false), intent.getBooleanExtra("org.appspot.apprtc.AECDUMP", false), intent.getBooleanExtra("org.appspot.apprtc.OPENSLES", false), intent.getBooleanExtra("org.appspot.apprtc.DISABLE_BUILT_IN_AEC", false), intent.getBooleanExtra("org.appspot.apprtc.DISABLE_BUILT_IN_AGC", false), intent.getBooleanExtra("org.appspot.apprtc.DISABLE_BUILT_IN_NS", false), intent.getBooleanExtra("org.appspot.apprtc.ENABLE_LEVEL_CONTROL", false), intent.getBooleanExtra("org.appspot.apprtc.DISABLE_WEBRTC_GAIN_CONTROL", false), dataChannelParameters);
        this.peerConnectionParameters = peerConnectionParameters2;
        this.commandLineRun = intent.getBooleanExtra("org.appspot.apprtc.CMDLINE", false);
        int intExtra3 = intent.getIntExtra("org.appspot.apprtc.RUNTIME", 0);
        if (booleanExtra || !com.example.datingapp.Original.SSGDirectRTCClient.IP_PATTERN.matcher(stringExtra2).matches()) {
            this.appRtcClient = new com.example.datingapp.Original.SSGWebSocketRTCClient(this);
        } else {
            this.appRtcClient = new com.example.datingapp.Original.SSGDirectRTCClient(this);
        }
        this.roomConnectionParameters = new RoomConnectionParameters(data.toString(), stringExtra2, booleanExtra, intent.getStringExtra(EXTRA_URLPARAMETERS));
        if (com.example.datingapp.Original.SSGCpuMonitor.isSupported()) {
            com.example.datingapp.Original.SSGCpuMonitor sSGCpuMonitor = new com.example.datingapp.Original.SSGCpuMonitor(this);
            this.cpuMonitor = sSGCpuMonitor;
            this.hudFragment.setCpuMonitor(sSGCpuMonitor);
        }
        this.callFragment.setArguments(intent.getExtras());
        this.hudFragment.setArguments(intent.getExtras());
        FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
        beginTransaction.add(R.id.np_call_fragment_container, this.callFragment);
        beginTransaction.add(R.id.np_hud_fragment_container, this.hudFragment);
        beginTransaction.commit();
        if (this.commandLineRun && intExtra3 > 0) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                }
            }, intExtra3);
        }
        this.peerConnectionClient = com.example.datingapp.Original.SSGPeerConnectionClient.getInstance();
        if (booleanExtra) {
            Options options = new Options();
            options.networkIgnoreMask = 0;
            this.peerConnectionClient.setPeerConnectionFactoryOptions(options);
        }
        this.peerConnectionClient.createPeerConnectionFactory(getApplicationContext(), this.peerConnectionParameters, this);
        if (this.screencaptureEnabled) {
            startScreenCapture();
        } else {
            startCall();
        }
    }

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getApplication().getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private static int getSystemUiVisibility() {
        return VERSION.SDK_INT >= 19 ? 4102 : 6;
    }

    private void startScreenCapture() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(((MediaProjectionManager) getApplication().getSystemService(MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(), 1);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1) {
            mediaProjectionPermissionResultCode = i2;
            mediaProjectionPermissionResultData = intent;
            startCall();
        }
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra("org.appspot.apprtc.CAMERA2", true);
    }

    private boolean captureToTexture() {
        return getIntent().getBooleanExtra("org.appspot.apprtc.CAPTURETOTEXTURE", false);
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator cameraEnumerator) {
        String[] deviceNames = cameraEnumerator.getDeviceNames();
        String str = TAG;
        for (String str2 : deviceNames) {
            if (cameraEnumerator.isFrontFacing(str2)) {
                CameraVideoCapturer createCapturer = cameraEnumerator.createCapturer(str2, null);
                if (createCapturer != null) {
                    return createCapturer;
                }
            }
        }
        for (String str22 : deviceNames) {
            if (!cameraEnumerator.isFrontFacing(str22)) {
                CameraVideoCapturer createCapturer2 = cameraEnumerator.createCapturer(str22, null);
                if (createCapturer2 != null) {
                    return createCapturer2;
                }
            }
        }
        return null;
    }

    private VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode == -1) {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return new ScreenCapturerAndroid(mediaProjectionPermissionResultData, new Callback() {
                    public void onStop() {
                        SSGCallActivity.this.reportError("User revoked permission to capture the screen.");
                    }
                });
            }
        }
        reportError("User didn't give permission to capture the screen.");
        return null;
    }

    public void onStop() {
        super.onStop();
        this.activityRunning = false;
        com.example.datingapp.Original.SSGPeerConnectionClient sSGPeerConnectionClient = this.peerConnectionClient;
        if (sSGPeerConnectionClient != null && !this.screencaptureEnabled) {
            sSGPeerConnectionClient.stopVideoSource();
        }
        com.example.datingapp.Original.SSGCpuMonitor sSGCpuMonitor = this.cpuMonitor;
        if (sSGCpuMonitor != null) {
            sSGCpuMonitor.pause();
        }
    }

    public void onStart() {
        super.onStart();
        this.activityRunning = true;
        com.example.datingapp.Original.SSGPeerConnectionClient sSGPeerConnectionClient = this.peerConnectionClient;
        if (sSGPeerConnectionClient != null && !this.screencaptureEnabled) {
            sSGPeerConnectionClient.startVideoSource();
        }
    }

    public void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        Toast toast = this.logToast;
        if (toast != null) {
            toast.cancel();
        }
        this.activityRunning = false;
        super.onDestroy();
    }

    public void onCallHangUp() {
        disconnect();
    }

    public void onCameraSwitch() {
        com.example.datingapp.Original.SSGPeerConnectionClient sSGPeerConnectionClient = this.peerConnectionClient;
        if (sSGPeerConnectionClient != null) {
            sSGPeerConnectionClient.switchCamera();
        }
    }

    public void onVideoScalingSwitch(ScalingType scalingType) {
        this.fullscreenRenderer.setScalingType(scalingType);
    }

    public void onCaptureFormatChange(int i, int i2, int i3) {
        com.example.datingapp.Original.SSGPeerConnectionClient sSGPeerConnectionClient = this.peerConnectionClient;
        if (sSGPeerConnectionClient != null) {
            sSGPeerConnectionClient.changeCaptureFormat(i, i2, i3);
        }
    }

    public boolean onToggleMic() {
        com.example.datingapp.Original.SSGPeerConnectionClient sSGPeerConnectionClient = this.peerConnectionClient;
        if (sSGPeerConnectionClient != null) {
            boolean z = !this.micEnabled;
            this.micEnabled = z;
            sSGPeerConnectionClient.setAudioEnabled(z);
        }
        return this.micEnabled;
    }

    private void toggleCallControlFragmentVisibility() {
        if (this.iceConnected && this.callFragment.isAdded()) {
            this.callControlFragmentVisible = !this.callControlFragmentVisible;
            FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
            if (this.callControlFragmentVisible) {
                beginTransaction.show(this.callFragment);
                beginTransaction.show(this.hudFragment);
            } else {
                beginTransaction.hide(this.callFragment);
                beginTransaction.hide(this.hudFragment);
            }
            beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            beginTransaction.commit();
        }
    }

    private void startCall() {
        if (this.appRtcClient != null) {
            this.callStartedTimeMs = System.currentTimeMillis();
            logAndToast(getString(R.string.connecting_to, this.roomConnectionParameters.np_roomUrl));
            this.appRtcClient.connectToRoom(this.roomConnectionParameters);
            com.example.datingapp.Original.SSGRTCAudioManager create = com.example.datingapp.Original.SSGRTCAudioManager.create(getApplicationContext());
            this.audioManager = create;
            create.start(new AudioManagerEvents() {
                public void onAudioDeviceChanged(AudioDevice audioDevice, Set<AudioDevice> set) {
                    SSGCallActivity.this.onAudioManagerDevicesChanged(audioDevice, set);
                }
            });
        }
    }

    public void callConnected() {
        long currentTimeMillis = System.currentTimeMillis() - this.callStartedTimeMs;
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Call connected: delay=");
        sb.append(currentTimeMillis);
        sb.append("ms");
        Log.i(str, sb.toString());
        com.example.datingapp.Original.SSGPeerConnectionClient sSGPeerConnectionClient = this.peerConnectionClient;
        if (sSGPeerConnectionClient == null || this.isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        sSGPeerConnectionClient.enableStatsEvents(true, 1000);
        setSwappedFeeds(false);
    }

    public void onAudioManagerDevicesChanged(AudioDevice audioDevice, Set<AudioDevice> set) {
    }

    private void disconnect() {
        this.activityRunning = false;
        this.remoteProxyRenderer.setTarget(null);
        this.localProxyRenderer.setTarget(null);
        com.example.datingapp.Original.SSGRTCClient sSGRTCClient = this.appRtcClient;
        if (sSGRTCClient != null) {
            sSGRTCClient.disconnectFromRoom();
            this.appRtcClient = null;
        }
        SurfaceViewRenderer surfaceViewRenderer = this.pipRenderer;
        if (surfaceViewRenderer != null) {
            surfaceViewRenderer.release();
            this.pipRenderer = null;
        }
        VideoFileRenderer videoFileRenderer2 = this.videoFileRenderer;
        if (videoFileRenderer2 != null) {
            videoFileRenderer2.release();
            this.videoFileRenderer = null;
        }
        SurfaceViewRenderer surfaceViewRenderer2 = this.fullscreenRenderer;
        if (surfaceViewRenderer2 != null) {
            surfaceViewRenderer2.release();
            this.fullscreenRenderer = null;
        }
        com.example.datingapp.Original.SSGPeerConnectionClient sSGPeerConnectionClient = this.peerConnectionClient;
        if (sSGPeerConnectionClient != null) {
            sSGPeerConnectionClient.close();
            this.peerConnectionClient = null;
        }
        com.example.datingapp.Original.SSGRTCAudioManager sSGRTCAudioManager = this.audioManager;
        if (sSGRTCAudioManager != null) {
            sSGRTCAudioManager.stop();
            this.audioManager = null;
        }
        if (!this.iceConnected || this.isError) {
            setResult(0);
        } else {
            setResult(-1);
        }
//        finish();
    }

    public void disconnect2() {
        com.example.datingapp.Original.SSGRTCClient sSGRTCClient = this.appRtcClient;
        if (sSGRTCClient != null) {
            sSGRTCClient.disconnectFromRoom();
            this.appRtcClient = null;
        }
        com.example.datingapp.Original.SSGStaticVar.incall = 1;
    }

    public void disconnectWithErrorMessage(String str) {
        disconnect2();
    }

    public void logAndToast(String str) {
        Toast toast = this.logToast;
        if (toast != null) {
            toast.cancel();
        }
    }

    public void reportError(final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (!SSGCallActivity.this.isError) {
                    SSGCallActivity.this.isError = true;
                    SSGCallActivity.this.disconnectWithErrorMessage(str);
                }
            }
        });
    }

    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        String stringExtra = getIntent().getStringExtra("org.appspot.apprtc.VIDEO_FILE_AS_CAMERA");
        if (stringExtra != null) {
            try {
                videoCapturer = new FileVideoCapturer(stringExtra);
            } catch (IOException e) {
                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (this.screencaptureEnabled) {
            return createScreenCapturer();
        } else {
            boolean useCamera2 = useCamera2();
            if (!useCamera2) {
                videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
            } else if (!captureToTexture()) {
                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            } else {
                videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
            }
        }
        if (videoCapturer != null) {
            return videoCapturer;
        }
        reportError("Failed to open camera");
        return null;
    }

    private void setSwappedFeeds(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("setSwappedFeeds: ");
        sb.append(z);
        this.isSwappedFeeds = z;
        this.localProxyRenderer.setTarget(z ? this.fullscreenRenderer : this.pipRenderer);
        this.remoteProxyRenderer.setTarget(z ? this.pipRenderer : this.fullscreenRenderer);
        this.fullscreenRenderer.setMirror(z);
        this.pipRenderer.setMirror(!z);
    }

    public void onConnectedToRoomInternal(SignalingParameters signalingParameters2) {
        long currentTimeMillis = System.currentTimeMillis() - this.callStartedTimeMs;
        this.signalingParameters = signalingParameters2;
        StringBuilder sb = new StringBuilder();
        sb.append("Creating peer connection, delay=");
        sb.append(currentTimeMillis);
        sb.append("ms");
        logAndToast(sb.toString());
        VideoCapturer videoCapturer = null;
        if (this.peerConnectionParameters.np_videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }
        this.peerConnectionClient.createPeerConnection(this.rootEglBase.getEglBaseContext(), this.localProxyRenderer, this.remoteRenderers, videoCapturer, this.signalingParameters);
        if (com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
            logAndToast("Creating OFFER...");
            this.peerConnectionClient.createOffer();
            return;
        }
        if (signalingParameters2.np_offerSdp != null) {
            this.peerConnectionClient.setRemoteDescription(signalingParameters2.np_offerSdp);
            logAndToast("Creating ANSWER...");
            this.peerConnectionClient.createAnswer();
        }
        if (signalingParameters2.np_iceCandidates != null) {
            for (IceCandidate addRemoteIceCandidate : signalingParameters2.np_iceCandidates) {
                this.peerConnectionClient.addRemoteIceCandidate(addRemoteIceCandidate);
            }
        }
    }

    public void onConnectedToRoom(final SignalingParameters signalingParameters2) {
        runOnUiThread(new Runnable() {
            public void run() {
                SSGCallActivity.this.onConnectedToRoomInternal(signalingParameters2);
            }
        });
    }

    public void onRemoteDescription(final SessionDescription sessionDescription) {
        final long currentTimeMillis = System.currentTimeMillis() - this.callStartedTimeMs;
        runOnUiThread(new Runnable() {
            public void run() {
                if (SSGCallActivity.this.peerConnectionClient == null) {
                    Log.e(SSGCallActivity.TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                SSGCallActivity callActivity = SSGCallActivity.this;
                StringBuilder sb = new StringBuilder();
                sb.append("Received remote ");
                sb.append(sessionDescription.type);
                sb.append(", delay=");
                sb.append(currentTimeMillis);
                sb.append("ms");
                callActivity.logAndToast(sb.toString());
                SSGCallActivity.this.peerConnectionClient.setRemoteDescription(sessionDescription);
                if (!com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
                    SSGCallActivity.this.logAndToast("Creating ANSWER...");
                    SSGCallActivity.this.peerConnectionClient.createAnswer();
                }
            }
        });
    }

    public void onRemoteIceCandidate(final IceCandidate iceCandidate) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (SSGCallActivity.this.peerConnectionClient == null) {
                    Log.e(SSGCallActivity.TAG, "Received ICE candidate for a non-initialized peer connection.");
                } else {
                    SSGCallActivity.this.peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        });
    }

    public void onRemoteIceCandidatesRemoved(final IceCandidate[] iceCandidateArr) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (SSGCallActivity.this.peerConnectionClient == null) {
                    Log.e(SSGCallActivity.TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                } else {
                    SSGCallActivity.this.peerConnectionClient.removeRemoteIceCandidates(iceCandidateArr);
                }
            }
        });
    }

    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            public void run() {
                SSGCallActivity.this.logAndToast("Remote end hung up; dropping PeerConnection");
                SSGCallActivity.this.disconnect2();
            }
        });
    }

    public void onChannelError(String str) {
        reportError(str);
    }

    public void onLocalDescription(final SessionDescription sessionDescription) {
        final long currentTimeMillis = System.currentTimeMillis() - this.callStartedTimeMs;
        runOnUiThread(new Runnable() {
            public void run() {
                if (SSGCallActivity.this.appRtcClient != null) {
                    SSGCallActivity callActivity = SSGCallActivity.this;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Sending ");
                    sb.append(sessionDescription.type);
                    sb.append(", delay=");
                    sb.append(currentTimeMillis);
                    sb.append("ms");
                    callActivity.logAndToast(sb.toString());
                    if (com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
                        SSGCallActivity.this.appRtcClient.sendOfferSdp(sessionDescription);
                    } else {
                        SSGCallActivity.this.appRtcClient.sendAnswerSdp(sessionDescription);
                    }
                }
                if (SSGCallActivity.this.peerConnectionParameters.videoMaxBitrate > 0) {
                    SSGCallActivity.this.peerConnectionClient.setVideoMaxBitrate(Integer.valueOf(SSGCallActivity.this.peerConnectionParameters.videoMaxBitrate));
                }
            }
        });
    }

    public void onIceCandidate(final IceCandidate iceCandidate) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (SSGCallActivity.this.appRtcClient != null) {
                    SSGCallActivity.this.appRtcClient.sendLocalIceCandidate(iceCandidate);
                }
            }
        });
    }

    public void onIceCandidatesRemoved(final IceCandidate[] iceCandidateArr) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (SSGCallActivity.this.appRtcClient != null) {
                    SSGCallActivity.this.appRtcClient.sendLocalIceCandidateRemovals(iceCandidateArr);
                }
            }
        });
    }

    public void onIceConnected() {
        final long currentTimeMillis = System.currentTimeMillis() - this.callStartedTimeMs;
        runOnUiThread(new Runnable() {
            public void run() {
                SSGCallActivity callActivity = SSGCallActivity.this;
                StringBuilder sb = new StringBuilder();
                sb.append("ICE connected, delay=");
                sb.append(currentTimeMillis);
                sb.append("ms");
                callActivity.logAndToast(sb.toString());
                SSGCallActivity.this.callFragment.updateEncoderStatistics("Connected");
                SSGCallActivity.this.iceConnected = true;
                SSGCallActivity.this.callConnected();
            }
        });
    }

    public void onPeerConnectionStatsReady(final StatsReport[] statsReportArr) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (!SSGCallActivity.this.isError && SSGCallActivity.this.iceConnected) {
                    SSGCallActivity.this.hudFragment.updateEncoderStatistics(statsReportArr);
                }
            }
        });
    }

    public void onPeerConnectionError(String str) {
        reportError(str);
    }
}
