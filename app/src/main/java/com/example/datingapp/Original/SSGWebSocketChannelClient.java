package com.example.datingapp.Original;

import android.os.Handler;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import de.tavendo.autobahn.WebSocket.WebSocketConnectionObserver;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

public class SSGWebSocketChannelClient {
    private static final int CLOSE_TIMEOUT = 1000;
    private static final String TAG = "WSChannelRTCClient";
    public String clientID;
    public boolean closeEvent;
    public final Object closeEventLock = new Object();
    public final WebSocketChannelEvents events;
    public final Handler handler;
    private String postServerUrl;
    public String roomID;
    public WebSocketConnectionState state;
    private WebSocketConnection ws;
    private WebSocketObserver wsObserver;
    private final List<String> wsSendQueue = new ArrayList();
    public String wsServerUrl;

    /* renamed from: com.videocall.live.chat.online.NewCode.SSGWebSocketChannelClient$2 reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$videocall$live$chat$online$NewCode$SSGWebSocketChannelClient$WebSocketConnectionState;

        static {
            int[] iArr = new int[WebSocketConnectionState.values().length];
            $SwitchMap$com$videocall$live$chat$online$NewCode$SSGWebSocketChannelClient$WebSocketConnectionState = iArr;
            try {
                iArr[WebSocketConnectionState.NEW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$videocall$live$chat$online$NewCode$SSGWebSocketChannelClient$WebSocketConnectionState[WebSocketConnectionState.CONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$videocall$live$chat$online$NewCode$SSGWebSocketChannelClient$WebSocketConnectionState[WebSocketConnectionState.ERROR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$videocall$live$chat$online$NewCode$SSGWebSocketChannelClient$WebSocketConnectionState[WebSocketConnectionState.CLOSED.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$videocall$live$chat$online$NewCode$SSGWebSocketChannelClient$WebSocketConnectionState[WebSocketConnectionState.REGISTERED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public interface WebSocketChannelEvents {
        void onWebSocketClose();

        void onWebSocketError(String str);

        void onWebSocketMessage(String str);
    }

    public enum WebSocketConnectionState {
        NEW,
        CONNECTED,
        REGISTERED,
        CLOSED,
        ERROR
    }

    private class WebSocketObserver implements WebSocketConnectionObserver {
        public void onBinaryMessage(byte[] bArr) {
        }

        public void onRawTextMessage(byte[] bArr) {
        }

        private WebSocketObserver() {
        }

        public void onOpen() {
            SSGWebSocketChannelClient.this.handler.post(new Runnable() {
                public void run() {
                    SSGWebSocketChannelClient.this.state = WebSocketConnectionState.CONNECTED;
                    if (SSGWebSocketChannelClient.this.roomID != null && SSGWebSocketChannelClient.this.clientID != null) {
                        SSGWebSocketChannelClient.this.register(SSGWebSocketChannelClient.this.roomID, SSGWebSocketChannelClient.this.clientID);
                    }
                }
            });
        }

        public void onClose(WebSocketCloseNotification webSocketCloseNotification, String str) {
            synchronized (SSGWebSocketChannelClient.this.closeEventLock) {
                SSGWebSocketChannelClient.this.closeEvent = true;
                SSGWebSocketChannelClient.this.closeEventLock.notify();
            }
            SSGWebSocketChannelClient.this.handler.post(new Runnable() {
                public void run() {
                    if (SSGWebSocketChannelClient.this.state != WebSocketConnectionState.CLOSED) {
                        SSGWebSocketChannelClient.this.state = WebSocketConnectionState.CLOSED;
                        SSGWebSocketChannelClient.this.events.onWebSocketClose();
                    }
                }
            });
        }

        public void onTextMessage(final String str) {
            SSGWebSocketChannelClient.this.handler.post(new Runnable() {
                public void run() {
                    if (SSGWebSocketChannelClient.this.state == WebSocketConnectionState.CONNECTED || SSGWebSocketChannelClient.this.state == WebSocketConnectionState.REGISTERED) {
                        SSGWebSocketChannelClient.this.events.onWebSocketMessage(str);
                    }
                }
            });
        }
    }

    private void sendWSSMessage(String str, String str2) {
    }

    public void disconnect(boolean z) {
    }

    public SSGWebSocketChannelClient(Handler handler2, WebSocketChannelEvents webSocketChannelEvents) {
        this.handler = handler2;
        this.events = webSocketChannelEvents;
        this.roomID = null;
        this.clientID = null;
        this.state = WebSocketConnectionState.NEW;
    }

    public WebSocketConnectionState getState() {
        return this.state;
    }

    public void connect(String str, String str2) {
        checkIfCalledOnValidThread();
        if (this.state != WebSocketConnectionState.NEW) {
            Log.e(TAG, "WebSocket is already connected.");
            return;
        }
        this.wsServerUrl = SSGStaticVar.wsUrl;
        this.postServerUrl = SSGStaticVar.hosts;
        this.closeEvent = false;
        this.ws = new WebSocketConnection();
        this.wsObserver = new WebSocketObserver();
        try {
            this.ws.connect(new URI(SSGStaticVar.wsUrl), this.wsObserver);
        } catch (URISyntaxException e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("URI error: ");
            sb2.append(e.getMessage());
            reportError(sb2.toString());
        } catch (WebSocketException e2) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append("WebSocket connection error: ");
            sb3.append(e2.getMessage());
            reportError(sb3.toString());
        }
    }

    public void register(String str, String str2) {
        checkIfCalledOnValidThread();
        this.roomID = str;
        this.clientID = str2;
        if (this.state != WebSocketConnectionState.CONNECTED) {
            String str3 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("WebSocket register() in state ");
            sb.append(this.state);
            Log.w(str3, sb.toString());
            return;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("cmd", "register");
            jSONObject.put("roomid", str);
            jSONObject.put("clientid", str2);
            this.ws.sendTextMessage(jSONObject.toString());
            this.state = WebSocketConnectionState.REGISTERED;
            for (String send : this.wsSendQueue) {
                send(send);
            }
            this.wsSendQueue.clear();
        } catch (JSONException e) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append("WebSocket register JSON error: ");
            sb4.append(e.getMessage());
            reportError(sb4.toString());
        }
    }

    public void send(String str) {
        checkIfCalledOnValidThread();
        switch (AnonymousClass2.$SwitchMap$com$videocall$live$chat$online$NewCode$SSGWebSocketChannelClient$WebSocketConnectionState[this.state.ordinal()]) {
            case 1:
            case 2:
                this.wsSendQueue.add(str);
                return;
            case 3:
            case 4:
                return;
            case 5:
                JSONObject jSONObject = new JSONObject();
                try {
                    jSONObject.put("cmd", "send");
                    jSONObject.put(NotificationCompat.CATEGORY_MESSAGE, str);
                    this.ws.sendTextMessage(jSONObject.toString());
                    break;
                } catch (JSONException e) {
                    StringBuilder sb4 = new StringBuilder();
                    sb4.append("WebSocket send JSON error: ");
                    sb4.append(e.getMessage());
                    reportError(sb4.toString());
                    break;
                }
        }
    }

    public void post(String str) {
        checkIfCalledOnValidThread();
    }

    private void reportError(final String str) {
        Log.e(TAG, str);
        this.handler.post(new Runnable() {
            public void run() {
                if (SSGWebSocketChannelClient.this.state != WebSocketConnectionState.ERROR) {
                    SSGWebSocketChannelClient.this.state = WebSocketConnectionState.ERROR;
                    SSGWebSocketChannelClient.this.events.onWebSocketError(str);
                }
            }
        });
    }

    private void checkIfCalledOnValidThread() {
        if (Thread.currentThread() != this.handler.getLooper().getThread()) {
            throw new IllegalStateException("WebSocket method is not called on valid thread");
        }
    }
}
