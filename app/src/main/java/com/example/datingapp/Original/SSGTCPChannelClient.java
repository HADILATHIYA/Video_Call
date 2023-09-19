package com.example.datingapp.Original;

import android.util.Log;

import org.webrtc.ThreadUtils.ThreadChecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;

public class SSGTCPChannelClient {
    private static final String TAG = "RIVCTCPChannelClient";
    public final TCPChannelEvents eventListener;
    public final ExecutorService executor;
    private final ThreadChecker executorThreadCheck;
    private TCPSocket socket;

    public interface TCPChannelEvents {
        void onTCPClose();

        void onTCPConnected(boolean z);

        void onTCPError(String str);

        void onTCPMessage(String str);
    }

    private abstract class TCPSocket extends Thread {
        private PrintWriter out;
        private Socket rawSocket;
        protected final Object rawSocketLock = new Object();

        public abstract Socket connect();

        public abstract boolean isServer();

        TCPSocket() {
        }

        public void run() {
            Socket connect = connect();
            synchronized (this.rawSocketLock) {
                if (this.rawSocket != null) {
                    Log.e(SSGTCPChannelClient.TAG, "Socket already existed and will be replaced.");
                }
                this.rawSocket = connect;
                if (connect != null) {
                    try {
                        this.out = new PrintWriter(new OutputStreamWriter(this.rawSocket.getOutputStream(), Charset.forName("UTF-8")), true);
                        new BufferedReader(new InputStreamReader(this.rawSocket.getInputStream(), Charset.forName("UTF-8")));
                    } catch (IOException e) {
                        SSGTCPChannelClient tCPChannelClient = SSGTCPChannelClient.this;
                        StringBuilder sb = new StringBuilder();
                        sb.append("Failed to open IO on rawSocket: ");
                        sb.append(e.getMessage());
                        tCPChannelClient.reportError(sb.toString());
                    }
                }
            }
        }

        public void disconnect() {
            try {
                synchronized (this.rawSocketLock) {
                    Socket socket = this.rawSocket;
                    if (socket != null) {
                        socket.close();
                        this.rawSocket = null;
                        this.out = null;
                        SSGTCPChannelClient.this.executor.execute(new Runnable() {
                            public void run() {
                                SSGTCPChannelClient.this.eventListener.onTCPClose();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                SSGTCPChannelClient tCPChannelClient = SSGTCPChannelClient.this;
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to close rawSocket: ");
                sb.append(e.getMessage());
                tCPChannelClient.reportError(sb.toString());
            }
        }

        public void send(String str) {
            String str2 = SSGTCPChannelClient.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Send: ");
            sb.append(str);
            Log.v(str2, sb.toString());
            synchronized (this.rawSocketLock) {
                PrintWriter printWriter = this.out;
                if (printWriter == null) {
                    SSGTCPChannelClient.this.reportError("Sending data on closed socket.");
                    return;
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str);
                sb2.append("\n");
                printWriter.write(sb2.toString());
                this.out.flush();
            }
        }
    }

    private class TCPSocketClient extends TCPSocket {
        private final InetAddress address;
        private final int port;

        public boolean isServer() {
            return false;
        }

        public TCPSocketClient(InetAddress inetAddress, int i) {
            super();
            this.address = inetAddress;
            this.port = i;
        }

        public Socket connect() {
            try {
                return new Socket(this.address, this.port);
            } catch (IOException e) {
                SSGTCPChannelClient tCPChannelClient = SSGTCPChannelClient.this;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Failed to connect: ");
                sb2.append(e.getMessage());
                tCPChannelClient.reportError(sb2.toString());
                return null;
            }
        }
    }

    private class TCPSocketServer extends TCPSocket {
        private final InetAddress address;
        private final int port;
        private ServerSocket serverSocket;

        public boolean isServer() {
            return true;
        }

        public TCPSocketServer(InetAddress inetAddress, int i) {
            super();
            this.address = inetAddress;
            this.port = i;
        }

        public Socket connect() {
            try {
                ServerSocket serverSocket2 = new ServerSocket(this.port, 0, this.address);
                synchronized (this.rawSocketLock) {
                    if (this.serverSocket != null) {
                        Log.e(SSGTCPChannelClient.TAG, "Server rawSocket was already listening and new will be opened.");
                    }
                    this.serverSocket = serverSocket2;
                }
                try {
                    return serverSocket2.accept();
                } catch (IOException e) {
                    SSGTCPChannelClient tCPChannelClient = SSGTCPChannelClient.this;
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Failed to receive connection: ");
                    sb2.append(e.getMessage());
                    tCPChannelClient.reportError(sb2.toString());
                    return null;
                }
            } catch (IOException e2) {
                SSGTCPChannelClient tCPChannelClient2 = SSGTCPChannelClient.this;
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Failed to create server socket: ");
                sb3.append(e2.getMessage());
                tCPChannelClient2.reportError(sb3.toString());
                return null;
            }
        }

        public void disconnect() {
            try {
                synchronized (this.rawSocketLock) {
                    ServerSocket serverSocket2 = this.serverSocket;
                    if (serverSocket2 != null) {
                        serverSocket2.close();
                        this.serverSocket = null;
                    }
                }
            } catch (IOException e) {
                SSGTCPChannelClient tCPChannelClient = SSGTCPChannelClient.this;
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to close server socket: ");
                sb.append(e.getMessage());
                tCPChannelClient.reportError(sb.toString());
            }
            super.disconnect();
        }
    }

    public SSGTCPChannelClient(ExecutorService executorService, TCPChannelEvents tCPChannelEvents, String str, int i) {
        ThreadChecker threadChecker = new ThreadChecker();
        this.executorThreadCheck = threadChecker;
        this.executor = executorService;
        threadChecker.detachThread();
        this.eventListener = tCPChannelEvents;
        try {
            InetAddress byName = InetAddress.getByName(str);
            if (byName.isAnyLocalAddress()) {
                this.socket = new TCPSocketServer(byName, i);
            } else {
                this.socket = new TCPSocketClient(byName, i);
            }
            this.socket.start();
        } catch (UnknownHostException e) {
            reportError("Invalid IP address.");
        }
    }

    public void disconnect() {
        this.executorThreadCheck.checkIsOnValidThread();
        this.socket.disconnect();
    }

    public void send(String str) {
        this.executorThreadCheck.checkIsOnValidThread();
        this.socket.send(str);
    }

    public void reportError(final String str) {
        String str2 = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("TCP Error: ");
        sb.append(str);
        Log.e(str2, sb.toString());
        this.executor.execute(new Runnable() {
            public void run() {
                SSGTCPChannelClient.this.eventListener.onTCPError(str);
            }
        });
    }
}
