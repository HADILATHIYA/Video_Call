package com.example.datingapp.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.datingapp.Original.SSGCallActivity;
import com.example.datingapp.Original.model.SSGCall_interface;
import com.example.datingapp.Original.model.SSGCall_model;
import com.example.datingapp.Original.SSGRetrofitClient;
import com.example.datingapp.Original.SSGStaticVar;
import com.example.datingapp.R;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;

public class RandomConnectingActivity extends AppCompatActivity {

    private static final String TAG = "RIVCConnectActivity";
    private static boolean commandLineRun;
    public String np_phoneno = "";
    String str = "0";
    private String np_keyprefAudioBitrateType;
    private String np_keyprefAudioBitrateValue;
    private String np_keyprefFps;
    private String np_keyprefResolution;
    private String np_keyprefRoom;
    private String np_keyprefRoomList;
    private String np_keyprefRoomServerUrl;
    private String np_keyprefVideoBitrateType;
    private String np_keyprefVideoBitrateValue;
    private SharedPreferences sharedPref;
    
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        this.np_keyprefResolution = getString(R.string.pref_resolution_key);
        this.np_keyprefFps = getString(R.string.pref_fps_key);
        this.np_keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        this.np_keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        this.np_keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        this.np_keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        this.np_keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        this.np_keyprefRoom = getString(R.string.pref_room_key);
        this.np_keyprefRoomList = getString(R.string.pref_room_list_key);
        
        setContentView(R.layout.activity_random_connecting);

        getroom();
    }

    public void getroom() {
        if (Build.SUPPORTED_ABIS[0].equals("arm64-v8a") || Build.SUPPORTED_ABIS[0].equals("armeabi-v7a")) {
            Call post = ((SSGCall_interface) SSGRetrofitClient.getInstance(this).create(SSGCall_interface.class)).getPost(SSGStaticVar.iid, SSGStaticVar.deviceid, this.np_phoneno, SSGStaticVar.version);
            post.enqueue(new Callback<List<SSGCall_model>>() {
                public void onResponse(Call<List<SSGCall_model>> call, retrofit2.Response<List<SSGCall_model>> response) {
                    Log.e("onResponse", "onResponse");
                    String str = "id";
                    String json = new Gson().toJson(response.body());
                    if (json.length() > 5) {
                        try {
                            JSONArray jSONArray = new JSONArray(json);
                            for (int i = 0; i < jSONArray.length(); i++) {
                                String string = jSONArray.getJSONObject(i).getString(str);
                                RandomConnectingActivity.this.str = new String(RandomConnectingActivity.this.encrypt("##$$%*7887", new StringBuffer(new StringBuffer(new StringBuffer(string).reverse().toString().trim()).reverse().toString().trim()).reverse().toString()));
                                JSONObject jSONObject = new JSONObject(RandomConnectingActivity.this.str);
                                SSGStaticVar.iid = jSONObject.getString(str);
                                SSGStaticVar.hosts = jSONObject.getString("host");
                                SSGStaticVar.user = jSONObject.getString("user");
                                SSGStaticVar.pass = jSONObject.getString("pass");
                                SSGStaticVar.wsUrl = jSONObject.getString("wsUrl");
                                SSGStaticVar.turn = jSONObject.getString("turn");
                                SSGStaticVar.initiatorcheck = jSONObject.getBoolean("offer");
                                SSGStaticVar.serverme = jSONObject.getBoolean("serverme");
                                SSGStaticVar.block = jSONObject.getString("block").trim();
                                SSGStaticVar.updates = jSONObject.getString("update").trim();
                                SSGStaticVar.notice = jSONObject.getString("notice");
                                SSGStaticVar.msg = jSONObject.getString(NotificationCompat.CATEGORY_MESSAGE);
                                SSGStaticVar.mediaurl = jSONObject.getString("video");
                                Log.e("onResponse", "connectToRoom");
                                RandomConnectingActivity.this.connectToRoom(SSGStaticVar.iid, false, false, false, 0);
                                SSGStaticVar.first = true;
                            }
                        } catch (JSONException e) {
                            Toast.makeText(RandomConnectingActivity.this, "No User Found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                public void onFailure(Call<List<SSGCall_model>> call, Throwable th) {
                    Toast.makeText(RandomConnectingActivity.this, "No User Found", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(RandomConnectingActivity.this, "No User Found", Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 1 && commandLineRun) {
            setResult(i2);
            commandLineRun = false;
            finish();
        }
    }

    private String sharedPrefGetString(int i, String str2, int i2, boolean z) {
        String string = getString(i2);
        if (!z) {
            return this.sharedPref.getString(getString(i), string);
        }
        String stringExtra = getIntent().getStringExtra(str2);
        return stringExtra != null ? stringExtra : string;
    }

    private boolean sharedPrefGetBoolean(int i, String str2, int i2, boolean z) {
        boolean parseBoolean = Boolean.parseBoolean(getString(i2));
        if (z) {
            return getIntent().getBooleanExtra(str2, parseBoolean);
        }
        return this.sharedPref.getBoolean(getString(i), parseBoolean);
    }

    private int sharedPrefGetInteger(int i, String str2, int i2, boolean z) {
        String string = getString(i2);
        int parseInt = Integer.parseInt(string);
        if (z) {
            return getIntent().getIntExtra(str2, parseInt);
        }
        String string2 = getString(i);
        String string3 = this.sharedPref.getString(string2, string);
        try {
            return Integer.parseInt(string3);
        } catch (NumberFormatException e) {
            String str3 = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("Wrong setting for: ");
            sb.append(string2);
            sb.append(":");
            sb.append(string3);
            Log.e(str3, sb.toString());
            return parseInt;
        }
    }

    public void connectToRoom(String str, boolean z, boolean z2, boolean z3, int i) {
        int i2;
        boolean z4;
        int i3;
        String str2;
        String str3;
        int i4;
        int i5;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        boolean z5 = false;
        int i10 = 0;
        int i11;
        int i12 = 0;
        int i13 = 0;
        int i14 = 0;
        int i15 = 0;
        int i16 = 0;
        int i17 = 0;
        int i18 = 0;
        int i19 = 0;
        boolean z6 = z2;
        boolean z7 = z3;
        commandLineRun = z;
        String num = z6 ? Integer.toString(new Random().nextInt(100000000)) : str;
        String string = sharedPref.getString(this.np_keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));
        Log.e("RoomUrl", string);
        Log.e("RoomUrlID", num);
        String str4 = SSGCallActivity.EXTRA_VIDEO_CALL;
        boolean sharedPrefGetBoolean = sharedPrefGetBoolean(R.string.pref_videocall_key, str4, R.string.pref_videocall_default, z7);
        String str5 = SSGCallActivity.EXTRA_SCREENCAPTURE;
        boolean sharedPrefGetBoolean2 = sharedPrefGetBoolean(R.string.pref_screencapture_key, str5, R.string.pref_screencapture_default, z7);
        String str6 = SSGCallActivity.EXTRA_CAMERA2;
        boolean sharedPrefGetBoolean3 = sharedPrefGetBoolean(R.string.pref_camera2_key, str6, R.string.pref_camera2_default, z7);
        String str7 = SSGCallActivity.EXTRA_VIDEOCODEC;
        String sharedPrefGetString = sharedPrefGetString(R.string.pref_videocodec_key, str7, R.string.pref_videocodec_default, z7);
        String str8 = SSGCallActivity.EXTRA_AUDIOCODEC;
        String str9 = str8;
        String sharedPrefGetString2 = sharedPrefGetString(R.string.pref_audiocodec_key, str8, R.string.pref_audiocodec_default, z7);
        String str10 = SSGCallActivity.EXTRA_HWCODEC_ENABLED;
        boolean sharedPrefGetBoolean4 = sharedPrefGetBoolean(R.string.pref_hwcodec_key, str10, R.string.pref_hwcodec_default, z7);
        String str11 = str10;
        String str12 = SSGCallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED;
        boolean sharedPrefGetBoolean5 = sharedPrefGetBoolean(R.string.pref_capturetotexture_key, str12, R.string.pref_capturetotexture_default, z7);
        String str13 = str12;
        String str14 = SSGCallActivity.EXTRA_FLEXFEC_ENABLED;
        boolean sharedPrefGetBoolean6 = sharedPrefGetBoolean(R.string.pref_flexfec_key, str14, R.string.pref_flexfec_default, z7);
        String str15 = str14;
        String str16 = SSGCallActivity.EXTRA_NOAUDIOPROCESSING_ENABLED;
        boolean sharedPrefGetBoolean7 = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key, str16, R.string.pref_noaudioprocessing_default, z7);
        String str17 = str16;
        boolean sharedPrefGetBoolean8 = sharedPrefGetBoolean(R.string.pref_aecdump_key, SSGCallActivity.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, z7);
        boolean sharedPrefGetBoolean9 = sharedPrefGetBoolean(R.string.pref_opensles_key, SSGCallActivity.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, z7);
        boolean sharedPrefGetBoolean10 = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key, SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default, z7);
        boolean sharedPrefGetBoolean11 = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key, SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default, z7);
        boolean sharedPrefGetBoolean12 = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key, SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default, z7);
        boolean sharedPrefGetBoolean13 = sharedPrefGetBoolean(R.string.pref_enable_level_control_key, SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, R.string.pref_enable_level_control_key, z7);
        boolean sharedPrefGetBoolean14 = sharedPrefGetBoolean(R.string.pref_disable_webrtc_agc_and_hpf_key, SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, R.string.pref_disable_webrtc_agc_and_hpf_key, z7);
        if (z7) {
            z4 = sharedPrefGetBoolean14;
            i2 = getIntent().getIntExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, 0);
            i3 = getIntent().getIntExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, 0);
        } else {
            z4 = sharedPrefGetBoolean14;
            i3 = 0;
            i2 = 0;
        }
        String str18 = "";
        if (i2 == 0 && i3 == 0) {
            i19 = i3;
            str3 = sharedPrefGetString;

            String string2 = sharedPref.getString(this.np_keyprefResolution, getString(R.string.pref_resolution_default));
            String[] split = string2.split("[ x]+");
            str2 = str7;
            Log.d("jatin", String.valueOf(split.length));
            if (split.length == 2) {
                try {
                    i4 = Integer.parseInt(split[0]);
                    i5 = Integer.parseInt(split[1]);
                } catch (NumberFormatException unused) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Wrong video resolution setting: ");
                    sb.append(string2);
                    Log.e(str18, sb.toString());
                    i5 = 0;
                    i4 = 0;
                }
                if (!z7) {
                    i6 = i5;
                    Toast.makeText(this, "io", Toast.LENGTH_SHORT).show();
                    i7 = getIntent().getIntExtra(SSGCallActivity.EXTRA_VIDEO_FPS, 0);
                } else {
                    i6 = i5;
                    i7 = 0;
                    Toast.makeText(this, "ios", Toast.LENGTH_SHORT).show();
                }
                if (i7 != 0) {
                    i18 = i7;
                    String string3 = sharedPref.getString(this.np_keyprefFps, getString(R.string.pref_fps_default));
                    String[] split2 = string3.split("[ x]+");
                    i8 = i4;
                    if (split2.length == 2) {
                        try {
                            i9 = Integer.parseInt(split2[0]);
                        } catch (NumberFormatException unused2) {
                            StringBuilder sb2 = new StringBuilder();
                            sb2.append("Wrong camera fps setting: ");
                            sb2.append(string3);
                            Log.e(str18, sb2.toString());
                            i9 = 0;
                        }
                        boolean sharedPrefGetBoolean15 = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key, SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, R.string.pref_capturequalityslider_default, z7);
                        if (z7) {
                            z5 = sharedPrefGetBoolean15;
                            i10 = getIntent().getIntExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, 0);
                        } else {
                            z5 = sharedPrefGetBoolean15;
                            i10 = 0;
                        }
                        if (i10 == 0) {
                            String string4 = getString(R.string.pref_maxvideobitrate_default);
                            i17 = i10;
                            if (!sharedPref.getString(this.np_keyprefVideoBitrateType, string4).equals(string4)) {
                                i11 = Integer.parseInt(sharedPref.getString(this.np_keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default)));
                                if (!z7) {
                                    i12 = i11;
                                    i13 = getIntent().getIntExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, 0);
                                } else {
                                    i12 = i11;
                                    i13 = 0;
                                }
                                if (i13 != 0) {
                                    String string5 = getString(R.string.pref_startaudiobitrate_default);
                                    i16 = i13;
                                    if (!sharedPref.getString(this.np_keyprefAudioBitrateType, string5).equals(string5)) {
                                        i14 = Integer.parseInt(sharedPref.getString(this.np_keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default)));
                                        int i20 = i14;
                                        boolean sharedPrefGetBoolean16 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                                        boolean sharedPrefGetBoolean17 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                                        boolean sharedPrefGetBoolean18 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                                        boolean sharedPrefGetBoolean19 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                                        boolean sharedPrefGetBoolean20 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                                        int sharedPrefGetInteger = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                                        int sharedPrefGetInteger2 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                                        int sharedPrefGetInteger3 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                                        String sharedPrefGetString3 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                                        StringBuilder sb3 = new StringBuilder();
                                        sb3.append("Connecting to room ");
                                        sb3.append(num);
                                        sb3.append(" at URL ");
                                        sb3.append(string);
                                        Log.d(str18, sb3.toString());
                                        if (validateUrl(string)) {
                                            Uri parse = Uri.parse(string);
                                            Intent intent = new Intent(this, SSGCallActivity.class);
                                            intent.setData(parse);
                                            intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                                            intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                                            intent.putExtra(str4, sharedPrefGetBoolean);
                                            intent.putExtra(str5, sharedPrefGetBoolean2);
                                            intent.putExtra(str6, sharedPrefGetBoolean3);
                                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                                            intent.putExtra(str2, str3);
                                            intent.putExtra(str11, sharedPrefGetBoolean4);
                                            intent.putExtra(str13, sharedPrefGetBoolean5);
                                            intent.putExtra(str15, sharedPrefGetBoolean6);
                                            intent.putExtra(str17, sharedPrefGetBoolean7);
                                            intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                                            intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                                            intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                                            intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i20);
                                            intent.putExtra(str9, sharedPrefGetString2);
                                            intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean16);
                                            intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean17);
                                            intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                                            intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                                            boolean z8 = sharedPrefGetBoolean18;
                                            intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                                            if (z8) {
                                                intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean19);
                                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger);
                                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger2);
                                                intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString3);
                                                intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean20);
                                                intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger3);
                                            }
                                            if (z7) {
                                                Intent intent2 = getIntent();
                                                String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                                                if (intent2.hasExtra(str19)) {
                                                    intent.putExtra(str19, getIntent().getStringExtra(str19));
                                                }
                                                Intent intent3 = getIntent();
                                                String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                                                if (intent3.hasExtra(str20)) {
                                                    intent.putExtra(str20, getIntent().getStringExtra(str20));
                                                }
                                                Intent intent4 = getIntent();
                                                String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                                                if (intent4.hasExtra(str21)) {
                                                    i15 = 0;
                                                    intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                                                } else {
                                                    i15 = 0;
                                                }
                                                Intent intent5 = getIntent();
                                                String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                                                if (intent5.hasExtra(str22)) {
                                                    intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                                                }
                                            }
                                            startActivityForResult(intent, 1);
                                            finish();
                                            return;
                                        }
                                        return;
                                    }
                                } else {
                                    i16 = i13;
                                }
                                i14 = i16;
                                int i202 = i14;
                                boolean sharedPrefGetBoolean162 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                                boolean sharedPrefGetBoolean172 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                                boolean sharedPrefGetBoolean182 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                                boolean sharedPrefGetBoolean192 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                                boolean sharedPrefGetBoolean202 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                                int sharedPrefGetInteger4 = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                                int sharedPrefGetInteger22 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                                int sharedPrefGetInteger32 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                                String sharedPrefGetString32 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                                StringBuilder sb32 = new StringBuilder();
                                sb32.append("Connecting to room ");
                                sb32.append(num);
                                sb32.append(" at URL ");
                                sb32.append(string);
                                Log.d(str18, sb32.toString());
                                if (validateUrl(string)) {
                                    Uri parse = Uri.parse(string);
                                    Intent intent = new Intent(this, SSGCallActivity.class);
                                    intent.setData(parse);
                                    intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                                    intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                                    intent.putExtra(str4, sharedPrefGetBoolean);
                                    intent.putExtra(str5, sharedPrefGetBoolean2);
                                    intent.putExtra(str6, sharedPrefGetBoolean3);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                                    intent.putExtra(str2, str3);
                                    intent.putExtra(str11, sharedPrefGetBoolean4);
                                    intent.putExtra(str13, sharedPrefGetBoolean5);
                                    intent.putExtra(str15, sharedPrefGetBoolean6);
                                    intent.putExtra(str17, sharedPrefGetBoolean7);
                                    intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                                    intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                                    intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                                    intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i202);
                                    intent.putExtra(str9, sharedPrefGetString2);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean162);
                                    intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean172);
                                    intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                                    intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                                    boolean z8 = sharedPrefGetBoolean182;
                                    intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                                    if (z8) {
                                        intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean192);
                                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger4);
                                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger22);
                                        intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString32);
                                        intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean202);
                                        intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger32);
                                    }
                                    if (z7) {
                                        Intent intent2 = getIntent();
                                        String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                                        if (intent2.hasExtra(str19)) {
                                            intent.putExtra(str19, getIntent().getStringExtra(str19));
                                        }
                                        Intent intent3 = getIntent();
                                        String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                                        if (intent3.hasExtra(str20)) {
                                            intent.putExtra(str20, getIntent().getStringExtra(str20));
                                        }
                                        Intent intent4 = getIntent();
                                        String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                                        if (intent4.hasExtra(str21)) {
                                            i15 = 0;
                                            intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                                        } else {
                                            i15 = 0;
                                        }
                                        Intent intent5 = getIntent();
                                        String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                                        if (intent5.hasExtra(str22)) {
                                            intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                                        }
                                    }
                                    startActivityForResult(intent, 1);
                                    finish();
                                    return;
                                }
                            }
                        } else {
                            i17 = i10;
                        }
                        i11 = i17;
                        if (!z7) {
                        }
                        if (i13 != 0) {
                        }
                        i14 = i16;
                        int i2022 = i14;
                        boolean sharedPrefGetBoolean1622 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                        boolean sharedPrefGetBoolean1722 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                        boolean sharedPrefGetBoolean1822 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                        boolean sharedPrefGetBoolean1922 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                        boolean sharedPrefGetBoolean2022 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                        int sharedPrefGetInteger42 = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                        int sharedPrefGetInteger222 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                        int sharedPrefGetInteger322 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                        String sharedPrefGetString322 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                        StringBuilder sb322 = new StringBuilder();
                        sb322.append("Connecting to room ");
                        sb322.append(num);
                        sb322.append(" at URL ");
                        sb322.append(string);
                        Log.d(str18, sb322.toString());
                        if (validateUrl(string)) {
                            Uri parse = Uri.parse(string);
                            Intent intent = new Intent(this, SSGCallActivity.class);
                            intent.setData(parse);
                            intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                            intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                            intent.putExtra(str4, sharedPrefGetBoolean);
                            intent.putExtra(str5, sharedPrefGetBoolean2);
                            intent.putExtra(str6, sharedPrefGetBoolean3);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                            intent.putExtra(str2, str3);
                            intent.putExtra(str11, sharedPrefGetBoolean4);
                            intent.putExtra(str13, sharedPrefGetBoolean5);
                            intent.putExtra(str15, sharedPrefGetBoolean6);
                            intent.putExtra(str17, sharedPrefGetBoolean7);
                            intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                            intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                            intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                            intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i2022);
                            intent.putExtra(str9, sharedPrefGetString2);
                            intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean1622);
                            intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean1722);
                            intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                            intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                            boolean z8 = sharedPrefGetBoolean1822;
                            intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                            if (z8) {
                                intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean1922);
                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger42);
                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger222);
                                intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString322);
                                intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean2022);
                                intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger322);
                            }
                            if (z7) {
                                Intent intent2 = getIntent();
                                String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                                if (intent2.hasExtra(str19)) {
                                    intent.putExtra(str19, getIntent().getStringExtra(str19));
                                }
                                Intent intent3 = getIntent();
                                String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                                if (intent3.hasExtra(str20)) {
                                    intent.putExtra(str20, getIntent().getStringExtra(str20));
                                }
                                Intent intent4 = getIntent();
                                String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                                if (intent4.hasExtra(str21)) {
                                    i15 = 0;
                                    intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                                } else {
                                    i15 = 0;
                                }
                                Intent intent5 = getIntent();
                                String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                                if (intent5.hasExtra(str22)) {
                                    intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                                }
                            }
                            startActivityForResult(intent, 1);
                            finish();
                            return;
                        }
                    }
                } else {
                    i18 = i7;
                    i8 = i4;
                }
                i9 = i18;
                boolean sharedPrefGetBoolean152 = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key, SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, R.string.pref_capturequalityslider_default, z7);
                if (z7) {
                    z5 = sharedPrefGetBoolean152;
                    i10 = getIntent().getIntExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, 0);
                } else {
                    z5 = sharedPrefGetBoolean152;
                    i10 = 0;
                }
                boolean sharedPrefGetBoolean15 = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key, SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, R.string.pref_capturequalityslider_default, z7);
                if (z7) {
                    z5 = sharedPrefGetBoolean15;
                    i10 = getIntent().getIntExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, 0);
                } else {
                    z5 = sharedPrefGetBoolean15;
                    i10 = 0;
                }
                if (i10 == 0) {
                    String string4 = getString(R.string.pref_maxvideobitrate_default);
                    i17 = i10;
                    if (!sharedPref.getString(this.np_keyprefVideoBitrateType, string4).equals(string4)) {
                        i11 = Integer.parseInt(sharedPref.getString(this.np_keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default)));
                        if (!z7) {
                            i12 = i11;
                            i13 = getIntent().getIntExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, 0);
                        } else {
                            i12 = i11;
                            i13 = 0;
                        }
                        if (i13 != 0) {
                            String string5 = getString(R.string.pref_startaudiobitrate_default);
                            i16 = i13;
                            if (!sharedPref.getString(this.np_keyprefAudioBitrateType, string5).equals(string5)) {
                                i14 = Integer.parseInt(sharedPref.getString(this.np_keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default)));
                                int i20 = i14;
                                boolean sharedPrefGetBoolean16 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                                boolean sharedPrefGetBoolean17 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                                boolean sharedPrefGetBoolean18 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                                boolean sharedPrefGetBoolean19 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                                boolean sharedPrefGetBoolean20 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                                int sharedPrefGetInteger = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                                int sharedPrefGetInteger2 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                                int sharedPrefGetInteger3 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                                String sharedPrefGetString3 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                                StringBuilder sb3 = new StringBuilder();
                                sb3.append("Connecting to room ");
                                sb3.append(num);
                                sb3.append(" at URL ");
                                sb3.append(string);
                                Log.d(str18, sb3.toString());
                                if (validateUrl(string)) {
                                    Uri parse = Uri.parse(string);
                                    Intent intent = new Intent(this, SSGCallActivity.class);
                                    intent.setData(parse);
                                    intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                                    intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                                    intent.putExtra(str4, sharedPrefGetBoolean);
                                    intent.putExtra(str5, sharedPrefGetBoolean2);
                                    intent.putExtra(str6, sharedPrefGetBoolean3);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                                    intent.putExtra(str2, str3);
                                    intent.putExtra(str11, sharedPrefGetBoolean4);
                                    intent.putExtra(str13, sharedPrefGetBoolean5);
                                    intent.putExtra(str15, sharedPrefGetBoolean6);
                                    intent.putExtra(str17, sharedPrefGetBoolean7);
                                    intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                                    intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                                    intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                                    intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i20);
                                    intent.putExtra(str9, sharedPrefGetString2);
                                    intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean16);
                                    intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean17);
                                    intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                                    intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                                    boolean z8 = sharedPrefGetBoolean18;
                                    intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                                    if (z8) {
                                        intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean19);
                                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger);
                                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger2);
                                        intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString3);
                                        intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean20);
                                        intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger3);
                                    }
                                    if (z7) {
                                        Intent intent2 = getIntent();
                                        String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                                        if (intent2.hasExtra(str19)) {
                                            intent.putExtra(str19, getIntent().getStringExtra(str19));
                                        }
                                        Intent intent3 = getIntent();
                                        String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                                        if (intent3.hasExtra(str20)) {
                                            intent.putExtra(str20, getIntent().getStringExtra(str20));
                                        }
                                        Intent intent4 = getIntent();
                                        String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                                        if (intent4.hasExtra(str21)) {
                                            i15 = 0;
                                            intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                                        } else {
                                            i15 = 0;
                                        }
                                        Intent intent5 = getIntent();
                                        String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                                        if (intent5.hasExtra(str22)) {
                                            intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                                        }
                                    }
                                    startActivityForResult(intent, 1);
                                    finish();
                                    return;
                                }
                                return;
                            }
                        } else {
                            i16 = i13;
                        }
                        i14 = i16;
                        int i202 = i14;
                        boolean sharedPrefGetBoolean162 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                        boolean sharedPrefGetBoolean172 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                        boolean sharedPrefGetBoolean182 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                        boolean sharedPrefGetBoolean192 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                        boolean sharedPrefGetBoolean202 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                        int sharedPrefGetInteger4 = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                        int sharedPrefGetInteger22 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                        int sharedPrefGetInteger32 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                        String sharedPrefGetString32 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                        StringBuilder sb32 = new StringBuilder();
                        sb32.append("Connecting to room ");
                        sb32.append(num);
                        sb32.append(" at URL ");
                        sb32.append(string);
                        Log.d(str18, sb32.toString());
                        if (validateUrl(string)) {
                            Uri parse = Uri.parse(string);
                            Intent intent = new Intent(this, SSGCallActivity.class);
                            intent.setData(parse);
                            intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                            intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                            intent.putExtra(str4, sharedPrefGetBoolean);
                            intent.putExtra(str5, sharedPrefGetBoolean2);
                            intent.putExtra(str6, sharedPrefGetBoolean3);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                            intent.putExtra(str2, str3);
                            intent.putExtra(str11, sharedPrefGetBoolean4);
                            intent.putExtra(str13, sharedPrefGetBoolean5);
                            intent.putExtra(str15, sharedPrefGetBoolean6);
                            intent.putExtra(str17, sharedPrefGetBoolean7);
                            intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                            intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                            intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                            intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i202);
                            intent.putExtra(str9, sharedPrefGetString2);
                            intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean162);
                            intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean172);
                            intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                            intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                            boolean z8 = sharedPrefGetBoolean182;
                            intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                            if (z8) {

                                intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean192);
                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger4);
                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger22);
                                intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString32);
                                intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean202);
                                intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger32);
                            }
                            if (z7) {
                                Intent intent2 = getIntent();
                                String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                                if (intent2.hasExtra(str19)) {
                                    intent.putExtra(str19, getIntent().getStringExtra(str19));
                                }
                                Intent intent3 = getIntent();
                                String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                                if (intent3.hasExtra(str20)) {
                                    intent.putExtra(str20, getIntent().getStringExtra(str20));
                                }
                                Intent intent4 = getIntent();
                                String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                                if (intent4.hasExtra(str21)) {
                                    i15 = 0;
                                    intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                                } else {
                                    i15 = 0;
                                }
                                Intent intent5 = getIntent();
                                String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                                if (intent5.hasExtra(str22)) {
                                    intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                                }
                            }
                            startActivityForResult(intent, 1);
                            finish();
                            return;
                        }
                    }
                } else {
                    i17 = i10;
                }
                i11 = i17;
                if (!z7) {
                }
                if (i13 != 0) {
                }
                i14 = i16;
                int i2022 = i14;
                boolean sharedPrefGetBoolean1622 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                boolean sharedPrefGetBoolean1722 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                boolean sharedPrefGetBoolean1822 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                boolean sharedPrefGetBoolean1922 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                boolean sharedPrefGetBoolean2022 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                int sharedPrefGetInteger42 = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                int sharedPrefGetInteger222 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                int sharedPrefGetInteger322 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                String sharedPrefGetString322 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                StringBuilder sb322 = new StringBuilder();
                sb322.append("Connecting to room ");
                sb322.append(num);
                sb322.append(" at URL ");
                sb322.append(string);
                Log.d(str18, sb322.toString());
                if (validateUrl(string)) {
                    Uri parse = Uri.parse(string);
                    Intent intent = new Intent(this, SSGCallActivity.class);
                    intent.setData(parse);
                    intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                    intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                    intent.putExtra(str4, sharedPrefGetBoolean);
                    intent.putExtra(str5, sharedPrefGetBoolean2);
                    intent.putExtra(str6, sharedPrefGetBoolean3);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                    intent.putExtra(str2, str3);
                    intent.putExtra(str11, sharedPrefGetBoolean4);
                    intent.putExtra(str13, sharedPrefGetBoolean5);
                    intent.putExtra(str15, sharedPrefGetBoolean6);
                    intent.putExtra(str17, sharedPrefGetBoolean7);
                    intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                    intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                    intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                    intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i2022);
                    intent.putExtra(str9, sharedPrefGetString2);
                    intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean1622);
                    intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean1722);
                    intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                    intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                    boolean z8 = sharedPrefGetBoolean1822;
                    intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                    if (z8) {
                        intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean1922);
                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger42);
                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger222);
                        intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString322);
                        intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean2022);
                        intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger322);
                    }
                    if (z7) {
                        Intent intent2 = getIntent();
                        String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                        if (intent2.hasExtra(str19)) {
                            intent.putExtra(str19, getIntent().getStringExtra(str19));
                        }
                        Intent intent3 = getIntent();
                        String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                        if (intent3.hasExtra(str20)) {
                            intent.putExtra(str20, getIntent().getStringExtra(str20));
                        }
                        Intent intent4 = getIntent();
                        String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                        if (intent4.hasExtra(str21)) {
                            i15 = 0;
                            intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                        } else {
                            i15 = 0;
                        }
                        Intent intent5 = getIntent();
                        String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                        if (intent5.hasExtra(str22)) {
                            intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                        }
                    }
                    startActivityForResult(intent, 1);
                    finish();
                    return;
                }
            }
        } else {
            i19 = i3;
            str3 = sharedPrefGetString;
            str2 = str7;
        }
        boolean sharedPrefGetBoolean15 = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key, SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, R.string.pref_capturequalityslider_default, z7);
        if (z7) {
            z5 = sharedPrefGetBoolean15;
            i10 = getIntent().getIntExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, 0);
        } else {
            z5 = sharedPrefGetBoolean15;
            i10 = 0;
        }
        if (i10 == 0) {
            String string4 = getString(R.string.pref_maxvideobitrate_default);
            i17 = i10;
            if (!sharedPref.getString(this.np_keyprefVideoBitrateType, string4).equals(string4)) {
                i11 = Integer.parseInt(sharedPref.getString(this.np_keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default)));
                if (!z7) {
                    i12 = i11;
                    i13 = getIntent().getIntExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, 0);
                } else {
                    i12 = i11;
                    i13 = 0;
                }
                if (i13 != 0) {
                    String string5 = getString(R.string.pref_startaudiobitrate_default);
                    i16 = i13;
                    if (!sharedPref.getString(this.np_keyprefAudioBitrateType, string5).equals(string5)) {
                        i14 = Integer.parseInt(sharedPref.getString(this.np_keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default)));
                        int i20 = i14;
                        boolean sharedPrefGetBoolean16 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                        boolean sharedPrefGetBoolean17 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                        boolean sharedPrefGetBoolean18 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                        boolean sharedPrefGetBoolean19 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                        boolean sharedPrefGetBoolean20 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                        int sharedPrefGetInteger = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                        int sharedPrefGetInteger2 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                        int sharedPrefGetInteger3 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                        String sharedPrefGetString3 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append("Connecting to room ");
                        sb3.append(num);
                        sb3.append(" at URL ");
                        sb3.append(string);
                        Log.d(str18, sb3.toString());
                        if (validateUrl(string)) {
                            Uri parse = Uri.parse(string);
                            Intent intent = new Intent(this, SSGCallActivity.class);
                            intent.setData(parse);
                            intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                            intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                            intent.putExtra(str4, sharedPrefGetBoolean);
                            intent.putExtra(str5, sharedPrefGetBoolean2);
                            intent.putExtra(str6, sharedPrefGetBoolean3);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                            intent.putExtra(str2, str3);
                            intent.putExtra(str11, sharedPrefGetBoolean4);
                            intent.putExtra(str13, sharedPrefGetBoolean5);
                            intent.putExtra(str15, sharedPrefGetBoolean6);
                            intent.putExtra(str17, sharedPrefGetBoolean7);
                            intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                            intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                            intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                            intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i20);
                            intent.putExtra(str9, sharedPrefGetString2);
                            intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean16);
                            intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean17);
                            intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                            intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                            boolean z8 = sharedPrefGetBoolean18;
                            intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                            if (z8) {
                                intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean19);
                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger);
                                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger2);
                                intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString3);
                                intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean20);
                                intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger3);
                            }
                            if (z7) {
                                Intent intent2 = getIntent();
                                String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                                if (intent2.hasExtra(str19)) {
                                    intent.putExtra(str19, getIntent().getStringExtra(str19));
                                }
                                Intent intent3 = getIntent();
                                String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                                if (intent3.hasExtra(str20)) {
                                    intent.putExtra(str20, getIntent().getStringExtra(str20));
                                }
                                Intent intent4 = getIntent();
                                String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                                if (intent4.hasExtra(str21)) {
                                    i15 = 0;
                                    intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                                } else {
                                    i15 = 0;
                                }
                                Intent intent5 = getIntent();
                                String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                                if (intent5.hasExtra(str22)) {
                                    intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                                }
                            }
                            startActivityForResult(intent, 1);
                            finish();
                            return;
                        }
                        return;
                    }
                } else {
                    i16 = i13;
                }
                i14 = i16;
                int i202 = i14;
                boolean sharedPrefGetBoolean162 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
                boolean sharedPrefGetBoolean172 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
                boolean sharedPrefGetBoolean182 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
                boolean sharedPrefGetBoolean192 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
                boolean sharedPrefGetBoolean202 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
                int sharedPrefGetInteger4 = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
                int sharedPrefGetInteger22 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
                int sharedPrefGetInteger32 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
                String sharedPrefGetString32 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
                StringBuilder sb32 = new StringBuilder();
                sb32.append("Connecting to room ");
                sb32.append(num);
                sb32.append(" at URL ");
                sb32.append(string);
                Log.d(str18, sb32.toString());
                if (validateUrl(string)) {
                    Uri parse = Uri.parse(string);
                    Intent intent = new Intent(this, SSGCallActivity.class);
                    intent.setData(parse);
                    intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
                    intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
                    intent.putExtra(str4, sharedPrefGetBoolean);
                    intent.putExtra(str5, sharedPrefGetBoolean2);
                    intent.putExtra(str6, sharedPrefGetBoolean3);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
                    intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
                    intent.putExtra(str2, str3);
                    intent.putExtra(str11, sharedPrefGetBoolean4);
                    intent.putExtra(str13, sharedPrefGetBoolean5);
                    intent.putExtra(str15, sharedPrefGetBoolean6);
                    intent.putExtra(str17, sharedPrefGetBoolean7);
                    intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
                    intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
                    intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
                    intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
                    intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i202);
                    intent.putExtra(str9, sharedPrefGetString2);
                    intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean162);
                    intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean172);
                    intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
                    intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
                    boolean z8 = sharedPrefGetBoolean182;
                    intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
                    if (z8) {
                        intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean192);
                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger4);
                        intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger22);
                        intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString32);
                        intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean202);
                        intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger32);
                    }
                    if (z7) {
                        Intent intent2 = getIntent();
                        String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                        if (intent2.hasExtra(str19)) {
                            intent.putExtra(str19, getIntent().getStringExtra(str19));
                        }
                        Intent intent3 = getIntent();
                        String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                        if (intent3.hasExtra(str20)) {
                            intent.putExtra(str20, getIntent().getStringExtra(str20));
                        }
                        Intent intent4 = getIntent();
                        String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                        if (intent4.hasExtra(str21)) {
                            i15 = 0;
                            intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                        } else {
                            i15 = 0;
                        }
                        Intent intent5 = getIntent();
                        String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                        if (intent5.hasExtra(str22)) {
                            intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                        }
                    }
                    startActivityForResult(intent, 1);
                    finish();
                    return;
                }
            }
        } else {
            i17 = i10;
        }

        if (!z7) {
        }
        if (i13 != 0) {
        }
        i14 = i16;
        int i2022 = i14;
        boolean sharedPrefGetBoolean1622 = sharedPrefGetBoolean(R.string.pref_displayhud_key, SSGCallActivity.EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, z7);
        boolean sharedPrefGetBoolean1722 = sharedPrefGetBoolean(R.string.pref_tracing_key, SSGCallActivity.EXTRA_TRACING, R.string.pref_tracing_default, z7);
        boolean sharedPrefGetBoolean1822 = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key, SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default, z7);
        boolean sharedPrefGetBoolean1922 = sharedPrefGetBoolean(R.string.pref_ordered_key, SSGCallActivity.EXTRA_ORDERED, R.string.pref_ordered_default, z7);
        boolean sharedPrefGetBoolean2022 = sharedPrefGetBoolean(R.string.pref_negotiated_key, SSGCallActivity.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, z7);
        int sharedPrefGetInteger42 = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default, z7);
        int sharedPrefGetInteger222 = sharedPrefGetInteger(R.string.pref_max_retransmits_key, SSGCallActivity.EXTRA_MAX_RETRANSMITS, R.string.pref_max_retransmits_default, z7);
        int sharedPrefGetInteger322 = sharedPrefGetInteger(R.string.pref_data_id_key, SSGCallActivity.EXTRA_ID, R.string.pref_data_id_default, z7);
        String sharedPrefGetString322 = sharedPrefGetString(R.string.pref_data_protocol_key, SSGCallActivity.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, z7);
        StringBuilder sb322 = new StringBuilder();
        sb322.append("Connecting to room ");
        sb322.append(num);
        sb322.append(" at URL ");
        sb322.append(string);
        Log.d(str18, sb322.toString());
        if (validateUrl(string)) {
            Uri parse = Uri.parse(string);
            Intent intent = new Intent(this, SSGCallActivity.class);
            intent.setData(parse);
            intent.putExtra(SSGCallActivity.EXTRA_ROOMID, num);
            intent.putExtra(SSGCallActivity.EXTRA_LOOPBACK, z6);
            intent.putExtra(str4, sharedPrefGetBoolean);
            intent.putExtra(str5, sharedPrefGetBoolean2);
            intent.putExtra(str6, sharedPrefGetBoolean3);
            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_WIDTH, i8);
            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_HEIGHT, i6);
            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_FPS, i9);
            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED, z5);
            intent.putExtra(SSGCallActivity.EXTRA_VIDEO_BITRATE, i12);
            intent.putExtra(str2, str3);
            intent.putExtra(str11, sharedPrefGetBoolean4);
            intent.putExtra(str13, sharedPrefGetBoolean5);
            intent.putExtra(str15, sharedPrefGetBoolean6);
            intent.putExtra(str17, sharedPrefGetBoolean7);
            intent.putExtra(SSGCallActivity.EXTRA_AECDUMP_ENABLED, sharedPrefGetBoolean8);
            intent.putExtra(SSGCallActivity.EXTRA_OPENSLES_ENABLED, sharedPrefGetBoolean9);
            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AEC, sharedPrefGetBoolean10);
            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_AGC, sharedPrefGetBoolean11);
            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_BUILT_IN_NS, sharedPrefGetBoolean12);
            intent.putExtra(SSGCallActivity.EXTRA_ENABLE_LEVEL_CONTROL, sharedPrefGetBoolean13);
            intent.putExtra(SSGCallActivity.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, z4);
            intent.putExtra(SSGCallActivity.EXTRA_AUDIO_BITRATE, i2022);
            intent.putExtra(str9, sharedPrefGetString2);
            intent.putExtra(SSGCallActivity.EXTRA_DISPLAY_HUD, sharedPrefGetBoolean1622);
            intent.putExtra(SSGCallActivity.EXTRA_TRACING, sharedPrefGetBoolean1722);
            intent.putExtra(SSGCallActivity.EXTRA_CMDLINE, z);
            intent.putExtra(SSGCallActivity.EXTRA_RUNTIME, i);
            boolean z8 = sharedPrefGetBoolean1822;
            intent.putExtra(SSGCallActivity.EXTRA_DATA_CHANNEL_ENABLED, z8);
            if (z8) {
                intent.putExtra(SSGCallActivity.EXTRA_ORDERED, sharedPrefGetBoolean1922);
                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS_MS, sharedPrefGetInteger42);
                intent.putExtra(SSGCallActivity.EXTRA_MAX_RETRANSMITS, sharedPrefGetInteger222);
                intent.putExtra(SSGCallActivity.EXTRA_PROTOCOL, sharedPrefGetString322);
                intent.putExtra(SSGCallActivity.EXTRA_NEGOTIATED, sharedPrefGetBoolean2022);
                intent.putExtra(SSGCallActivity.EXTRA_ID, sharedPrefGetInteger322);
            }
            if (z7) {
                Intent intent2 = getIntent();
                String str19 = SSGCallActivity.EXTRA_VIDEO_FILE_AS_CAMERA;
                if (intent2.hasExtra(str19)) {
                    intent.putExtra(str19, getIntent().getStringExtra(str19));
                }
                Intent intent3 = getIntent();
                String str20 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE;
                if (intent3.hasExtra(str20)) {
                    intent.putExtra(str20, getIntent().getStringExtra(str20));
                }
                Intent intent4 = getIntent();
                String str21 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH;
                if (intent4.hasExtra(str21)) {
                    i15 = 0;
                    intent.putExtra(str21, getIntent().getIntExtra(str21, 0));
                } else {
                    i15 = 0;
                }
                Intent intent5 = getIntent();
                String str22 = SSGCallActivity.EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT;
                if (intent5.hasExtra(str22)) {
                    intent.putExtra(str22, getIntent().getIntExtra(str22, i15));
                }
            }
            startActivityForResult(intent, 1);
            finish();
        }

    }

    private boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }
        new AlertDialog.Builder(this).setTitle(getText(R.string.invalid_url_title)).setMessage(getString(R.string.invalid_url_text, new Object[]{url})).setCancelable(false).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).create().show();
        return false;
    }

    public byte[] encrypt(String str2, String str3) {
        int length = str3.length();
        byte[] bArr = new byte[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            bArr[i / 2] = (byte) ((Character.digit(str3.charAt(i), 16) << 4) + Character.digit(str3.charAt(i + 1), 16));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str2);
        sb.append("");
        SSGStaticVar.encriptedkey = sb.toString();
        try {
            char lowerCase = Character.toLowerCase(SSGStaticVar.encriptedkey.charAt(5));
            if (lowerCase == '%') {
                StringBuilder sb2 = new StringBuilder();
                sb2.append(SSGStaticVar.encriptedkey);
                sb2.append("9");
                SSGStaticVar.encriptedkey = sb2.toString();
            } else if (lowerCase != '?') {
                switch (lowerCase) {
                    case ' ':
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(SSGStaticVar.encriptedkey);
                        sb3.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
                        SSGStaticVar.encriptedkey = sb3.toString();
                        break;
                    case '!':
                        StringBuilder sb4 = new StringBuilder();
                        sb4.append(SSGStaticVar.encriptedkey);
                        sb4.append("8");
                        SSGStaticVar.encriptedkey = sb4.toString();
                        break;
                    case '\"':
                        StringBuilder sb5 = new StringBuilder();
                        sb5.append(SSGStaticVar.encriptedkey);
                        sb5.append('5');
                        SSGStaticVar.encriptedkey = sb5.toString();
                        break;
                    default:
                        switch (lowerCase) {
                            case '(':
                                StringBuilder sb6 = new StringBuilder();
                                sb6.append(SSGStaticVar.encriptedkey);
                                sb6.append('4');
                                SSGStaticVar.encriptedkey = sb6.toString();
                                break;
                            case ')':
                                StringBuilder sb7 = new StringBuilder();
                                sb7.append(SSGStaticVar.encriptedkey);
                                sb7.append("7");
                                SSGStaticVar.encriptedkey = sb7.toString();
                                break;
                            default:
                                switch (lowerCase) {
                                    case ',':
                                        StringBuilder sb8 = new StringBuilder();
                                        sb8.append(SSGStaticVar.encriptedkey);
                                        sb8.append("1");
                                        SSGStaticVar.encriptedkey = sb8.toString();
                                        break;
                                    case '-':
                                        StringBuilder sb9 = new StringBuilder();
                                        sb9.append(SSGStaticVar.encriptedkey);
                                        sb9.append("6");
                                        SSGStaticVar.encriptedkey = sb9.toString();
                                        break;
                                    case '.':
                                        StringBuilder sb10 = new StringBuilder();
                                        sb10.append(SSGStaticVar.encriptedkey);
                                        sb10.append('3');
                                        SSGStaticVar.encriptedkey = sb10.toString();
                                        break;
                                    default:
                                        switch (lowerCase) {
                                            case '0':
                                                StringBuilder sb11 = new StringBuilder();
                                                sb11.append(SSGStaticVar.encriptedkey);
                                                sb11.append("z");
                                                SSGStaticVar.encriptedkey = sb11.toString();
                                                break;
                                            case '1':
                                                StringBuilder sb12 = new StringBuilder();
                                                sb12.append(SSGStaticVar.encriptedkey);
                                                sb12.append("r");
                                                SSGStaticVar.encriptedkey = sb12.toString();
                                                break;
                                            case '2':
                                                StringBuilder sb13 = new StringBuilder();
                                                sb13.append(SSGStaticVar.encriptedkey);
                                                sb13.append("k");
                                                SSGStaticVar.encriptedkey = sb13.toString();
                                                break;
                                            case '3':
                                                StringBuilder sb14 = new StringBuilder();
                                                sb14.append(SSGStaticVar.encriptedkey);
                                                sb14.append("b");
                                                SSGStaticVar.encriptedkey = sb14.toString();
                                                break;
                                            case '4':
                                                StringBuilder sb15 = new StringBuilder();
                                                sb15.append(SSGStaticVar.encriptedkey);
                                                sb15.append("e");
                                                SSGStaticVar.encriptedkey = sb15.toString();
                                                break;
                                            case '5':
                                                StringBuilder sb16 = new StringBuilder();
                                                sb16.append(SSGStaticVar.encriptedkey);
                                                sb16.append("q");
                                                SSGStaticVar.encriptedkey = sb16.toString();
                                                break;
                                            case '6':
                                                StringBuilder sb17 = new StringBuilder();
                                                sb17.append(SSGStaticVar.encriptedkey);
                                                sb17.append("h");
                                                SSGStaticVar.encriptedkey = sb17.toString();
                                                break;
                                            case '7':
                                                StringBuilder sb18 = new StringBuilder();
                                                sb18.append(SSGStaticVar.encriptedkey);
                                                sb18.append("u");
                                                SSGStaticVar.encriptedkey = sb18.toString();
                                                break;
                                            case '8':
                                                StringBuilder sb19 = new StringBuilder();
                                                sb19.append(SSGStaticVar.encriptedkey);
                                                sb19.append("y");
                                                SSGStaticVar.encriptedkey = sb19.toString();
                                                break;
                                            case '9':
                                                StringBuilder sb20 = new StringBuilder();
                                                sb20.append(SSGStaticVar.encriptedkey);
                                                sb20.append("w");
                                                SSGStaticVar.encriptedkey = sb20.toString();
                                                break;
                                            default:
                                                switch (lowerCase) {
                                                    case 'a':
                                                        StringBuilder sb21 = new StringBuilder();
                                                        sb21.append(SSGStaticVar.encriptedkey);
                                                        sb21.append("{");
                                                        SSGStaticVar.encriptedkey = sb21.toString();
                                                        break;
                                                    case 'b':
                                                        StringBuilder sb22 = new StringBuilder();
                                                        sb22.append(SSGStaticVar.encriptedkey);
                                                        sb22.append("}");
                                                        SSGStaticVar.encriptedkey = sb22.toString();
                                                        break;
                                                    case 'c':
                                                        StringBuilder sb23 = new StringBuilder();
                                                        sb23.append(SSGStaticVar.encriptedkey);
                                                        sb23.append("#");
                                                        SSGStaticVar.encriptedkey = sb23.toString();
                                                        break;
                                                    case 'd':
                                                        StringBuilder sb24 = new StringBuilder();
                                                        sb24.append(SSGStaticVar.encriptedkey);
                                                        sb24.append("~");
                                                        SSGStaticVar.encriptedkey = sb24.toString();
                                                        break;
                                                    case 'e':
                                                        StringBuilder sb25 = new StringBuilder();
                                                        sb25.append(SSGStaticVar.encriptedkey);
                                                        sb25.append("+");
                                                        SSGStaticVar.encriptedkey = sb25.toString();
                                                        break;
                                                    case 'f':
                                                        StringBuilder sb26 = new StringBuilder();
                                                        sb26.append(SSGStaticVar.encriptedkey);
                                                        sb26.append("-");
                                                        SSGStaticVar.encriptedkey = sb26.toString();
                                                        break;
                                                    case 'g':
                                                        StringBuilder sb27 = new StringBuilder();
                                                        sb27.append(SSGStaticVar.encriptedkey);
                                                        sb27.append("*");
                                                        SSGStaticVar.encriptedkey = sb27.toString();
                                                        break;
                                                    case 'h':
                                                        StringBuilder sb28 = new StringBuilder();
                                                        sb28.append(SSGStaticVar.encriptedkey);
                                                        sb28.append("@");
                                                        SSGStaticVar.encriptedkey = sb28.toString();
                                                        break;
                                                    case 'i':
                                                        StringBuilder sb29 = new StringBuilder();
                                                        sb29.append(SSGStaticVar.encriptedkey);
                                                        sb29.append("/");
                                                        SSGStaticVar.encriptedkey = sb29.toString();
                                                        break;
                                                    case 'j':
                                                        StringBuilder sb30 = new StringBuilder();
                                                        sb30.append(SSGStaticVar.encriptedkey);
                                                        sb30.append("\\");
                                                        SSGStaticVar.encriptedkey = sb30.toString();
                                                        break;
                                                    case 'k':
                                                        StringBuilder sb31 = new StringBuilder();
                                                        sb31.append(SSGStaticVar.encriptedkey);
                                                        sb31.append("?");
                                                        SSGStaticVar.encriptedkey = sb31.toString();
                                                        break;
                                                    case 'l':
                                                        StringBuilder sb32 = new StringBuilder();
                                                        sb32.append(SSGStaticVar.encriptedkey);
                                                        sb32.append("$");
                                                        SSGStaticVar.encriptedkey = sb32.toString();
                                                        break;
                                                    case 'm':
                                                        StringBuilder sb33 = new StringBuilder();
                                                        sb33.append(SSGStaticVar.encriptedkey);
                                                        sb33.append("!");
                                                        SSGStaticVar.encriptedkey = sb33.toString();
                                                        break;
                                                    case 'n':
                                                        StringBuilder sb34 = new StringBuilder();
                                                        sb34.append(SSGStaticVar.encriptedkey);
                                                        sb34.append("^");
                                                        SSGStaticVar.encriptedkey = sb34.toString();
                                                        break;
                                                    case 'o':
                                                        StringBuilder sb35 = new StringBuilder();
                                                        sb35.append(SSGStaticVar.encriptedkey);
                                                        sb35.append("(");
                                                        SSGStaticVar.encriptedkey = sb35.toString();
                                                        break;
                                                    case 'p':
                                                        StringBuilder sb36 = new StringBuilder();
                                                        sb36.append(SSGStaticVar.encriptedkey);
                                                        sb36.append(")");
                                                        SSGStaticVar.encriptedkey = sb36.toString();
                                                        break;
                                                    case 'q':
                                                        StringBuilder sb37 = new StringBuilder();
                                                        sb37.append(SSGStaticVar.encriptedkey);
                                                        sb37.append("<");
                                                        SSGStaticVar.encriptedkey = sb37.toString();
                                                        break;
                                                    case 'r':
                                                        StringBuilder sb38 = new StringBuilder();
                                                        sb38.append(SSGStaticVar.encriptedkey);
                                                        sb38.append(">");
                                                        SSGStaticVar.encriptedkey = sb38.toString();
                                                        break;
                                                    case 's':
                                                        StringBuilder sb39 = new StringBuilder();
                                                        sb39.append(SSGStaticVar.encriptedkey);
                                                        sb39.append("=");
                                                        SSGStaticVar.encriptedkey = sb39.toString();
                                                        break;
                                                    case 't':
                                                        StringBuilder sb40 = new StringBuilder();
                                                        sb40.append(SSGStaticVar.encriptedkey);
                                                        sb40.append(";");
                                                        SSGStaticVar.encriptedkey = sb40.toString();
                                                        break;
                                                    case 'u':
                                                        StringBuilder sb41 = new StringBuilder();
                                                        sb41.append(SSGStaticVar.encriptedkey);
                                                        sb41.append(",");
                                                        SSGStaticVar.encriptedkey = sb41.toString();
                                                        break;
                                                    case 'v':
                                                        StringBuilder sb42 = new StringBuilder();
                                                        sb42.append(SSGStaticVar.encriptedkey);
                                                        sb42.append("_");
                                                        SSGStaticVar.encriptedkey = sb42.toString();
                                                        break;
                                                    case 'w':
                                                        StringBuilder sb43 = new StringBuilder();
                                                        sb43.append(SSGStaticVar.encriptedkey);
                                                        sb43.append("[");
                                                        SSGStaticVar.encriptedkey = sb43.toString();
                                                        break;
                                                    case 'x':
                                                        StringBuilder sb44 = new StringBuilder();
                                                        sb44.append(SSGStaticVar.encriptedkey);
                                                        sb44.append("]");
                                                        SSGStaticVar.encriptedkey = sb44.toString();
                                                        break;
                                                    case 'y':
                                                        StringBuilder sb45 = new StringBuilder();
                                                        sb45.append(SSGStaticVar.encriptedkey);
                                                        sb45.append(":");
                                                        SSGStaticVar.encriptedkey = sb45.toString();
                                                        break;
                                                    case 'z':
                                                        StringBuilder sb46 = new StringBuilder();
                                                        sb46.append(SSGStaticVar.encriptedkey);
                                                        sb46.append("\"");
                                                        SSGStaticVar.encriptedkey = sb46.toString();
                                                        break;
                                                    default:
                                                        StringBuilder sb47 = new StringBuilder();
                                                        sb47.append(SSGStaticVar.encriptedkey);
                                                        sb47.append("0");
                                                        SSGStaticVar.encriptedkey = sb47.toString();
                                                        break;
                                                }
                                                break;
                                        }
                                }
                        }
                }
            } else {
                StringBuilder sb48 = new StringBuilder();
                sb48.append(SSGStaticVar.encriptedkey);
                sb48.append("2");
                SSGStaticVar.encriptedkey = sb48.toString();
            }
        } catch (Exception e) {
        }
        return bArr;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(null);
        }
    }

    @Override
    public void onBackPressed() {
        if (handler != null) {
            handler.removeCallbacks(null);
        }
        finish();
    }
}