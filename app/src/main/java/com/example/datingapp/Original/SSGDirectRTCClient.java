package com.example.datingapp.Original;

import android.util.Log;

import com.example.datingapp.Original.SSGTCPChannelClient.TCPChannelEvents;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSGDirectRTCClient implements com.example.datingapp.Original.SSGRTCClient, TCPChannelEvents {
    static final Pattern IP_PATTERN = Pattern.compile("(((\\d+\\.){3}\\d+)|\\[((([0-9a-fA-F]{1,4}:)*[0-9a-fA-F]{1,4})?::(([0-9a-fA-F]{1,4}:)*[0-9a-fA-F]{1,4})?)\\]|\\[(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4})\\]|((([0-9a-fA-F]{1,4}:)*[0-9a-fA-F]{1,4})?::(([0-9a-fA-F]{1,4}:)*[0-9a-fA-F]{1,4})?)|(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4})|localhost)(:(\\d+))?");
    private static final int DEFAULT_PORT = 8888;
    private static final String TAG = "RIVCDirectRTCClient";
    public final SignalingEvents np_events;
    private final ExecutorService np_executor = Executors.newSingleThreadExecutor();
    public ConnectionState np_roomState = ConnectionState.NEW;
    public com.example.datingapp.Original.SSGTCPChannelClient np_tcpClient;
    private RoomConnectionParameters np_connectionParameters;

    public SSGDirectRTCClient(SignalingEvents signalingEvents) {
        this.np_events = signalingEvents;
    }

    public static void jsonPut(JSONObject jSONObject, String str, Object obj) {
        try {
            jSONObject.put(str, obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject toJsonCandidate(IceCandidate iceCandidate) {
        JSONObject jSONObject = new JSONObject();
        jsonPut(jSONObject, "label", Integer.valueOf(iceCandidate.sdpMLineIndex));
        jsonPut(jSONObject, "id", iceCandidate.sdpMid);
        jsonPut(jSONObject, "candidate", iceCandidate.sdp);
        return jSONObject;
    }

    private static IceCandidate toJavaCandidate(JSONObject jSONObject) throws JSONException {
        return new IceCandidate(jSONObject.getString("id"), jSONObject.getInt("label"), jSONObject.getString("candidate"));
    }

    public void connectToRoom(RoomConnectionParameters roomConnectionParameters) {
        this.np_connectionParameters = roomConnectionParameters;
        if (roomConnectionParameters.np_loopback) {
            reportError("Loopback connections aren't supported by RIVCDirectRTCClient.");
        }
        this.np_executor.execute(new Runnable() {
            public void run() {
                SSGDirectRTCClient.this.connectToRoomInternal();
            }
        });
    }

    public void disconnectFromRoom() {
        this.np_executor.execute(new Runnable() {
            public void run() {
                SSGDirectRTCClient.this.disconnectFromRoomInternal();
            }
        });
    }

    public void connectToRoomInternal() {
        int i;
        this.np_roomState = ConnectionState.NEW;
        Matcher matcher = IP_PATTERN.matcher(this.np_connectionParameters.np_roomId);
        if (!matcher.matches()) {
            reportError("roomId must match IP_PATTERN for RIVCDirectRTCClient.");
            return;
        }
        String group = matcher.group(1);
        String group2 = matcher.group(matcher.groupCount());
        if (group2 != null) {
            try {
                i = Integer.parseInt(group2);
            } catch (NumberFormatException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid port number: ");
                sb.append(group2);
                reportError(sb.toString());
                return;
            }
        } else {
            i = DEFAULT_PORT;
        }
        this.np_tcpClient = new com.example.datingapp.Original.SSGTCPChannelClient(this.np_executor, this, group, i);
    }

    public void disconnectFromRoomInternal() {
        this.np_roomState = ConnectionState.CLOSED;
        com.example.datingapp.Original.SSGTCPChannelClient sSGTCPChannelClient = this.np_tcpClient;
        if (sSGTCPChannelClient != null) {
            sSGTCPChannelClient.disconnect();
            this.np_tcpClient = null;
        }
        this.np_executor.shutdown();
    }

    public void sendOfferSdp(final SessionDescription sessionDescription) {
        this.np_executor.execute(new Runnable() {
            public void run() {
                if (SSGDirectRTCClient.this.np_roomState != ConnectionState.CONNECTED) {
                    SSGDirectRTCClient.this.reportError("Sending offer SDP in non connected state.");
                    return;
                }
                JSONObject jSONObject = new JSONObject();
                SSGDirectRTCClient.jsonPut(jSONObject, "sdp", sessionDescription.description);
                SSGDirectRTCClient.jsonPut(jSONObject, "type", "offer");
                SSGDirectRTCClient.this.sendMessage(jSONObject.toString());
            }
        });
    }

    public void sendAnswerSdp(final SessionDescription sessionDescription) {
        this.np_executor.execute(new Runnable() {
            public void run() {
                JSONObject jSONObject = new JSONObject();
                SSGDirectRTCClient.jsonPut(jSONObject, "sdp", sessionDescription.description);
                SSGDirectRTCClient.jsonPut(jSONObject, "type", "answer");
                SSGDirectRTCClient.this.sendMessage(jSONObject.toString());
            }
        });
    }

    public void sendLocalIceCandidate(final IceCandidate iceCandidate) {
        this.np_executor.execute(new Runnable() {
            public void run() {
                JSONObject jSONObject = new JSONObject();
                String str = "candidate";
                SSGDirectRTCClient.jsonPut(jSONObject, "type", str);
                SSGDirectRTCClient.jsonPut(jSONObject, "label", Integer.valueOf(iceCandidate.sdpMLineIndex));
                SSGDirectRTCClient.jsonPut(jSONObject, "id", iceCandidate.sdpMid);
                SSGDirectRTCClient.jsonPut(jSONObject, str, iceCandidate.sdp);
                if (SSGDirectRTCClient.this.np_roomState != ConnectionState.CONNECTED) {
                    SSGDirectRTCClient.this.reportError("Sending ICE candidate in non connected state.");
                } else {
                    SSGDirectRTCClient.this.sendMessage(jSONObject.toString());
                }
            }
        });
    }

    public void sendLocalIceCandidateRemovals(final IceCandidate[] iceCandidateArr) {
        this.np_executor.execute(new Runnable() {
            public void run() {
                JSONObject jSONObject = new JSONObject();
                SSGDirectRTCClient.jsonPut(jSONObject, "type", "remove-candidates");
                JSONArray jSONArray = new JSONArray();
                for (IceCandidate access$600 : iceCandidateArr) {
                    jSONArray.put(SSGDirectRTCClient.toJsonCandidate(access$600));
                }
                SSGDirectRTCClient.jsonPut(jSONObject, "candidates", jSONArray);
                if (SSGDirectRTCClient.this.np_roomState != ConnectionState.CONNECTED) {
                    SSGDirectRTCClient.this.reportError("Sending ICE candidate removals in non connected state.");
                } else {
                    SSGDirectRTCClient.this.sendMessage(jSONObject.toString());
                }
            }
        });
    }

    public void onTCPConnected(boolean z) {
        if (z) {
            this.np_roomState = ConnectionState.CONNECTED;
            SignalingParameters signalingParameters = new SignalingParameters(new ArrayList(), z, null, null, null, null, null);
            this.np_events.onConnectedToRoom(signalingParameters);
        }
    }

    public void onTCPMessage(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            String optString = jSONObject.optString("type");
            if (optString.equals("candidate")) {
                this.np_events.onRemoteIceCandidate(toJavaCandidate(jSONObject));
            } else if (optString.equals("remove-candidates")) {
                JSONArray jSONArray = jSONObject.getJSONArray("candidates");
                IceCandidate[] iceCandidateArr = new IceCandidate[jSONArray.length()];
                for (int i = 0; i < jSONArray.length(); i++) {
                    iceCandidateArr[i] = toJavaCandidate(jSONArray.getJSONObject(i));
                }
                this.np_events.onRemoteIceCandidatesRemoved(iceCandidateArr);
            } else {
                String str2 = "sdp";
                if (optString.equals("answer")) {
                    this.np_events.onRemoteDescription(new SessionDescription(Type.fromCanonicalForm(optString), jSONObject.getString(str2)));
                } else if (optString.equals("offer")) {
                    SignalingParameters signalingParameters = new SignalingParameters(new ArrayList(), false, null, null, null, new SessionDescription(Type.fromCanonicalForm(optString), jSONObject.getString(str2)), null);
                    this.np_roomState = ConnectionState.CONNECTED;
                    this.np_events.onConnectedToRoom(signalingParameters);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Unexpected TCP message: ");
                    sb.append(str);
                    reportError(sb.toString());
                }
            }
        } catch (JSONException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("TCP message JSON parsing error: ");
            sb2.append(e.toString());
            reportError(sb2.toString());
        }
    }

    public void onTCPError(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("TCP connection error: ");
        sb.append(str);
        reportError(sb.toString());
    }

    public void onTCPClose() {
        this.np_events.onChannelClose();
    }

    public void reportError(final String str) {
        Log.e(TAG, str);
        this.np_executor.execute(new Runnable() {
            public void run() {
                if (SSGDirectRTCClient.this.np_roomState != ConnectionState.ERROR) {
                    SSGDirectRTCClient.this.np_roomState = ConnectionState.ERROR;
                    SSGDirectRTCClient.this.np_events.onChannelError(str);
                }
            }
        });
    }

    public void sendMessage(final String str) {
        this.np_executor.execute(new Runnable() {
            public void run() {
                SSGDirectRTCClient.this.np_tcpClient.send(str);
            }
        });
    }

    private enum ConnectionState {
        NEW,
        CONNECTED,
        CLOSED,
        ERROR
    }
}
