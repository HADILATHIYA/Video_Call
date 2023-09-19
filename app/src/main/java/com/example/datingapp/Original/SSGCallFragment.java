package com.example.datingapp.Original;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.datingapp.R;

import org.webrtc.RendererCommon.ScalingType;

public class SSGCallFragment extends Fragment {
    public OnCallEvents np_callEvents;
    public Handler np_customHandler = new Handler();
    public Handler np_customHandlerstart = new Handler();
    public int np_cut = 0;
    public int np_secs = 0;
    public long np_startTime = 0;
    public long np_startTimestart = 0;
    public TextView np_status;
    public ImageView np_toggleMuteButton;
    public VideoView videoView;
    long np_timeInMilliseconds = 0;
    long np_timeInMillisecondstart = 0;
    long np_timeSwapBuff = 0;
    long np_timeSwapBuffstart = 0;
    long updatedTime = 0;
    public Runnable updateTimerThread = new Runnable() {
        public void run() {
            SSGCallFragment.this.np_timeInMilliseconds = SystemClock.uptimeMillis() - SSGCallFragment.this.np_startTime;
            SSGCallFragment sSGCallFragment = SSGCallFragment.this;
            sSGCallFragment.updatedTime = sSGCallFragment.np_timeSwapBuff + SSGCallFragment.this.np_timeInMilliseconds;
            int i = (int) (SSGCallFragment.this.updatedTime / 1000);
            int i2 = i / 60;
            SSGCallFragment.this.np_secs = i % 60;
            int i3 = SSGCallFragment.this.np_secs;
            SSGCallFragment sSGCallFragment2 = SSGCallFragment.this;
            sSGCallFragment2.np_cut = sSGCallFragment2.np_secs;
            try {
                if (com.example.datingapp.Original.SSGStaticVar.incall == 1) {
                    SSGCallFragment.this.np_status.setText("Call Disconnected");
                    SSGCallFragment.this.np_customHandler.removeCallbacks(SSGCallFragment.this.updateTimerThread);
                    SSGCallFragment.this.end();
                } else {
                    TextView access$100 = SSGCallFragment.this.np_status;
                    StringBuilder sb = new StringBuilder();
                    sb.append("");
                    sb.append(i2);
                    sb.append(":");
                    sb.append(String.format("%02d", Integer.valueOf(SSGCallFragment.this.np_secs)));
                    access$100.setText(sb.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                SSGCallFragment.this.np_customHandler.postDelayed(this, 0);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    };
    long updatedTimestart = 0;
    public Runnable updateTimerThreadstart = new Runnable() {
        public void run() {
            SSGCallFragment.this.np_timeInMillisecondstart = SystemClock.uptimeMillis() - SSGCallFragment.this.np_startTimestart;
            SSGCallFragment sSGCallFragment = SSGCallFragment.this;
            sSGCallFragment.updatedTimestart = sSGCallFragment.np_timeSwapBuffstart + SSGCallFragment.this.np_timeInMillisecondstart;
            int i = (int) (SSGCallFragment.this.updatedTimestart / 1000);
            int i2 = i / 60;
            SSGCallFragment.this.np_cut = i % 60;
            try {
                SSGCallFragment.this.np_customHandlerstart.postDelayed(this, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private ImageView np_cameraSwitchButton;
    private View np_controlView;
    private ImageView np_disconnectButton;
    private ImageView np_reportButton;
    private boolean videoCallEnabled = true;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        View inflate = layoutInflater.inflate(R.layout.abc_fragment_call, viewGroup, false);
        this.np_controlView = inflate;


        np_disconnectButton = np_controlView.findViewById(R.id.np_button_call_disconnect);
        np_reportButton = np_controlView.findViewById(R.id.np_button);
        np_cameraSwitchButton = np_controlView.findViewById(R.id.np_button_call_switch_camera);
        np_toggleMuteButton = np_controlView.findViewById(R.id.np_button_call_toggle_mic);
        TextView textView = np_controlView.findViewById(R.id.np_status);
        np_status = textView;
        textView.setText("Connecting...");
        np_startTimestart = SystemClock.uptimeMillis();
        np_customHandlerstart.postDelayed(updateTimerThreadstart, 0);

        VideoView videoView2 = np_controlView.findViewById(R.id.np_videoView);

        videoView = videoView2;
        videoView2.setVideoURI(Uri.parse(com.example.datingapp.Original.SSGStaticVar.mediaurl));

        videoView.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(false);
                if (com.example.datingapp.Original.SSGStaticVar.first) {
                    com.example.datingapp.Original.SSGStaticVar.first = false;
                    start();
                    videoView.start();
                    return;
                }
                np_status.setText("Connecting...");
                np_customHandler.removeCallbacks(updateTimerThread);
            }
        });

        this.videoView.setOnErrorListener((mediaPlayer, i, i2) -> true);

        this.videoView.setOnCompletionListener(mediaPlayer -> {
            videoView.stopPlayback();
            endvideo();
        });

//        this.np_reportButton.setOnClickListener(view -> ReportDialog());

        this.np_disconnectButton.setOnClickListener(view -> Exit_CallendDialog());

        this.np_cameraSwitchButton.setOnClickListener(view -> np_callEvents.onCameraSwitch());

        this.np_toggleMuteButton.setOnClickListener(view -> {

            if (np_callEvents.onToggleMic()) {

                np_toggleMuteButton.setImageResource(R.drawable.ic_mic);

            } else {
                np_toggleMuteButton.setImageResource(R.drawable.ic_mutemic);
            }

        });


        return this.np_controlView;
    }

    @SuppressLint("ResourceType")
//    public void ReportDialog() {
//        CalculatorDialog calculatorDialog = new CalculatorDialog(getActivity());
//        calculatorDialog.setCancelable(false);
//        calculatorDialog.show();
//    }

    public void Exit_CallendDialog() {
        np_customHandlerstart.removeCallbacks(updateTimerThreadstart);
        com.example.datingapp.Original.SSGStaticVar.incall = 0;
        this.np_status.setText("Disconnected");
        this.np_customHandler.removeCallbacks(this.updateTimerThread);
        this.np_callEvents.onCallHangUp();
        getActivity().finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        np_customHandlerstart.removeCallbacks(updateTimerThreadstart);
        end();
    }

    public void onStart() {
        super.onStart();
        Bundle arguments = getArguments();
        boolean z = false;
        if (arguments != null) {
            boolean z2 = arguments.getBoolean("org.appspot.apprtc.VIDEO_CALL", true);
            this.videoCallEnabled = z2;
            if (z2 && arguments.getBoolean("org.appsopt.apprtc.VIDEO_CAPTUREQUALITYSLIDER", false)) {
                z = true;
            }
        }
        if (!this.videoCallEnabled) {
            this.np_cameraSwitchButton.setVisibility(View.INVISIBLE);
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.np_callEvents = (OnCallEvents) activity;
    }

    public void updateEncoderStatistics(String str) {
        this.np_status.setText(str);
        if (str.equals("Connected")) {
            this.videoView.stopPlayback();
            this.videoView.setVisibility(View.GONE);
            start();
        }
        if (str.equals("Disconnected")) {
            end();
        }
    }

    public void start() {
        this.np_status.setText("Connected");
        this.np_startTime = SystemClock.uptimeMillis();
        this.np_customHandler.postDelayed(this.updateTimerThread, 0);
    }

    public void end() {
        com.example.datingapp.Original.SSGStaticVar.incall = 0;
        this.np_status.setText("Disconnected");
        this.np_customHandler.removeCallbacks(this.updateTimerThread);
        this.np_callEvents.onCallHangUp();
        getActivity().finish();
    }

    public void endvideo() {
        com.example.datingapp.Original.SSGStaticVar.incall = 0;
        this.np_status.setText("Disconnected");
        this.np_customHandler.removeCallbacks(this.updateTimerThread);
    }

    public void onResume() {
        super.onResume();
        if (getView() != null) {
            getView().setFocusableInTouchMode(true);
            getView().requestFocus();
            getView().setOnKeyListener((view, i, keyEvent) -> {
                if (keyEvent.getAction() != 1 || i != 4) {
                    return false;
                }
                Exit_CallendDialog();
                return true;
            });
        }
    }

    public interface OnCallEvents {
        void onCallHangUp();

        void onCameraSwitch();

        void onCaptureFormatChange(int i, int i2, int i3);

        boolean onToggleMic();

        void onVideoScalingSwitch(ScalingType scalingType);
    }
}
