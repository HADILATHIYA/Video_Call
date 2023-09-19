package com.example.datingapp.Original;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.datingapp.R;

import org.webrtc.StatsReport;
import org.webrtc.StatsReport.Value;

import java.util.HashMap;
import java.util.Map;

public class SSGHudFragment extends Fragment {
    private volatile boolean isRunning;
    private SSGCpuMonitor np_cpuMonitor;
    public boolean np_displayHud;
    private TextView np_encoderStatView;
    public TextView np_hudViewBwe;
    private TextView np_hudViewConnection;
    private TextView np_hudViewVideoRecv;
    private TextView np_hudViewVideoSend;
    private ImageView np_toggleDebugButton;
    private boolean np_videoCallEnabled;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.abc_fragment_hud, viewGroup, false);
        this.np_encoderStatView = (TextView) inflate.findViewById(R.id.np_encoder_stat_call);
        this.np_hudViewBwe = (TextView) inflate.findViewById(R.id.np_hud_stat_bwe);
        this.np_hudViewConnection = (TextView) inflate.findViewById(R.id.np_hud_stat_connection);
        this.np_hudViewVideoSend = (TextView) inflate.findViewById(R.id.np_hud_stat_video_send);
        this.np_hudViewVideoRecv = (TextView) inflate.findViewById(R.id.np_hud_stat_video_recv);
        ImageView imageButton = (ImageView) inflate.findViewById(R.id.np_button_toggle_debug);
        this.np_toggleDebugButton = imageButton;
        imageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (SSGHudFragment.this.np_displayHud) {
                    SSGHudFragment sSGHudFragment = SSGHudFragment.this;
                    sSGHudFragment.hudViewsSetProperties(sSGHudFragment.np_hudViewBwe.getVisibility() == 0 ? 4 : 0);
                }
            }
        });
        return inflate;
    }

    public void onStart() {
        super.onStart();
        Bundle arguments = getArguments();
        int i = 0;
        if (arguments != null) {
            this.np_videoCallEnabled = arguments.getBoolean("org.appspot.apprtc.VIDEO_CALL", true);
            this.np_displayHud = arguments.getBoolean("org.appspot.apprtc.DISPLAY_HUD", false);
        }
        if (!this.np_displayHud) {
            i = 4;
        }
        this.np_encoderStatView.setVisibility(i);
        this.np_toggleDebugButton.setVisibility(i);
        hudViewsSetProperties(4);
        this.isRunning = true;
    }

    public void onStop() {
        this.isRunning = false;
        super.onStop();
    }

    public void setCpuMonitor(SSGCpuMonitor cpuMonitor2) {
        this.np_cpuMonitor = cpuMonitor2;
    }

    public void hudViewsSetProperties(int i) {
        this.np_hudViewBwe.setVisibility(i);
        this.np_hudViewConnection.setVisibility(i);
        this.np_hudViewVideoSend.setVisibility(i);
        this.np_hudViewVideoRecv.setVisibility(i);
        this.np_hudViewBwe.setTextSize(3, 5.0f);
        this.np_hudViewConnection.setTextSize(3, 5.0f);
        this.np_hudViewVideoSend.setTextSize(3, 5.0f);
        this.np_hudViewVideoRecv.setTextSize(3, 5.0f);
    }

    private Map<String, String> getReportMap(StatsReport statsReport) {
        Value[] valueArr;
        HashMap hashMap = new HashMap();
        for (Value value : statsReport.values) {
            hashMap.put(value.name, value.value);
        }
        return hashMap;
    }

    public void updateEncoderStatistics(StatsReport[] statsReportArr) {
        String str;
        StringBuilder sb;
        StringBuilder sb5;
        StringBuilder sb2;
        int i;
        String str2;
        int length;
        int i2;
        StatsReport[] statsReportArr2 = statsReportArr;
        if (this.isRunning && this.np_displayHud) {
            StringBuilder sb3 = new StringBuilder(128);
            StringBuilder sb22 = new StringBuilder();
            StringBuilder sb32 = new StringBuilder();
            StringBuilder sb4 = new StringBuilder();
            StringBuilder sb52 = new StringBuilder();
            int length2 = statsReportArr2.length;
            String str3 = null;
            String str22 = null;
            String str32 = null;
            int i22 = 0;
            while (true) {
                str = "\n";
                if (i22 >= length2) {
                    break;
                }
                StatsReport statsReport = statsReportArr2[i22];
                String str4 = "ssrc";
                StatsReport[] statsReportArr3 = statsReportArr2;
                String str5 = "=";
                String str33 = str32;
                String str34 = "goog";
                String str23 = str22;
                String str24 = "";
                if (!statsReport.type.equals(str4) || !statsReport.id.contains(str4)) {
                    sb2 = sb3;
                } else {
                    sb2 = sb3;
                    if (statsReport.id.contains("send")) {
                        Map reportMap2 = getReportMap(statsReport);
                        String str8 = (String) reportMap2.get("googTrackId");
                        if (str8 == null || !str8.contains("ARDAMSv0")) {
                        } else {
                            String str6 = (String) reportMap2.get("googFrameRateSent");
                            sb4.append(statsReport.id);
                            sb4.append(str);
                            Value[] valueArr6 = statsReport.values;
                            Map map = reportMap2;
                            int length5 = valueArr6.length;
                            String str7 = str6;
                            int i6 = 0;
                            while (i6 < length5) {
                                int length52 = length5;
                                Value value4 = valueArr6[i6];
                                int i7 = length2;
                                Value[] valueArr = valueArr6;
                                int i3 = length2;
                                sb4.append(value4.name.replace(str34, str24));
                                sb4.append(str5);
                                sb4.append(value4.value);
                                sb4.append(str);
                                i6++;
                                length2 = i7;
                                length5 = length52;
                            }
                            int i4 = length2;
                            str3 = str7;
                        }
                        i = length2;
                        sb5 = sb52;
                        str32 = str33;
                        str22 = str23;
                        i22++;
                        length2 = i;
                        statsReportArr2 = statsReportArr;
                        sb3 = sb2;
                        sb52 = sb5;
                    }
                }
                int i5 = length2;
                if (!statsReport.type.equals(str4) || !statsReport.id.contains(str4)) {
                    i2 = i5;
                    length = length2;
                } else if (!statsReport.id.contains("recv")) {
                    i2 = i5;
                    length = length2;
                } else {
                    if (((String) getReportMap(statsReport).get("googFrameWidthReceived")) != null) {
                        sb52.append(statsReport.id);
                        sb52.append(str);
                        Value[] valueArr4 = statsReport.values;
                        int length4 = valueArr4.length;
                        i2 = i5;
                        int i52 = 0;
                        while (i52 < length4) {
                            int length3 = length2;
                            Value value3 = valueArr4[i52];
                            Value[] valueArr5 = valueArr4;
                            Value[] valueArr2 = valueArr4;
                            sb52.append(value3.name.replace(str34, str24));
                            sb52.append(str5);
                            sb52.append(value3.value);
                            sb52.append(str);
                            i52++;
                            valueArr4 = valueArr5;
                            length2 = length3;
                        }
                        length = length2;
                        Value[] valueArr3 = valueArr4;
                        sb5 = sb52;
                        str2 = str3;
                    } else {
                        i2 = i5;
                        length = length2;
                        sb5 = sb52;
                        str2 = str3;
                    }
                    str32 = str33;
                    str22 = str23;
                    i = i2;
                    int i8 = length;
                    str3 = str2;
                    i22++;
                    length2 = i;
                    statsReportArr2 = statsReportArr;
                    sb3 = sb2;
                    sb52 = sb5;
                }
                if (statsReport.id.equals("bweforvideo")) {
                    Map reportMap = getReportMap(statsReport);
                    String str42 = (String) reportMap.get("googTargetEncBitrate");
                    String str52 = (String) reportMap.get("googActualEncBitrate");
                    sb22.append(statsReport.id);
                    sb22.append(str);
                    Value[] valueArr7 = statsReport.values;
                    Map map2 = reportMap;
                    int length22 = valueArr7.length;
                    String str9 = str3;
                    int i32 = 0;
                    while (i32 < length22) {
                        int length23 = length22;
                        Value value = valueArr7[i32];
                        String str10 = str52;
                        Value[] valueArr8 = valueArr7;
                        StringBuilder sb53 = sb52;
                        sb22.append(value.name.replace(str34, str24).replace("Available", str24));
                        sb22.append(str5);
                        sb22.append(value.value);
                        sb22.append(str);
                        i32++;
                        length22 = length23;
                        valueArr7 = valueArr8;
                        sb52 = sb53;
                    }
                    sb5 = sb52;
                    Value[] valueArr9 = valueArr7;
                    str22 = str42;
                    str32 = str52;
                    i = i2;
                    int i9 = length;
                    str3 = str9;
                    i22++;
                    length2 = i;
                    statsReportArr2 = statsReportArr;
                    sb3 = sb2;
                    sb52 = sb5;
                } else {
                    sb5 = sb52;
                    str2 = str3;
                    if (statsReport.type.equals("googCandidatePair")) {
                        String str72 = (String) getReportMap(statsReport).get("googActiveConnection");
                        if (str72 == null || !str72.equals("true")) {
                        } else {
                            sb32.append(statsReport.id);
                            sb32.append(str);
                            Value[] valueArr22 = statsReport.values;
                            int length32 = valueArr22.length;
                            int i42 = 0;
                            while (i42 < length32) {
                                Value value2 = valueArr22[i42];
                                Value[] valueArr10 = valueArr22;
                                String str73 = str72;
                                sb32.append(value2.name.replace(str34, str24));
                                sb32.append(str5);
                                sb32.append(value2.value);
                                sb32.append(str);
                                i42++;
                                str72 = str73;
                            }
                        }
                    }
                    str32 = str33;
                    str22 = str23;
                    i = i2;
                    int i82 = length;
                    str3 = str2;
                    i22++;
                    length2 = i;
                    statsReportArr2 = statsReportArr;
                    sb3 = sb2;
                    sb52 = sb5;
                }
            }
            StatsReport[] statsReportArr22 = statsReportArr2;
            StringBuilder sb6 = sb3;
            StringBuilder sb54 = sb52;
            int i10 = length2;
            String str11 = str3;
            String str25 = str22;
            String str35 = str32;
            this.np_hudViewBwe.setText(sb22.toString());
            this.np_hudViewConnection.setText(sb32.toString());
            this.np_hudViewVideoSend.setText(sb4.toString());
            this.np_hudViewVideoRecv.setText(sb54.toString());
            if (this.np_videoCallEnabled) {
                if (str11 != null) {
                    sb = sb6;
                    sb.append("Fps:  ");
                    sb.append(str11);
                    sb.append(str);
                } else {
                    sb = sb6;
                    String str12 = str11;
                }
                if (str25 != null) {
                    sb.append("Target BR: ");
                    sb.append(str25);
                    sb.append(str);
                }
                if (str35 != null) {
                    sb.append("Actual BR: ");
                    sb.append(str35);
                    sb.append(str);
                }
            } else {
                String str13 = str25;
                sb = sb6;
                String str14 = str11;
            }
            if (this.np_cpuMonitor != null) {
                sb.append("CPU%: ");
                sb.append(this.np_cpuMonitor.getCpuUsageCurrent());
                sb.append("/");
                sb.append(this.np_cpuMonitor.getCpuUsageAverage());
                sb.append(". Freq: ");
                sb.append(this.np_cpuMonitor.getFrequencyScaleAverage());
            }
            this.np_encoderStatView.setText(sb.toString());
            StatsReport[] statsReportArr4 = statsReportArr22;
        }
    }
}
