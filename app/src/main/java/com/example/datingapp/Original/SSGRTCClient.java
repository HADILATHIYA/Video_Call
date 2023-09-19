package com.example.datingapp.Original;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.SessionDescription;

import java.util.List;

public interface SSGRTCClient {

    public static class RoomConnectionParameters {
        public final boolean np_loopback;
        public final String np_roomId;
        public final String np_roomUrl;
        public final String np_urlParameters;

        public RoomConnectionParameters(String str, String str2, boolean z, String str3) {
            this.np_roomUrl = str;
            this.np_roomId = str2;
            this.np_loopback = z;
            this.np_urlParameters = str3;
        }

        public RoomConnectionParameters(String str, String str2, boolean z) {
            this(str, str2, z, null);
        }
    }

    public interface SignalingEvents {
        void onChannelClose();

        void onChannelError(String str);

        void onConnectedToRoom(SignalingParameters signalingParameters);

        void onRemoteDescription(SessionDescription sessionDescription);

        void onRemoteIceCandidate(IceCandidate iceCandidate);

        void onRemoteIceCandidatesRemoved(IceCandidate[] iceCandidateArr);
    }

    public static class SignalingParameters {
        public final String np_clientId;
        public final List<IceCandidate> np_iceCandidates;
        public final List<IceServer> np_iceServers;
        public final boolean np_initiator;
        public final SessionDescription np_offerSdp;
        public final String np_wssPostUrl;
        public final String np_wssUrl;

        public SignalingParameters(List<IceServer> list, boolean z, String str, String str2, String str3, SessionDescription sessionDescription, List<IceCandidate> list2) {
            this.np_iceServers = list;
            this.np_initiator = z;
            this.np_clientId = str;
            this.np_wssUrl = str2;
            this.np_wssPostUrl = str3;
            this.np_offerSdp = sessionDescription;
            this.np_iceCandidates = list2;
        }
    }

    void connectToRoom(RoomConnectionParameters roomConnectionParameters);

    void disconnectFromRoom();

    void sendAnswerSdp(SessionDescription sessionDescription);

    void sendLocalIceCandidate(IceCandidate iceCandidate);

    void sendLocalIceCandidateRemovals(IceCandidate[] iceCandidateArr);

    void sendOfferSdp(SessionDescription sessionDescription);
}
