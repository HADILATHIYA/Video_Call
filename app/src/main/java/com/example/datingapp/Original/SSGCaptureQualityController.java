package com.example.datingapp.Original;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.datingapp.Original.SSGCallFragment.OnCallEvents;
import com.example.datingapp.R;

import org.webrtc.CameraEnumerationAndroid.CaptureFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SSGCaptureQualityController implements OnSeekBarChangeListener {
    private static final int FRAMERATE_THRESHOLD = 15;
    private final List<CaptureFormat> formats = Arrays.asList(new CaptureFormat[]{new CaptureFormat(1280, 720, 0, 30000), new CaptureFormat(960, 540, 0, 30000), new CaptureFormat(640, 480, 0, 30000), new CaptureFormat(480, 360, 0, 30000), new CaptureFormat(320, 240, 0, 30000), new CaptureFormat(256, 144, 0, 30000)});
    private int framerate;
    private int height;
    private OnCallEvents np_callEvents;
    private TextView np_captureFormatText;
    private final Comparator<CaptureFormat> np_compareFormats = new Comparator<CaptureFormat>() {
        public int compare(CaptureFormat captureFormat, CaptureFormat captureFormat2) {
            SSGCaptureQualityController sSGCaptureQualityController = SSGCaptureQualityController.this;
            int access$100 = sSGCaptureQualityController.calculateFramerate(sSGCaptureQualityController.targetBandwidth, captureFormat);
            SSGCaptureQualityController sSGCaptureQualityController2 = SSGCaptureQualityController.this;
            int access$1002 = sSGCaptureQualityController2.calculateFramerate(sSGCaptureQualityController2.targetBandwidth, captureFormat2);
            return ((access$100 < 15 || access$1002 < 15) && access$100 != access$1002) ? access$100 - access$1002 : (captureFormat.width * captureFormat.height) - (captureFormat2.width * captureFormat2.height);
        }
    };
    public double targetBandwidth;
    private int width;

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public SSGCaptureQualityController(TextView textView, OnCallEvents onCallEvents) {
        this.np_captureFormatText = textView;
        this.np_callEvents = onCallEvents;
    }

    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        int i2 = i;
        if (i2 == 0) {
            this.width = 0;
            this.height = 0;
            this.framerate = 0;
            this.np_captureFormatText.setText(R.string.muted);
            return;
        }
        long j = Long.MIN_VALUE;
        for (CaptureFormat captureFormat : this.formats) {
            j = Math.max(j, ((long) captureFormat.width) * ((long) captureFormat.height) * ((long) captureFormat.framerate.max));
        }
        double d = (double) i2;
        Double.isNaN(d);
        Double.isNaN(d);
        double exp = (Math.exp((d / 100.0d) * 3.0d) - 1.0d) / (Math.exp(3.0d) - 1.0d);
        double d2 = (double) j;
        Double.isNaN(d2);
        Double.isNaN(d2);
        this.targetBandwidth = exp * d2;
        CaptureFormat captureFormat2 = (CaptureFormat) Collections.max(this.formats, this.np_compareFormats);
        this.width = captureFormat2.width;
        this.height = captureFormat2.height;
        this.framerate = calculateFramerate(this.targetBandwidth, captureFormat2);
        TextView textView = this.np_captureFormatText;
        textView.setText(String.format(textView.getContext().getString(R.string.format_description), new Object[]{Integer.valueOf(this.width), Integer.valueOf(this.height), Integer.valueOf(this.framerate)}));
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.np_callEvents.onCaptureFormatChange(this.width, this.height, this.framerate);
    }

    public int calculateFramerate(double d, CaptureFormat captureFormat) {
        int i = captureFormat.framerate.max;
        double d2 = (double) (captureFormat.width * captureFormat.height);
        Double.isNaN(d2);
        Double.isNaN(d2);
        double min = (double) Math.min(i, (int) Math.round(d / d2));
        Double.isNaN(min);
        Double.isNaN(min);
        return (int) Math.round(min / 1000.0d);
    }
}
