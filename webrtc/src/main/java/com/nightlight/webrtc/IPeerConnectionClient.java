package com.nightlight.webrtc;

import org.webrtc.EglRenderer;

public interface IPeerConnectionClient {
    void addOnFrameListener(EglRenderer.FrameListener onFrameListener);

    void removeOnFrameListener();

    boolean isPlaying();

    void playOrPause(boolean play);

    boolean isSpeaker();

    void enableSpeaker(boolean isSpeaker);

    boolean isMute();

    void muteAudio(boolean isMute);

    void createOffer(String url);

    void release();
}
