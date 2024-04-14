package com.nightlight.webrtc;

import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.SurfaceViewRenderer;

public class WebRTCClient {

    private final IPeerConnectionClient peerConnectionClient;

    public WebRTCClient(SurfaceViewRenderer surfaceViewRenderer, EglBase.Context eglBaseContext) {
        peerConnectionClient = new PeerConnectionClient(surfaceViewRenderer, eglBaseContext);
    }

    public void start(String url) {
        peerConnectionClient.createOffer(url);
    }

    // 播放视频
    public void playOrPause(boolean play) {
        peerConnectionClient.playOrPause(play);
    }

    public boolean isPlaying() {
        return peerConnectionClient.isPlaying();
    }

    public boolean isMute() {
        return peerConnectionClient.isMute();
    }

    public boolean isSpeaker() {
        return peerConnectionClient.isSpeaker();
    }

    // 静音
    public void muteAudio(boolean mute) {
        peerConnectionClient.muteAudio(mute);
    }

    // 打开扬声器
    public void enableSpeaker(boolean enabled) {
        peerConnectionClient.enableSpeaker(enabled);
    }

    public void addOnFrameListener(EglRenderer.FrameListener onFrameListener) {
        peerConnectionClient.addOnFrameListener(onFrameListener);
    }

    public void removeOnFrameListener() {
        peerConnectionClient.removeOnFrameListener();
    }

    public void release() {
        peerConnectionClient.release();
    }
}
