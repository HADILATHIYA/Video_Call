package com.example.datingapp.Original;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.core.app.NotificationCompat;

import com.example.datingapp.Original.SSGAsyncHttpURLConnection.AsyncHttpEvents;
import com.example.datingapp.Original.SSGRoomParametersFetcher.RoomParametersFetcherEvents;
import com.example.datingapp.Original.SSGWebSocketChannelClient.WebSocketChannelEvents;
import com.example.datingapp.Original.SSGWebSocketChannelClient.WebSocketConnectionState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;

import java.util.Random;

public class SSGWebSocketRTCClient implements com.example.datingapp.Original.SSGRTCClient, WebSocketChannelEvents {
    private static final String ROOM_JOIN = "join";
    private static final String ROOM_LEAVE = "leave";
    private static final String ROOM_MESSAGE = "message";
    private static final String TAG = "WSRTCClient";
    public final Handler handler;
    public RoomConnectionParameters connectionParameters;
    public SignalingEvents events;
    public ConnectionState roomState = ConnectionState.NEW;
    public com.example.datingapp.Original.SSGWebSocketChannelClient wsClient;
    private String leaveUrl;
    private String messageUrl;
    private SignalingParameters sp;

    public SSGWebSocketRTCClient(SignalingEvents signalingEvents) {
        this.events = signalingEvents;
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
    }

    public static void jsonPut(JSONObject jSONObject, String str, Object obj) {
        try {
            jSONObject.put(str, obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void onWebSocketClose() {
    }

    public void connectToRoom(RoomConnectionParameters roomConnectionParameters) {
        this.connectionParameters = roomConnectionParameters;
        this.handler.post(new Runnable() {
            public void run() {
                SSGWebSocketRTCClient.this.connectToRoomInternal();
            }
        });
    }

    public void disconnectFromRoom() {
        this.handler.post(new Runnable() {
            public void run() {
                SSGWebSocketRTCClient.this.disconnectFromRoomInternal();
                SSGWebSocketRTCClient.this.handler.getLooper().quit();
            }
        });
    }

    public void connectToRoomInternal() {
        String connectionUrl = getConnectionUrl(this.connectionParameters);
        this.roomState = ConnectionState.NEW;
        this.wsClient = new com.example.datingapp.Original.SSGWebSocketChannelClient(this.handler, this);
        new com.example.datingapp.Original.SSGRoomParametersFetcher(connectionUrl, null, new RoomParametersFetcherEvents() {
            public void onSignalingParametersReady(final SignalingParameters signalingParameters) {
                SSGWebSocketRTCClient.this.handler.post(new Runnable() {
                    public void run() {
                        SSGWebSocketRTCClient.this.signalingParametersReady(signalingParameters);
                    }
                });
            }
        }).makeRequest();
    }

    public void disconnectFromRoomInternal() {
        if (this.roomState == ConnectionState.CONNECTED) {
            this.wsClient.send(null);
        }
        this.roomState = ConnectionState.CLOSED;
        com.example.datingapp.Original.SSGWebSocketChannelClient sSGWebSocketChannelClient = this.wsClient;
        if (sSGWebSocketChannelClient != null) {
            sSGWebSocketChannelClient.disconnect(true);
        }
    }

    private String getConnectionUrl(RoomConnectionParameters roomConnectionParameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(roomConnectionParameters.np_roomUrl);
        String str = "/";
        sb.append(str);
        sb.append(ROOM_JOIN);
        sb.append(str);
        sb.append(roomConnectionParameters.np_roomId);
        sb.append(getQueryString(roomConnectionParameters));
        return sb.toString();
    }

    private String getMessageUrl(RoomConnectionParameters roomConnectionParameters, SignalingParameters signalingParameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(roomConnectionParameters.np_roomUrl);
        String str = "/";
        sb.append(str);
        sb.append(ROOM_MESSAGE);
        sb.append(str);
        sb.append(roomConnectionParameters.np_roomId);
        sb.append(str);
        sb.append(signalingParameters.np_clientId);
        sb.append(getQueryString(roomConnectionParameters));
        return sb.toString();
    }

    private String getLeaveUrl(RoomConnectionParameters roomConnectionParameters, SignalingParameters signalingParameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(roomConnectionParameters.np_roomUrl);
        String str = "/";
        sb.append(str);
        sb.append(ROOM_LEAVE);
        sb.append(str);
        sb.append(roomConnectionParameters.np_roomId);
        sb.append(str);
        sb.append(signalingParameters.np_clientId);
        sb.append(getQueryString(roomConnectionParameters));
        return sb.toString();
    }

    private String getQueryString(RoomConnectionParameters roomConnectionParameters) {
        if (roomConnectionParameters.np_urlParameters == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        sb.append(roomConnectionParameters.np_urlParameters);
        return sb.toString();
    }

    public void signalingParametersReady(SignalingParameters signalingParameters) {
        if (!this.connectionParameters.np_loopback || (com.example.datingapp.Original.SSGStaticVar.initiatorcheck && signalingParameters.np_offerSdp == null)) {
            if (!this.connectionParameters.np_loopback && !com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
                SessionDescription sessionDescription = signalingParameters.np_offerSdp;
            }
            this.messageUrl = getMessageUrl(this.connectionParameters, signalingParameters);
            this.leaveUrl = getLeaveUrl(this.connectionParameters, signalingParameters);
            this.roomState = ConnectionState.CONNECTED;
            this.sp = signalingParameters;
            if (com.example.datingapp.Original.SSGStaticVar.serverme) {
                this.events.onConnectedToRoom(this.sp);
            }
            this.wsClient.connect(signalingParameters.np_wssUrl, signalingParameters.np_wssPostUrl);
            this.wsClient.register(this.connectionParameters.np_roomId, signalingParameters.np_clientId);
            return;
        }
        reportError("np_loopback room is busy.");
    }

    public void sendOfferSdp(final SessionDescription sessionDescription) {
        this.handler.post(new Runnable() {
            public void run() {
                if (SSGWebSocketRTCClient.this.roomState != ConnectionState.CONNECTED) {
                    SSGWebSocketRTCClient.this.reportError("Sending offer SDP in non connected state.");
                    return;
                }
                JSONObject jSONObject = new JSONObject();
                SSGWebSocketRTCClient.jsonPut(jSONObject, "sdp", sessionDescription.description);
                SSGWebSocketRTCClient.jsonPut(jSONObject, "type", "offer");
                SSGWebSocketRTCClient.this.wsClient.send(jSONObject.toString());
                if (SSGWebSocketRTCClient.this.connectionParameters.np_loopback) {
                    SSGWebSocketRTCClient.this.events.onRemoteDescription(new SessionDescription(Type.fromCanonicalForm("answer"), sessionDescription.description));
                }
            }
        });
    }

    public void sendAnswerSdp(final SessionDescription sessionDescription) {
        this.handler.post(new Runnable() {
            public void run() {
                if (!SSGWebSocketRTCClient.this.connectionParameters.np_loopback) {
                    JSONObject jSONObject = new JSONObject();
                    SSGWebSocketRTCClient.jsonPut(jSONObject, "sdp", sessionDescription.description);
                    SSGWebSocketRTCClient.jsonPut(jSONObject, "type", "answer");
                    SSGWebSocketRTCClient.this.wsClient.send(jSONObject.toString());
                }
            }
        });
    }

    public void sendLocalIceCandidate(final IceCandidate iceCandidate) {
        this.handler.post(new Runnable() {
            public void run() {
                JSONObject jSONObject = new JSONObject();
                String str = "candidate";
                SSGWebSocketRTCClient.jsonPut(jSONObject, "type", str);
                SSGWebSocketRTCClient.jsonPut(jSONObject, "label", Integer.valueOf(iceCandidate.sdpMLineIndex));
                SSGWebSocketRTCClient.jsonPut(jSONObject, "id", iceCandidate.sdpMid);
                SSGWebSocketRTCClient.jsonPut(jSONObject, str, iceCandidate.sdp);
                if (!com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
                    SSGWebSocketRTCClient.this.wsClient.send(jSONObject.toString());
                } else if (SSGWebSocketRTCClient.this.roomState != ConnectionState.CONNECTED) {
                    SSGWebSocketRTCClient.this.reportError("Sending ICE candidate in non connected state.");
                } else {
                    SSGWebSocketRTCClient.this.wsClient.send(jSONObject.toString());
                    if (SSGWebSocketRTCClient.this.connectionParameters.np_loopback) {
                        SSGWebSocketRTCClient.this.events.onRemoteIceCandidate(iceCandidate);
                    }
                }
            }
        });
    }

    public void sendLocalIceCandidateRemovals(final IceCandidate[] iceCandidateArr) {
        this.handler.post(new Runnable() {
            public void run() {
                JSONObject jSONObject = new JSONObject();
                SSGWebSocketRTCClient.jsonPut(jSONObject, "type", "remove-candidates");
                JSONArray jSONArray = new JSONArray();
                for (IceCandidate access$1000 : iceCandidateArr) {
                    jSONArray.put(SSGWebSocketRTCClient.this.toJsonCandidate(access$1000));
                }
                SSGWebSocketRTCClient.jsonPut(jSONObject, "candidates", jSONArray);
                if (!com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
                    SSGWebSocketRTCClient.this.wsClient.send(jSONObject.toString());
                } else if (SSGWebSocketRTCClient.this.roomState != ConnectionState.CONNECTED) {
                    SSGWebSocketRTCClient.this.reportError("Sending ICE candidate removals in non connected state.");
                } else {
                    SSGWebSocketRTCClient.this.wsClient.send(jSONObject.toString());
                    if (SSGWebSocketRTCClient.this.connectionParameters.np_loopback) {
                        SSGWebSocketRTCClient.this.events.onRemoteIceCandidatesRemoved(iceCandidateArr);
                    }
                }
            }
        });
    }

    public void onWebSocketMessage(String str) {
        String str2 = "type";
        String str3 = "bye";
        String str4 = "yes_talk_hit";
        if (this.wsClient.getState() == WebSocketConnectionState.REGISTERED) {
            try {
                JSONObject jSONObject = new JSONObject(str);
                String string = jSONObject.getString(NotificationCompat.CATEGORY_MESSAGE);
                String optString = jSONObject.optString("error");
                if (string.contains(str3)) {
                    this.events.onChannelClose();
                } else if (string.contains(str2)) {
                    String str5 = "Unexpected WebSocket message: ";
                    if (string.length() > 0) {
                        JSONObject jSONObject9 = new JSONObject(string);
                        String optString2 = jSONObject9.optString(str2);
                        if (optString2.equals("candidate")) {
                            this.events.onRemoteIceCandidate(toJavaCandidate(jSONObject9));
                        } else if (optString2.equals("remove-candidates")) {
                            JSONArray jSONArray = jSONObject9.getJSONArray("candidates");
                            IceCandidate[] iceCandidateArr = new IceCandidate[jSONArray.length()];
                            for (int i = 0; i < jSONArray.length(); i++) {
                                iceCandidateArr[i] = toJavaCandidate(jSONArray.getJSONObject(i));
                            }
                            this.events.onRemoteIceCandidatesRemoved(iceCandidateArr);
                        } else {
                            String str6 = "sdp";
                            if (optString2.equals("answer")) {
                                if (com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
                                    this.events.onRemoteDescription(new SessionDescription(Type.fromCanonicalForm(optString2), jSONObject9.getString(str6)));
                                } else {
                                    StringBuilder sb3 = new StringBuilder();
                                    sb3.append("Received answer for call initiator: ");
                                    sb3.append(str);
                                    reportError(sb3.toString());
                                }
                            } else if (optString2.equals("offer")) {
                                if (!com.example.datingapp.Original.SSGStaticVar.initiatorcheck) {
                                    this.events.onRemoteDescription(new SessionDescription(Type.fromCanonicalForm(optString2), jSONObject9.getString(str6)));
                                } else {
                                    StringBuilder sb4 = new StringBuilder();
                                    sb4.append("Received offer for call receiver: ");
                                    sb4.append(str);
                                    reportError(sb4.toString());
                                }
                            } else if (optString2.equals(str3)) {
                                this.events.onChannelClose();
                            } else {
                                StringBuilder sb5 = new StringBuilder();
                                sb5.append(str5);
                                sb5.append(str);
                                reportError(sb5.toString());
                            }
                        }
                        return;
                    }
                    if (optString != null) {
                        if (optString.length() > 0) {
                            StringBuilder sb7 = new StringBuilder();
                            sb7.append("WebSocket error message: ");
                            sb7.append(optString);
                            reportError(sb7.toString());
                            return;
                        }
                    }
                    StringBuilder sb6 = new StringBuilder();
                    sb6.append(str5);
                    sb6.append(str);
                    reportError(sb6.toString());
                } else if (!com.example.datingapp.Original.SSGStaticVar.serverme) {
                    String str7 = "true";
                    if (string.contains(str4)) {
                        JSONObject jSONObject2 = new JSONObject();
                        jsonPut(jSONObject2, str4, str7);
                        this.wsClient.send(jSONObject2.toString());
                    }
                    if (string.contains("no_talk_hit")) {
                        JSONObject jSONObject3 = new JSONObject();
                        jsonPut(jSONObject3, str4, str7);
                        this.wsClient.send(jSONObject3.toString());
                    }
                    if (string.contains("call_initiator") && !com.example.datingapp.Original.SSGStaticVar.serverme) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(com.example.datingapp.Original.SSGStaticVar.serverme);
                        sb.append("");
                        com.example.datingapp.Original.SSGStaticVar.initiatorcheck = true;
                        this.events.onConnectedToRoom(this.sp);
                    }
                    if (string.contains("call_receiver")) {
                        com.example.datingapp.Original.SSGStaticVar.initiatorcheck = false;
                        this.events.onConnectedToRoom(this.sp);
                        JSONObject jSONObject4 = new JSONObject();
                        jsonPut(jSONObject4, "delayed_disconnect", str7);
                        this.wsClient.send(jSONObject4.toString());
                        JSONObject jSONObject5 = new JSONObject();
                        jsonPut(jSONObject5, "ping", str7);
                        this.wsClient.send(jSONObject5.toString());
                        JSONObject jSONObject6 = new JSONObject();
                        jsonPut(jSONObject6, "unlimited_call", str7);
                        this.wsClient.send(jSONObject6.toString());
                        int nextInt = new Random().nextInt(10000) + 1;
                        JSONObject jSONObject7 = new JSONObject();
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("RoomID_");
                        sb2.append(Integer.toString(nextInt));
                        sb2.append("_RoomID");
                        jsonPut(jSONObject7, sb2.toString(), str7);
                        this.wsClient.send(jSONObject7.toString());
                        JSONObject jSONObject8 = new JSONObject();
                        jsonPut(jSONObject8, "verify_person", str7);
                        this.wsClient.send(jSONObject8.toString());
                    }
                }
            } catch (JSONException e) {
                StringBuilder sb8 = new StringBuilder();
                sb8.append("WebSocket message JSON parsing error: ");
                sb8.append(e.toString());
                reportError(sb8.toString());
            }
        }
    }

    public void onWebSocketError(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("WebSocket error: ");
        sb.append(str);
        reportError(sb.toString());
    }

    public void reportError(final String str) {
        this.handler.post(new Runnable() {
            public void run() {
                if (SSGWebSocketRTCClient.this.roomState != ConnectionState.ERROR) {
                    SSGWebSocketRTCClient.this.roomState = ConnectionState.ERROR;
                    SSGWebSocketRTCClient.this.events.onChannelError(str);
                }
            }
        });
    }

    private void sendPostMessage(final MessageType messageType, String str, String str2) {
        if (str2 != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(". Message: ");
            sb.append(str2);
            sb.toString();
        }
        new com.example.datingapp.Original.SSGAsyncHttpURLConnection("POST", str, str2, new AsyncHttpEvents() {
            public void onHttpError(String str) {
                SSGWebSocketRTCClient webSocketRTCClient = SSGWebSocketRTCClient.this;
                StringBuilder sb = new StringBuilder();
                sb.append("GAE POST error: ");
                sb.append(str);
                webSocketRTCClient.reportError(sb.toString());
            }

            public void onHttpComplete(String str) {
                if (messageType == MessageType.MESSAGE) {
                    try {
                        String string = new JSONObject(str).getString("result");
                        if (!string.equals("SUCCESS")) {
                            SSGWebSocketRTCClient webSocketRTCClient = SSGWebSocketRTCClient.this;
                            StringBuilder sb = new StringBuilder();
                            sb.append("GAE POST error: ");
                            sb.append(string);
                            webSocketRTCClient.reportError(sb.toString());
                        }
                    } catch (JSONException e) {
                        SSGWebSocketRTCClient webSocketRTCClient2 = SSGWebSocketRTCClient.this;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("GAE POST JSON error: ");
                        sb2.append(e.toString());
                        webSocketRTCClient2.reportError(sb2.toString());
                    }
                }
            }
        });
    }

    public JSONObject toJsonCandidate(IceCandidate iceCandidate) {
        JSONObject jSONObject = new JSONObject();
        jsonPut(jSONObject, "label", Integer.valueOf(iceCandidate.sdpMLineIndex));
        jsonPut(jSONObject, "id", iceCandidate.sdpMid);
        jsonPut(jSONObject, "candidate", iceCandidate.sdp);
        return jSONObject;
    }

    private IceCandidate toJavaCandidate(JSONObject jSONObject) throws JSONException {
        return new IceCandidate(jSONObject.getString("id"), jSONObject.getInt("label"), jSONObject.getString("candidate"));
    }

    private enum ConnectionState {
        NEW,
        CONNECTED,
        CLOSED,
        ERROR
    }

    private enum MessageType {
        MESSAGE,
        LEAVE
    }
}
