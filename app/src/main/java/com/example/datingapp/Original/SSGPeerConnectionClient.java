package com.example.datingapp.Original;

import android.content.Context;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.datingapp.Original.SSGRTCClient.SignalingParameters;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DataChannel.Buffer;
import org.webrtc.DataChannel.Init;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.Logging.Severity;
import org.webrtc.Logging.TraceLevel;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.BundlePolicy;
import org.webrtc.PeerConnection.ContinualGatheringPolicy;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.KeyType;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.RTCConfiguration;
import org.webrtc.PeerConnection.RtcpMuxPolicy;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.PeerConnection.TcpCandidatePolicy;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.PeerConnectionFactory.Options;
import org.webrtc.RtpParameters;
import org.webrtc.RtpParameters.Encoding;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioRecord.AudioRecordStartErrorCode;
import org.webrtc.voiceengine.WebRtcAudioRecord.WebRtcAudioRecordErrorCallback;
import org.webrtc.voiceengine.WebRtcAudioTrack;
import org.webrtc.voiceengine.WebRtcAudioTrack.WebRtcAudioTrackErrorCallback;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSGPeerConnectionClient {
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final int BPS_IN_KBPS = 1000;
    private static final String DISABLE_WEBRTC_AGC_FIELDTRIAL = "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final String TAG = "PCRTCClient";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String VIDEO_CODEC_H264_BASELINE = "H264 Baseline";
    private static final String VIDEO_CODEC_H264_HIGH = "H264 High";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_FLEXFEC_FIELDTRIAL = "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String VIDEO_H264_HIGH_PROFILE_FIELDTRIAL = "WebRTC-H264HighProfile/Enabled/";
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String VIDEO_TRACK_TYPE = "video";
    private static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    private static final SSGPeerConnectionClient instance = new SSGPeerConnectionClient();
    private AudioSource audioSource;
    private DataChannel dataChannel;
    /* access modifiers changed from: private */
    public final ExecutorService executor = Executors.newSingleThreadExecutor();
    /* access modifiers changed from: private */
    public boolean isError;
    private MediaStream mediaStream;
    private ParcelFileDescriptor np_aecDumpFileDescriptor;
    private MediaConstraints np_audioConstraints;
    /* access modifiers changed from: private */
    public boolean np_dataChannelEnabled;
    /* access modifiers changed from: private */
    public boolean np_enableAudio;
    /* access modifiers changed from: private */
    public PeerConnectionEvents np_events;
    private PeerConnectionFactory np_factory;
    /* access modifiers changed from: private */
    public boolean np_isInitiator;
    /* access modifiers changed from: private */
    public AudioTrack np_localAudioTrack;
    private Callbacks np_localRender;
    /* access modifiers changed from: private */
    public SessionDescription np_localSdp;
    /* access modifiers changed from: private */
    public RtpSender np_localVideoSender;
    /* access modifiers changed from: private */
    public VideoTrack np_localVideoTrack;
    private MediaConstraints np_pcConstraints;
    /* access modifiers changed from: private */
    public boolean np_preferIsac;
    /* access modifiers changed from: private */
    public String np_preferredVideoCodec;
    /* access modifiers changed from: private */
    public LinkedList<IceCandidate> np_queuedRemoteCandidates;
    /* access modifiers changed from: private */
    public List<Callbacks> np_remoteRenders;
    /* access modifiers changed from: private */
    public VideoTrack np_remoteVideoTrack;
    /* access modifiers changed from: private */
    public boolean np_renderVideo;
    /* access modifiers changed from: private */
    public MediaConstraints np_sdpMediaConstraints;
    private Timer np_statsTimer;
    /* access modifiers changed from: private */
    public boolean np_videoCallEnabled;
    /* access modifiers changed from: private */
    public boolean np_videoCapturerStopped;
    /* access modifiers changed from: private */
    public int np_videoFps;
    /* access modifiers changed from: private */
    public int np_videoHeight;
    /* access modifiers changed from: private */
    public int np_videoWidth;
    Options options = null;
    private final PCObserver pcObserver = new PCObserver();
    /* access modifiers changed from: private */
    public PeerConnection peerConnection;
    /* access modifiers changed from: private */
    public PeerConnectionParameters peerConnectionParameters;
    /* access modifiers changed from: private */
    public final SDPObserver sdpObserver = new SDPObserver();
    private SignalingParameters signalingParameters;
    /* access modifiers changed from: private */
    public VideoCapturer videoCapturer;
    private VideoSource videoSource;

    public static class DataChannelParameters {
        public final int id;
        public final int maxRetransmitTimeMs;
        public final int maxRetransmits;
        public final boolean negotiated;
        public final boolean ordered;
        public final String protocol;

        public DataChannelParameters(boolean ordered2, int maxRetransmitTimeMs2, int maxRetransmits2, String protocol2, boolean negotiated2, int id2) {
            this.ordered = ordered2;
            this.maxRetransmitTimeMs = maxRetransmitTimeMs2;
            this.maxRetransmits = maxRetransmits2;
            this.protocol = protocol2;
            this.negotiated = negotiated2;
            this.id = id2;
        }
    }

    private class PCObserver implements Observer {
        private PCObserver() {
        }

        public void onIceCandidate(final IceCandidate candidate) {
            SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                public void run() {
                    SSGPeerConnectionClient.this.np_events.onIceCandidate(candidate);
                }
            });
        }

        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                public void run() {
                    SSGPeerConnectionClient.this.np_events.onIceCandidatesRemoved(candidates);
                }
            });
        }

        public void onSignalingChange(SignalingState newState) {
        }

        public void onIceConnectionChange(final IceConnectionState newState) {
            SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                public void run() {
                    if (newState == IceConnectionState.CONNECTED) {
                        SSGPeerConnectionClient.this.np_events.onIceConnected();
                    } else if (newState == IceConnectionState.DISCONNECTED) {
                        SSGPeerConnectionClient.this.np_events.onIceDisconnected();
                    } else if (newState == IceConnectionState.FAILED) {
                        SSGPeerConnectionClient.this.reportError("ICE connection failed.");
                    }
                }
            });
        }

        public void onIceGatheringChange(IceGatheringState newState) {
        }

        public void onIceConnectionReceivingChange(boolean receiving) {
        }

        public void onAddStream(final MediaStream stream) {
            SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                public void run() {
                    if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                        if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
                            SSGPeerConnectionClient sSGPeerConnectionClient = SSGPeerConnectionClient.this;
                            StringBuilder sb = new StringBuilder();
                            sb.append("Weird-looking stream: ");
                            sb.append(stream);
                            sSGPeerConnectionClient.reportError(sb.toString());
                            return;
                        }
                        if (stream.videoTracks.size() == 1) {
                            SSGPeerConnectionClient.this.np_remoteVideoTrack = (VideoTrack) stream.videoTracks.get(0);
                            SSGPeerConnectionClient.this.np_remoteVideoTrack.setEnabled(SSGPeerConnectionClient.this.np_renderVideo);
                            for (Callbacks remoteRender : SSGPeerConnectionClient.this.np_remoteRenders) {
                                SSGPeerConnectionClient.this.np_remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
                            }
                        }
                    }
                }
            });
        }

        public void onRemoveStream(MediaStream stream) {
            SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                public void run() {
                    SSGPeerConnectionClient.this.np_remoteVideoTrack = null;
                }
            });
        }

        public void onDataChannel(final DataChannel dc) {
            if (SSGPeerConnectionClient.this.np_dataChannelEnabled) {
                dc.registerObserver(new DataChannel.Observer() {
                    public void onBufferedAmountChange(long previousAmount) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Data channel buffered amount changed: ");
                        sb.append(dc.label());
                        sb.append(": ");
                        sb.append(dc.state());
                        Log.d(SSGPeerConnectionClient.TAG, sb.toString());
                    }

                    public void onStateChange() {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Data channel state changed: ");
                        sb.append(dc.label());
                        sb.append(": ");
                        sb.append(dc.state());
                        Log.d(SSGPeerConnectionClient.TAG, sb.toString());
                    }

                    public void onMessage(Buffer buffer) {
                        if (!buffer.binary) {
                            ByteBuffer data = buffer.data;
                            byte[] bytes = new byte[data.capacity()];
                            data.get(bytes);
                            new String(bytes);
                        }
                    }
                });
            }
        }

        public void onRenegotiationNeeded() {
        }

        public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {
        }
    }

    public interface PeerConnectionEvents {
        void onIceCandidate(IceCandidate iceCandidate);

        void onIceCandidatesRemoved(IceCandidate[] iceCandidateArr);

        void onIceConnected();

        void onIceDisconnected();

        void onLocalDescription(SessionDescription sessionDescription);

        void onPeerConnectionClosed();

        void onPeerConnectionError(String str);

        void onPeerConnectionStatsReady(StatsReport[] statsReportArr);
    }

    public static class PeerConnectionParameters {
        public final boolean aecDump;
        public final String audioCodec;
        public final int audioStartBitrate;
        /* access modifiers changed from: private */
        public final DataChannelParameters dataChannelParameters;
        public final boolean disableBuiltInAEC;
        public final boolean disableBuiltInAGC;
        public final boolean disableBuiltInNS;
        public final boolean disableWebRtcAGCAndHPF;
        public final boolean enableLevelControl;
        public final boolean loopback;
        public final boolean noAudioProcessing;
        public final boolean np_videoCallEnabled;
        public final int np_videoFps;
        public final int np_videoHeight;
        public final int np_videoWidth;
        public final boolean tracing;
        public final boolean useOpenSLES;
        public final String videoCodec;
        public final boolean videoCodecHwAcceleration;
        public final boolean videoFlexfecEnabled;
        public final int videoMaxBitrate;

        public PeerConnectionParameters(boolean np_videoCallEnabled2, boolean loopback2, boolean tracing2, int np_videoWidth2, int np_videoHeight2, int np_videoFps2, int videoMaxBitrate2, String videoCodec2, boolean videoCodecHwAcceleration2, boolean videoFlexfecEnabled2, int audioStartBitrate2, String audioCodec2, boolean noAudioProcessing2, boolean aecDump2, boolean useOpenSLES2, boolean disableBuiltInAEC2, boolean disableBuiltInAGC2, boolean disableBuiltInNS2, boolean enableLevelControl2, boolean disableWebRtcAGCAndHPF2) {
            this(np_videoCallEnabled2, loopback2, tracing2, np_videoWidth2, np_videoHeight2, np_videoFps2, videoMaxBitrate2, videoCodec2, videoCodecHwAcceleration2, videoFlexfecEnabled2, audioStartBitrate2, audioCodec2, noAudioProcessing2, aecDump2, useOpenSLES2, disableBuiltInAEC2, disableBuiltInAGC2, disableBuiltInNS2, enableLevelControl2, disableWebRtcAGCAndHPF2, null);
        }

        public PeerConnectionParameters(boolean np_videoCallEnabled2, boolean loopback2, boolean tracing2, int np_videoWidth2, int np_videoHeight2, int np_videoFps2, int videoMaxBitrate2, String videoCodec2, boolean videoCodecHwAcceleration2, boolean videoFlexfecEnabled2, int audioStartBitrate2, String audioCodec2, boolean noAudioProcessing2, boolean aecDump2, boolean useOpenSLES2, boolean disableBuiltInAEC2, boolean disableBuiltInAGC2, boolean disableBuiltInNS2, boolean enableLevelControl2, boolean disableWebRtcAGCAndHPF2, DataChannelParameters dataChannelParameters2) {
            this.np_videoCallEnabled = np_videoCallEnabled2;
            this.loopback = loopback2;
            this.tracing = tracing2;
            this.np_videoWidth = np_videoWidth2;
            this.np_videoHeight = np_videoHeight2;
            this.np_videoFps = np_videoFps2;
            this.videoMaxBitrate = videoMaxBitrate2;
            this.videoCodec = videoCodec2;
            this.videoFlexfecEnabled = videoFlexfecEnabled2;
            this.videoCodecHwAcceleration = videoCodecHwAcceleration2;
            this.audioStartBitrate = audioStartBitrate2;
            this.audioCodec = audioCodec2;
            this.noAudioProcessing = noAudioProcessing2;
            this.aecDump = aecDump2;
            this.useOpenSLES = useOpenSLES2;
            this.disableBuiltInAEC = disableBuiltInAEC2;
            this.disableBuiltInAGC = disableBuiltInAGC2;
            this.disableBuiltInNS = disableBuiltInNS2;
            this.enableLevelControl = enableLevelControl2;
            this.disableWebRtcAGCAndHPF = disableWebRtcAGCAndHPF2;
            this.dataChannelParameters = dataChannelParameters2;
        }
    }

    private class SDPObserver implements SdpObserver {
        private SDPObserver() {
        }

        public void onCreateSuccess(SessionDescription origSdp) {
            if (SSGPeerConnectionClient.this.np_localSdp != null) {
                SSGPeerConnectionClient.this.reportError("Multiple SDP create.");
                return;
            }
            String sdpDescription = origSdp.description;
            if (SSGPeerConnectionClient.this.np_preferIsac) {
                sdpDescription = SSGPeerConnectionClient.preferCodec(sdpDescription, SSGPeerConnectionClient.AUDIO_CODEC_ISAC, true);
            }
            if (SSGPeerConnectionClient.this.np_videoCallEnabled) {
                sdpDescription = SSGPeerConnectionClient.preferCodec(sdpDescription, SSGPeerConnectionClient.this.np_preferredVideoCodec, false);
            }
            final SessionDescription sdp = new SessionDescription(origSdp.type, sdpDescription);
            SSGPeerConnectionClient.this.np_localSdp = sdp;
            SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                public void run() {
                    if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                        SSGPeerConnectionClient.this.peerConnection.setLocalDescription(SSGPeerConnectionClient.this.sdpObserver, sdp);
                    }
                }
            });
        }

        public void onSetSuccess() {
            SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                public void run() {
                    if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                        if (SSGPeerConnectionClient.this.np_isInitiator) {
                            if (SSGPeerConnectionClient.this.peerConnection.getRemoteDescription() == null) {
                                SSGPeerConnectionClient.this.np_events.onLocalDescription(SSGPeerConnectionClient.this.np_localSdp);
                            } else {
                                SSGPeerConnectionClient.this.drainCandidates();
                            }
                        } else if (SSGPeerConnectionClient.this.peerConnection.getLocalDescription() != null) {
                            SSGPeerConnectionClient.this.np_events.onLocalDescription(SSGPeerConnectionClient.this.np_localSdp);
                            SSGPeerConnectionClient.this.drainCandidates();
                        }
                    }
                }
            });
        }

        public void onCreateFailure(String error) {
            SSGPeerConnectionClient sSGPeerConnectionClient = SSGPeerConnectionClient.this;
            StringBuilder sb = new StringBuilder();
            sb.append("createSDP error: ");
            sb.append(error);
            sSGPeerConnectionClient.reportError(sb.toString());
        }

        public void onSetFailure(String error) {
            SSGPeerConnectionClient sSGPeerConnectionClient = SSGPeerConnectionClient.this;
            StringBuilder sb = new StringBuilder();
            sb.append("setSDP error: ");
            sb.append(error);
            sSGPeerConnectionClient.reportError(sb.toString());
        }
    }

    public static SSGPeerConnectionClient getInstance() {
        return instance;
    }

    public void setPeerConnectionFactoryOptions(Options options2) {
        this.options = options2;
    }

    public void createPeerConnectionFactory(final Context context, PeerConnectionParameters peerConnectionParameters2, PeerConnectionEvents np_events2) {
        this.peerConnectionParameters = peerConnectionParameters2;
        this.np_events = np_events2;
        this.np_videoCallEnabled = peerConnectionParameters2.np_videoCallEnabled;
        this.np_dataChannelEnabled = peerConnectionParameters2.dataChannelParameters != null;
        this.np_factory = null;
        this.peerConnection = null;
        this.np_preferIsac = false;
        this.np_videoCapturerStopped = false;
        this.isError = false;
        this.np_queuedRemoteCandidates = null;
        this.np_localSdp = null;
        this.mediaStream = null;
        this.videoCapturer = null;
        this.np_renderVideo = true;
        this.np_localVideoTrack = null;
        this.np_remoteVideoTrack = null;
        this.np_localVideoSender = null;
        this.np_enableAudio = true;
        this.np_localAudioTrack = null;
        this.np_statsTimer = new Timer();
        this.executor.execute(new Runnable() {
            public void run() {
                SSGPeerConnectionClient.this.createPeerConnectionFactoryInternal(context);
            }
        });
    }

    public void createPeerConnection(EglBase.Context renderEGLContext, Callbacks np_localRender2, Callbacks remoteRender, VideoCapturer videoCapturer2, SignalingParameters signalingParameters2) {
        createPeerConnection(renderEGLContext, np_localRender2, Collections.singletonList(remoteRender), videoCapturer2, signalingParameters2);
    }

    public void createPeerConnection(final EglBase.Context renderEGLContext, Callbacks np_localRender2, List<Callbacks> np_remoteRenders2, VideoCapturer videoCapturer2, SignalingParameters signalingParameters2) {
        if (this.peerConnectionParameters == null) {
            Log.e(TAG, "Creating peer connection without initializing np_factory.");
            return;
        }
        this.np_localRender = np_localRender2;
        this.np_remoteRenders = np_remoteRenders2;
        this.videoCapturer = videoCapturer2;
        this.signalingParameters = signalingParameters2;
        this.executor.execute(new Runnable() {
            public void run() {
                try {
                    SSGPeerConnectionClient.this.createMediaConstraintsInternal();
                    SSGPeerConnectionClient.this.createPeerConnectionInternal(renderEGLContext);
                } catch (Exception e) {
                    SSGPeerConnectionClient sSGPeerConnectionClient = SSGPeerConnectionClient.this;
                    StringBuilder sb = new StringBuilder();
                    sb.append("Failed to create peer connection: ");
                    sb.append(e.getMessage());
                    sSGPeerConnectionClient.reportError(sb.toString());
                    throw e;
                }
            }
        });
    }

    public void close() {
        this.executor.execute(new Runnable() {
            public void run() {
                SSGPeerConnectionClient.this.closeInternal();
            }
        });
    }

    public boolean isVideoCallEnabled() {
        return this.np_videoCallEnabled;
    }

    /* access modifiers changed from: private */
    public void createPeerConnectionFactoryInternal(Context context) {
        PeerConnectionFactory.initializeInternalTracer();
        if (this.peerConnectionParameters.tracing) {
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
            sb.append(File.separator);
            sb.append("webrtc-trace.txt");
            PeerConnectionFactory.startInternalTracingCapture(sb.toString());
        }
        this.isError = false;
        String fieldTrials = "";
        if (this.peerConnectionParameters.videoFlexfecEnabled) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(fieldTrials);
            sb2.append(VIDEO_FLEXFEC_FIELDTRIAL);
            fieldTrials = sb2.toString();
        }
        StringBuilder sb3 = new StringBuilder();
        sb3.append(fieldTrials);
        sb3.append(VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL);
        String fieldTrials2 = sb3.toString();
        if (this.peerConnectionParameters.disableWebRtcAGCAndHPF) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(fieldTrials2);
            sb4.append(DISABLE_WEBRTC_AGC_FIELDTRIAL);
            fieldTrials2 = sb4.toString();
        }
        String str = VIDEO_CODEC_VP8;
        this.np_preferredVideoCodec = str;
        if (this.np_videoCallEnabled && this.peerConnectionParameters.videoCodec != null) {
            String str2 = this.peerConnectionParameters.videoCodec;
            char c = 65535;
            int hashCode = str2.hashCode();
            String str3 = VIDEO_CODEC_VP9;
            switch (hashCode) {
                case -2140422726:
                    if (str2.equals(VIDEO_CODEC_H264_HIGH)) {
                        c = 3;
                        break;
                    }
                    break;
                case -1031013795:
                    if (str2.equals(VIDEO_CODEC_H264_BASELINE)) {
                        c = 2;
                        break;
                    }
                    break;
                case 85182:
                    if (str2.equals(str)) {
                        c = 0;
                        break;
                    }
                    break;
                case 85183:
                    if (str2.equals(str3)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            String str4 = VIDEO_CODEC_H264;
            switch (c) {
                case 0:
                    this.np_preferredVideoCodec = str;
                    break;
                case 1:
                    this.np_preferredVideoCodec = str3;
                    break;
                case 2:
                    this.np_preferredVideoCodec = str4;
                    break;
                case 3:
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append(fieldTrials2);
                    sb5.append(VIDEO_H264_HIGH_PROFILE_FIELDTRIAL);
                    fieldTrials2 = sb5.toString();
                    this.np_preferredVideoCodec = str4;
                    break;
                default:
                    this.np_preferredVideoCodec = str;
                    break;
            }
        }
        PeerConnectionFactory.initializeFieldTrials(fieldTrials2);
        this.np_preferIsac = this.peerConnectionParameters.audioCodec != null && this.peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);
        if (!this.peerConnectionParameters.useOpenSLES) {
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true);
        } else {
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
        }
        if (this.peerConnectionParameters.disableBuiltInAEC) {
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        } else {
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);
        }
        if (this.peerConnectionParameters.disableBuiltInAGC) {
            WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(true);
        } else {
            WebRtcAudioUtils.setWebRtcBasedAutomaticGainControl(false);
        }
        if (this.peerConnectionParameters.disableBuiltInNS) {
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(true);
        } else {
            WebRtcAudioUtils.setWebRtcBasedNoiseSuppressor(false);
        }
        WebRtcAudioRecord.setErrorCallback(new WebRtcAudioRecordErrorCallback() {
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                SSGPeerConnectionClient.this.reportError(errorMessage);
            }

            public void onWebRtcAudioRecordStartError(AudioRecordStartErrorCode errorCode, String errorMessage) {
                SSGPeerConnectionClient.this.reportError(errorMessage);
            }

            public void onWebRtcAudioRecordError(String errorMessage) {
                SSGPeerConnectionClient.this.reportError(errorMessage);
            }
        });
        WebRtcAudioTrack.setErrorCallback(new WebRtcAudioTrackErrorCallback() {
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                SSGPeerConnectionClient.this.reportError(errorMessage);
            }

            public void onWebRtcAudioTrackStartError(String errorMessage) {
                SSGPeerConnectionClient.this.reportError(errorMessage);
            }

            public void onWebRtcAudioTrackError(String errorMessage) {
                SSGPeerConnectionClient.this.reportError(errorMessage);
            }
        });
        PeerConnectionFactory.initializeAndroidGlobals(context, this.peerConnectionParameters.videoCodecHwAcceleration);
        Options options2 = this.options;
        String str5 = TAG;
        if (options2 != null) {
            StringBuilder sb6 = new StringBuilder();
            sb6.append("np_factory networkIgnoreMask option: ");
            sb6.append(this.options.networkIgnoreMask);
            Log.d(str5, sb6.toString());
        }
        this.np_factory = new PeerConnectionFactory(this.options);
        Log.d(str5, "Peer connection np_factory created.");
    }

    /* access modifiers changed from: private */
    public void createMediaConstraintsInternal() {
        this.np_pcConstraints = new MediaConstraints();
        boolean z = this.peerConnectionParameters.loopback;
        String str = DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT;
        String str2 = "true";
        String str3 = "false";
        if (z) {
            this.np_pcConstraints.optional.add(new KeyValuePair(str, str3));
        } else {
            this.np_pcConstraints.optional.add(new KeyValuePair(str, str2));
        }
        VideoCapturer videoCapturer2 = this.videoCapturer;
        String str4 = TAG;
        if (videoCapturer2 == null) {
            Log.w(str4, "No camera on device. Switch to audio only call.");
            this.np_videoCallEnabled = false;
        }
        if (this.np_videoCallEnabled) {
            this.np_videoWidth = this.peerConnectionParameters.np_videoWidth;
            this.np_videoHeight = this.peerConnectionParameters.np_videoHeight;
            int i = this.peerConnectionParameters.np_videoFps;
            this.np_videoFps = i;
            if (this.np_videoWidth == 0 || this.np_videoHeight == 0) {
                this.np_videoWidth = HD_VIDEO_WIDTH;
                this.np_videoHeight = HD_VIDEO_HEIGHT;
            }
            if (i == 0) {
                this.np_videoFps = 30;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Capturing format: ");
            sb.append(this.np_videoWidth);
            sb.append("x");
            sb.append(this.np_videoHeight);
            sb.append("@");
            sb.append(this.np_videoFps);
            Logging.d(str4, sb.toString());
        }
        this.np_audioConstraints = new MediaConstraints();
        if (this.peerConnectionParameters.noAudioProcessing) {
            this.np_audioConstraints.mandatory.add(new KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, str3));
            this.np_audioConstraints.mandatory.add(new KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, str3));
            this.np_audioConstraints.mandatory.add(new KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, str3));
            this.np_audioConstraints.mandatory.add(new KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, str3));
        }
        if (this.peerConnectionParameters.enableLevelControl) {
            this.np_audioConstraints.mandatory.add(new KeyValuePair(AUDIO_LEVEL_CONTROL_CONSTRAINT, str2));
        }
        MediaConstraints mediaConstraints = new MediaConstraints();
        this.np_sdpMediaConstraints = mediaConstraints;
        mediaConstraints.mandatory.add(new KeyValuePair("OfferToReceiveAudio", str2));
        String str5 = "OfferToReceiveVideo";
        if (this.np_videoCallEnabled || this.peerConnectionParameters.loopback) {
            this.np_sdpMediaConstraints.mandatory.add(new KeyValuePair(str5, str2));
        } else {
            this.np_sdpMediaConstraints.mandatory.add(new KeyValuePair(str5, str3));
        }
    }

    /* access modifiers changed from: private */
    public void createPeerConnectionInternal(EglBase.Context renderEGLContext) {
        PeerConnectionFactory peerConnectionFactory = this.np_factory;
        String str = TAG;
        if (peerConnectionFactory == null || this.isError) {
            Log.e(str, "Peerconnection np_factory is not created");
            return;
        }
        Log.d(str, "Create peer connection.");
        StringBuilder sb = new StringBuilder();
        sb.append("np_pcConstraints: ");
        sb.append(this.np_pcConstraints.toString());
        Log.d(str, sb.toString());
        this.np_queuedRemoteCandidates = new LinkedList<>();
        if (this.np_videoCallEnabled) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("EGLContext: ");
            sb2.append(renderEGLContext);
            Log.d(str, sb2.toString());
            this.np_factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
        }
        RTCConfiguration rtcConfig = new RTCConfiguration(this.signalingParameters.np_iceServers);
        rtcConfig.tcpCandidatePolicy = TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.keyType = KeyType.ECDSA;
        this.peerConnection = this.np_factory.createPeerConnection(rtcConfig, this.np_pcConstraints, (Observer) this.pcObserver);
        if (this.np_dataChannelEnabled) {
            Init init = new Init();
            init.ordered = this.peerConnectionParameters.dataChannelParameters.ordered;
            init.negotiated = this.peerConnectionParameters.dataChannelParameters.negotiated;
            init.maxRetransmits = this.peerConnectionParameters.dataChannelParameters.maxRetransmits;
            init.maxRetransmitTimeMs = this.peerConnectionParameters.dataChannelParameters.maxRetransmitTimeMs;
            init.id = this.peerConnectionParameters.dataChannelParameters.id;
            init.protocol = this.peerConnectionParameters.dataChannelParameters.protocol;
            this.dataChannel = this.peerConnection.createDataChannel("ApprtcDemo data", init);
        }
        this.np_isInitiator = false;
        Logging.enableTracing("logcat:", EnumSet.of(TraceLevel.TRACE_DEFAULT));
        Logging.enableLogToDebugOutput(Severity.LS_INFO);
        MediaStream createLocalMediaStream = this.np_factory.createLocalMediaStream("ARDAMS");
        this.mediaStream = createLocalMediaStream;
        if (this.np_videoCallEnabled) {
            createLocalMediaStream.addTrack(createVideoTrack(this.videoCapturer));
        }
        this.mediaStream.addTrack(createAudioTrack());
        this.peerConnection.addStream(this.mediaStream);
        if (this.np_videoCallEnabled) {
            findVideoSender();
        }
        if (this.peerConnectionParameters.aecDump) {
            try {
                StringBuilder sb3 = new StringBuilder();
                sb3.append(Environment.getExternalStorageDirectory().getPath());
                sb3.append(File.separator);
                sb3.append("Download/audio.aecdump");
                ParcelFileDescriptor open = ParcelFileDescriptor.open(new File(sb3.toString()), 1006632960);
                this.np_aecDumpFileDescriptor = open;
                this.np_factory.startAecDump(open.getFd(), -1);
            } catch (IOException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void closeInternal() {
        if (this.np_factory != null && this.peerConnectionParameters.aecDump) {
            this.np_factory.stopAecDump();
        }
        this.np_statsTimer.cancel();
        DataChannel dataChannel2 = this.dataChannel;
        if (dataChannel2 != null) {
            dataChannel2.dispose();
            this.dataChannel = null;
        }
        PeerConnection peerConnection2 = this.peerConnection;
        if (peerConnection2 != null) {
            peerConnection2.dispose();
            this.peerConnection = null;
        }
        AudioSource audioSource2 = this.audioSource;
        if (audioSource2 != null) {
            audioSource2.dispose();
            this.audioSource = null;
        }
        VideoCapturer videoCapturer2 = this.videoCapturer;
        if (videoCapturer2 != null) {
            try {
                videoCapturer2.stopCapture();
                this.np_videoCapturerStopped = true;
                this.videoCapturer.dispose();
                this.videoCapturer = null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        VideoSource videoSource2 = this.videoSource;
        if (videoSource2 != null) {
            videoSource2.dispose();
            this.videoSource = null;
        }
        this.np_localRender = null;
        this.np_remoteRenders = null;
        PeerConnectionFactory peerConnectionFactory = this.np_factory;
        if (peerConnectionFactory != null) {
            peerConnectionFactory.dispose();
            this.np_factory = null;
        }
        this.options = null;
        this.np_events.onPeerConnectionClosed();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        this.np_events = null;
    }

    public boolean isHDVideo() {
        boolean z = false;
        if (!this.np_videoCallEnabled) {
            return false;
        }
        if (this.np_videoWidth * this.np_videoHeight >= 921600) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void getStats() {
        PeerConnection peerConnection2 = this.peerConnection;
        if (peerConnection2 != null && !this.isError && !peerConnection2.getStats(new StatsObserver() {
            public void onComplete(StatsReport[] reports) {
                SSGPeerConnectionClient.this.np_events.onPeerConnectionStatsReady(reports);
            }
        }, null)) {
            Log.e(TAG, "getStats() returns false!");
        }
    }

    public void enableStatsEvents(boolean enable, int periodMs) {
        if (enable) {
            try {
                this.np_statsTimer.schedule(new TimerTask() {
                    public void run() {
                        SSGPeerConnectionClient.this.executor.execute(new Runnable() {
                            public void run() {
                                SSGPeerConnectionClient.this.getStats();
                            }
                        });
                    }
                }, 0, (long) periodMs);
            } catch (Exception e) {
                Log.e(TAG, "Can not schedule statistics timer", e);
            }
        } else {
            this.np_statsTimer.cancel();
        }
    }

    public void setAudioEnabled(final boolean enable) {
        this.executor.execute(new Runnable() {
            public void run() {
                SSGPeerConnectionClient.this.np_enableAudio = enable;
                if (SSGPeerConnectionClient.this.np_localAudioTrack != null) {
                    SSGPeerConnectionClient.this.np_localAudioTrack.setEnabled(SSGPeerConnectionClient.this.np_enableAudio);
                }
            }
        });
    }

    public void setVideoEnabled(final boolean enable) {
        this.executor.execute(new Runnable() {
            public void run() {
                SSGPeerConnectionClient.this.np_renderVideo = enable;
                if (SSGPeerConnectionClient.this.np_localVideoTrack != null) {
                    SSGPeerConnectionClient.this.np_localVideoTrack.setEnabled(SSGPeerConnectionClient.this.np_renderVideo);
                }
                if (SSGPeerConnectionClient.this.np_remoteVideoTrack != null) {
                    SSGPeerConnectionClient.this.np_remoteVideoTrack.setEnabled(SSGPeerConnectionClient.this.np_renderVideo);
                }
            }
        });
    }

    public void createOffer() {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                    Log.d(SSGPeerConnectionClient.TAG, "PC Create OFFER");
                    SSGPeerConnectionClient.this.np_isInitiator = true;
                    SSGPeerConnectionClient.this.peerConnection.createOffer(SSGPeerConnectionClient.this.sdpObserver, SSGPeerConnectionClient.this.np_sdpMediaConstraints);
                }
            }
        });
    }

    public void createAnswer() {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                    Log.d(SSGPeerConnectionClient.TAG, "PC create ANSWER");
                    SSGPeerConnectionClient.this.np_isInitiator = false;
                    SSGPeerConnectionClient.this.peerConnection.createAnswer(SSGPeerConnectionClient.this.sdpObserver, SSGPeerConnectionClient.this.np_sdpMediaConstraints);
                }
            }
        });
    }

    public void addRemoteIceCandidate(final IceCandidate candidate) {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                    if (SSGPeerConnectionClient.this.np_queuedRemoteCandidates != null) {
                        SSGPeerConnectionClient.this.np_queuedRemoteCandidates.add(candidate);
                    } else {
                        SSGPeerConnectionClient.this.peerConnection.addIceCandidate(candidate);
                    }
                }
            }
        });
    }

    public void removeRemoteIceCandidates(final IceCandidate[] candidates) {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                    SSGPeerConnectionClient.this.drainCandidates();
                    SSGPeerConnectionClient.this.peerConnection.removeIceCandidates(candidates);
                }
            }
        });
    }

    public void setRemoteDescription(final SessionDescription sdp) {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.peerConnection != null && !SSGPeerConnectionClient.this.isError) {
                    String sdpDescription = sdp.description;
                    if (SSGPeerConnectionClient.this.np_preferIsac) {
                        sdpDescription = SSGPeerConnectionClient.preferCodec(sdpDescription, SSGPeerConnectionClient.AUDIO_CODEC_ISAC, true);
                    }
                    if (SSGPeerConnectionClient.this.np_videoCallEnabled) {
                        sdpDescription = SSGPeerConnectionClient.preferCodec(sdpDescription, SSGPeerConnectionClient.this.np_preferredVideoCodec, false);
                    }
                    if (SSGPeerConnectionClient.this.peerConnectionParameters.audioStartBitrate > 0) {
                        sdpDescription = SSGPeerConnectionClient.setStartBitrate(SSGPeerConnectionClient.AUDIO_CODEC_OPUS, false, sdpDescription, SSGPeerConnectionClient.this.peerConnectionParameters.audioStartBitrate);
                    }
                    SSGPeerConnectionClient.this.peerConnection.setRemoteDescription(SSGPeerConnectionClient.this.sdpObserver, new SessionDescription(sdp.type, sdpDescription));
                }
            }
        });
    }

    public void stopVideoSource() {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.videoCapturer != null && !SSGPeerConnectionClient.this.np_videoCapturerStopped) {
                    try {
                        SSGPeerConnectionClient.this.videoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                    }
                    SSGPeerConnectionClient.this.np_videoCapturerStopped = true;
                }
            }
        });
    }

    public void startVideoSource() {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.videoCapturer != null && SSGPeerConnectionClient.this.np_videoCapturerStopped) {
                    SSGPeerConnectionClient.this.videoCapturer.startCapture(SSGPeerConnectionClient.this.np_videoWidth, SSGPeerConnectionClient.this.np_videoHeight, SSGPeerConnectionClient.this.np_videoFps);
                    SSGPeerConnectionClient.this.np_videoCapturerStopped = false;
                }
            }
        });
    }

    public void setVideoMaxBitrate(final Integer maxBitrateKbps) {
        this.executor.execute(new Runnable() {
            public void run() {
                if (SSGPeerConnectionClient.this.peerConnection != null && SSGPeerConnectionClient.this.np_localVideoSender != null && !SSGPeerConnectionClient.this.isError && SSGPeerConnectionClient.this.np_localVideoSender != null) {
                    RtpParameters parameters = SSGPeerConnectionClient.this.np_localVideoSender.getParameters();
                    if (parameters.encodings.size() != 0) {
                        Iterator it = parameters.encodings.iterator();
                        while (it.hasNext()) {
                            Encoding encoding = (Encoding) it.next();
                            Integer num = maxBitrateKbps;
                            encoding.maxBitrateBps = num == null ? null : Integer.valueOf(num.intValue() * 1000);
                        }
                        SSGPeerConnectionClient.this.np_localVideoSender.setParameters(parameters);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void reportError(final String errorMessage) {
        this.executor.execute(new Runnable() {
            public void run() {
                if (!SSGPeerConnectionClient.this.isError) {
                    SSGPeerConnectionClient.this.np_events.onPeerConnectionError(errorMessage);
                    SSGPeerConnectionClient.this.isError = true;
                }
            }
        });
    }

    private AudioTrack createAudioTrack() {
        AudioSource createAudioSource = this.np_factory.createAudioSource(this.np_audioConstraints);
        this.audioSource = createAudioSource;
        AudioTrack createAudioTrack = this.np_factory.createAudioTrack("ARDAMSa0", createAudioSource);
        this.np_localAudioTrack = createAudioTrack;
        createAudioTrack.setEnabled(this.np_enableAudio);
        return this.np_localAudioTrack;
    }

    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        this.videoSource = this.np_factory.createVideoSource(capturer);
        capturer.startCapture(this.np_videoWidth, this.np_videoHeight, this.np_videoFps);
        VideoTrack createVideoTrack = this.np_factory.createVideoTrack("ARDAMSv0", this.videoSource);
        this.np_localVideoTrack = createVideoTrack;
        createVideoTrack.setEnabled(this.np_renderVideo);
        this.np_localVideoTrack.addRenderer(new VideoRenderer(this.np_localRender));
        return this.np_localVideoTrack;
    }

    private void findVideoSender() {
        for (RtpSender sender : this.peerConnection.getSenders()) {
            if (sender.track() != null && sender.track().kind().equals("video")) {
                Log.d(TAG, "Found video sender.");
                this.np_localVideoSender = sender;
            }
        }
    }

    /* access modifiers changed from: private */
    public static String setStartBitrate(String codec, boolean isVideoCodec, String sdpDescription, int bitrateKbps) {
        String bitrateSet;
        String str = sdpDescription;
        int i = bitrateKbps;
        String str2 = "\r\n";
        String[] lines = str.split(str2);
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        StringBuilder sb = new StringBuilder();
        sb.append("^a=rtpmap:(\\d+) ");
        sb.append(codec);
        sb.append("(/\\d+)+[\r]?$");
        Pattern codecPattern = Pattern.compile(sb.toString());
        int i2 = 0;
        while (true) {
            if (i2 >= lines.length) {
                break;
            }
            Matcher codecMatcher = codecPattern.matcher(lines[i2]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i2;
                break;
            }
            i2++;
        }
        if (codecRtpMap == null) {
            return str;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("^a=fmtp:");
        sb2.append(codecRtpMap);
        sb2.append(" \\w+=\\d+.*[\r]?$");
        Pattern codecPattern2 = Pattern.compile(sb2.toString());
        int i3 = 0;
        while (true) {
            if (i3 >= lines.length) {
                break;
            } else if (codecPattern2.matcher(lines[i3]).matches()) {
                if (isVideoCodec) {
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(lines[i3]);
                    sb3.append("; x-google-start-bitrate=");
                    sb3.append(i);
                    lines[i3] = sb3.toString();
                } else {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append(lines[i3]);
                    sb4.append("; maxaveragebitrate=");
                    sb4.append(i * 1000);
                    lines[i3] = sb4.toString();
                }
                sdpFormatUpdated = true;
            } else {
                i3++;
            }
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (int i4 = 0; i4 < lines.length; i4++) {
            newSdpDescription.append(lines[i4]);
            newSdpDescription.append(str2);
            if (!sdpFormatUpdated && i4 == rtpmapLineIndex) {
                String str3 = "=";
                String str4 = String.valueOf(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
                String str5 = "a=fmtp:";
                if (isVideoCodec) {
                    StringBuilder sb5 = new StringBuilder();
                    sb5.append(str5);
                    sb5.append(codecRtpMap);
                    sb5.append(str4);
                    sb5.append(VIDEO_CODEC_PARAM_START_BITRATE);
                    sb5.append(str3);
                    sb5.append(i);
                    bitrateSet = sb5.toString();
                } else {
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append(str5);
                    sb6.append(codecRtpMap);
                    sb6.append(str4);
                    sb6.append(AUDIO_CODEC_PARAM_BITRATE);
                    sb6.append(str3);
                    sb6.append(i * 1000);
                    bitrateSet = sb6.toString();
                }
                newSdpDescription.append(bitrateSet);
                newSdpDescription.append(str2);
            }
        }
        return newSdpDescription.toString();
    }

    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; i++) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static String joinString(Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder((CharSequence) iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter);
            buffer.append((CharSequence) iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    private static String movePayloadTypesToFront(List<String> preferredPayloadTypes, String mLine) {
        String str = String.valueOf(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        List<String> origLineParts = Arrays.asList(mLine.split(str));
        if (origLineParts.size() <= 3) {
            return null;
        }
        List<String> header = origLineParts.subList(0, 3);
        List<String> unpreferredPayloadTypes = new ArrayList<>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        List<String> newLineParts = new ArrayList<>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, str, false);
    }

    /* access modifiers changed from: private */
    public static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        String str = "\r\n";
        String[] lines = sdpDescription.split(str);
        int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        if (mLineIndex == -1) {
            return sdpDescription;
        }
        List<String> codecPayloadTypes = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("^a=rtpmap:(\\d+) ");
        sb.append(codec);
        sb.append("(/\\d+)+[\r]?$");
        Pattern codecPattern = Pattern.compile(sb.toString());
        for (String matcher : lines) {
            Matcher codecMatcher = codecPattern.matcher(matcher);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            return sdpDescription;
        }
        String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdpDescription;
        }
        lines[mLineIndex] = newMLine;
        return joinString(Arrays.asList(lines), str, true);
    }

    /* access modifiers changed from: private */
    public void drainCandidates() {
        LinkedList<IceCandidate> linkedList = this.np_queuedRemoteCandidates;
        if (linkedList != null) {
            Iterator it = linkedList.iterator();
            while (it.hasNext()) {
                this.peerConnection.addIceCandidate((IceCandidate) it.next());
            }
            this.np_queuedRemoteCandidates = null;
        }
    }

    /* access modifiers changed from: private */
    public void switchCameraInternal() {
        VideoCapturer videoCapturer2 = this.videoCapturer;
        if ((videoCapturer2 instanceof CameraVideoCapturer) && this.np_videoCallEnabled && !this.isError && videoCapturer2 != null) {
            ((CameraVideoCapturer) videoCapturer2).switchCamera(null);
        }
    }

    public void switchCamera() {
        this.executor.execute(new Runnable() {
            public void run() {
                SSGPeerConnectionClient.this.switchCameraInternal();
            }
        });
    }

    public void changeCaptureFormat(final int width, final int height, final int framerate) {
        this.executor.execute(new Runnable() {
            public void run() {
                SSGPeerConnectionClient.this.changeCaptureFormatInternal(width, height, framerate);
            }
        });
    }

    /* access modifiers changed from: private */
    public void changeCaptureFormatInternal(int width, int height, int framerate) {
        if (this.np_videoCallEnabled && !this.isError && this.videoCapturer != null) {
            this.videoSource.adaptOutputFormat(width, height, framerate);
        }
    }
}
