package com.nightlight.webrtc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private WebRTCClient webRTCClient;
    private SurfaceViewRenderer remoteRenderer;

    private Button btnPlay;
    private Button btnPause;
    private Button btnMute;
    private Button btnSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        remoteRenderer = findViewById(R.id.surface_view);
        EglBase.Context eglBaseContext = EglBase.create().getEglBaseContext();
        webRTCClient = new WebRTCClient(remoteRenderer, eglBaseContext);
        btnPlay = findViewById(R.id.btn_play);
        btnPlay.setOnClickListener(this);
        btnPause = findViewById(R.id.btn_pause);
        btnPause.setOnClickListener(this);
        btnMute = findViewById(R.id.btn_mute);
        btnMute.setOnClickListener(this);
        btnSpeaker = findViewById(R.id.btn_speaker);
        btnSpeaker.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_play) {
            webRTCClient.start("http://192.144.229.234:80/index/api/webrtc?app=rtp&stream=4401020048_44010200482000000002&type=play");
        } else if (id == R.id.btn_pause) {
            if (webRTCClient.isPlaying()) {
                webRTCClient.playOrPause(false);
                remoteRenderer.clearImage();
            } else {
                webRTCClient.playOrPause(true);
            }
            btnPause.setText(webRTCClient.isPlaying() ? "暂停" : "播放");
        } else if (id == R.id.btn_mute) {
            webRTCClient.muteAudio(!webRTCClient.isMute());
            btnMute.setText(webRTCClient.isMute() ? "静音" : "非静音");
        } else if (id == R.id.btn_speaker) {
            webRTCClient.enableSpeaker(!webRTCClient.isSpeaker());
            btnSpeaker.setText(webRTCClient.isSpeaker() ? "扬声器" : "非扬声器");
        }
    }
}