package com.example.datingapp.Original;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

public class SSGAsyncHttpURLConnection {
    private static final String HTTP_ORIGIN = "https://appr.tc";
    private static final int HTTP_TIMEOUT_MS = 8000;
    private String np_contentType;
    private final AsyncHttpEvents np_events;
    private final String np_message;
    private final String np_method;
    private final String url;

    public interface AsyncHttpEvents {
        void onHttpComplete(String str);

        void onHttpError(String str);
    }

    public SSGAsyncHttpURLConnection(String str, String str2, String str3, AsyncHttpEvents asyncHttpEvents) {
        this.np_method = str;
        this.url = str2;
        this.np_message = str3;
        this.np_events = asyncHttpEvents;
    }

    public void setContentType(String str) {
        this.np_contentType = str;
    }

    public void send() {
        new Thread(new Runnable() {
            public final void run() {
                SSGAsyncHttpURLConnection.this.sendHttpMessage();
            }
        }).start();
    }

    public void sendHttpMessage() {
        String str = " to ";
        String str2 = "HTTP ";
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.url).openConnection();
            boolean z = false;
            byte[] bArr = new byte[0];
            String str3 = this.np_message;
            if (str3 != null) {
                bArr = str3.getBytes("UTF-8");
            }
            httpURLConnection.setRequestMethod(this.np_method);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setConnectTimeout(HTTP_TIMEOUT_MS);
            httpURLConnection.setReadTimeout(HTTP_TIMEOUT_MS);
            httpURLConnection.addRequestProperty("origin", HTTP_ORIGIN);
            if (this.np_method.equals("POST")) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setFixedLengthStreamingMode(bArr.length);
                z = true;
            }
            String str4 = this.np_contentType;
            String str5 = "Content-Type";
            if (str4 == null) {
                httpURLConnection.setRequestProperty(str5, "text/plain; charset=utf-8");
            } else {
                httpURLConnection.setRequestProperty(str5, str4);
            }
            if (z && bArr.length > 0) {
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(bArr);
                outputStream.close();
            }
            if (httpURLConnection.getResponseCode() != 200) {
                AsyncHttpEvents asyncHttpEvents = this.np_events;
                StringBuilder sb = new StringBuilder();
                sb.append("Non-200 response to ");
                sb.append(this.np_method);
                sb.append(" to URL: ");
                sb.append(this.url);
                sb.append(" : ");
                sb.append(httpURLConnection.getHeaderField(null));
                asyncHttpEvents.onHttpError(sb.toString());
                httpURLConnection.disconnect();
                return;
            }
            InputStream inputStream = httpURLConnection.getInputStream();
            String drainStream = drainStream(inputStream);
            inputStream.close();
            httpURLConnection.disconnect();
            this.np_events.onHttpComplete(drainStream);
        } catch (SocketTimeoutException e) {
            AsyncHttpEvents asyncHttpEvents2 = this.np_events;
            StringBuilder sb2 = new StringBuilder();
            sb2.append(str2);
            sb2.append(this.np_method);
            sb2.append(str);
            sb2.append(this.url);
            sb2.append(" timeout");
            asyncHttpEvents2.onHttpError(sb2.toString());
        } catch (IOException e2) {
            AsyncHttpEvents asyncHttpEvents3 = this.np_events;
            StringBuilder sb3 = new StringBuilder();
            sb3.append(str2);
            sb3.append(this.np_method);
            sb3.append(str);
            sb3.append(this.url);
            sb3.append(" error: ");
            sb3.append(e2.getMessage());
            asyncHttpEvents3.onHttpError(sb3.toString());
        }
    }

    private static String drainStream(InputStream inputStream) {
        Scanner useDelimiter = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
        return useDelimiter.hasNext() ? useDelimiter.next() : "";
    }
}
