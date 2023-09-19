package com.example.datingapp.Original;

import com.example.datingapp.Original.SSGRTCClient.SignalingParameters;

import org.webrtc.PeerConnection.IceServer;

import java.util.LinkedList;
import java.util.Random;

public class SSGRoomParametersFetcher {
    private static final String TAG = "RoomRTCClient";
    private static final int TURN_HTTP_TIMEOUT_MS = 5000;
    private final RoomParametersFetcherEvents events;
    private SSGAsyncHttpURLConnection httpConnection;
    private final String roomMessage;
    private final String roomUrl;

    public interface RoomParametersFetcherEvents {
        void onSignalingParametersReady(SignalingParameters signalingParameters);
    }

    public SSGRoomParametersFetcher(String str, String str2, RoomParametersFetcherEvents roomParametersFetcherEvents) {
        this.roomUrl = str;
        this.roomMessage = str2;
        this.events = roomParametersFetcherEvents;
    }

    public void makeRequest() {
        LinkedList linkedList = new LinkedList();
        linkedList.add(new IceServer(SSGStaticVar.turn, SSGStaticVar.user, SSGStaticVar.pass));
        int nextInt = new Random().nextInt(37964929) + 111476;
        boolean z = SSGStaticVar.initiatorcheck;
        String str = SSGStaticVar.wsUrl;
        StringBuilder sb = new StringBuilder();
        sb.append("");
        sb.append(nextInt);
        SignalingParameters signalingParameters = new SignalingParameters(linkedList, z, sb.toString(), str, "http://abSkas7a7.google7a.com", null, null);
        this.events.onSignalingParametersReady(signalingParameters);
    }
}
