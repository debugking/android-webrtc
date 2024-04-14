package com.nightlight.webrtc;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.view.SurfaceView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioTrack;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSink;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerConnectionClient implements IPeerConnectionClient {

    private final HttpSignalingClient signalingClient;
    private PeerConnectionFactory factory;
    private final AudioManager audioManager;
    private PeerConnection peerConnection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SurfaceViewRenderer surfaceViewRenderer;

    private MediaStream remoteMediaStream;

    private VideoTrack remoteVideoTrack;

    static {
        System.loadLibrary("jingle_peerconnection_so");
    }

    private AudioTrack remoteAudioTrack;
    private boolean isSpeaker;
    private boolean isMute;
    private EglRenderer.FrameListener onFrameListener;

    public PeerConnectionClient(SurfaceViewRenderer surfaceViewRenderer, EglBase.Context eglBaseContext) {
        this.surfaceViewRenderer = surfaceViewRenderer;
        surfaceViewRenderer.init(eglBaseContext, null);
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        surfaceViewRenderer.setEnableHardwareScaler(true);
        surfaceViewRenderer.setFpsReduction(30f);
        Context context = surfaceViewRenderer.getContext();
        peerConnection = createPeerConnection(context, eglBaseContext);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(false);
        signalingClient = new HttpSignalingClient();
    }

    private PeerConnection createPeerConnection(Context context, EglBase.Context eglBaseContext) {
        // 初始化PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true)
                        .createInitializationOptions();
        Logging.enableLogToDebugOutput(Logging.Severity.LS_ERROR);
        PeerConnectionFactory.initialize(initializationOptions);
        DefaultVideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(eglBaseContext,
                false, true);
        DefaultVideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(eglBaseContext);

        factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.IceServer stun = new PeerConnection.IceServer("stun:stun.l.google.com:19302");
        iceServers.add(stun);

        PeerConnection.RTCConfiguration configuration = new PeerConnection.RTCConfiguration(iceServers);
        configuration.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        configuration.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        configuration.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        return factory.createPeerConnection(configuration, new PeerObserverAdapter() {
            @Override
            public void onAddStream(MediaStream mediaStream) {
                remoteMediaStream = mediaStream;
                super.onAddStream(mediaStream);
                if (surfaceViewRenderer != null) {
                    List<VideoTrack> videoTracks = mediaStream.videoTracks;
                    if (!videoTracks.isEmpty()) {
                        remoteVideoTrack = videoTracks.get(0);
                        remoteVideoTrack.setEnabled(true);
                        remoteVideoTrack.addSink(surfaceViewRenderer);
                    }
                    List<AudioTrack> audioTracks = mediaStream.audioTracks;
                    if (!audioTracks.isEmpty()) {
                        remoteAudioTrack = audioTracks.get(0);
                        remoteAudioTrack.setEnabled(true);
                    }
                }
            }
        });
    }


    public void addOnFrameListener(EglRenderer.FrameListener onFrameListener) {
        this.onFrameListener = onFrameListener;
        if (surfaceViewRenderer != null) {
            surfaceViewRenderer.addFrameListener(onFrameListener, 2f);
        }
    }

    public void removeOnFrameListener() {
        if (onFrameListener != null && surfaceViewRenderer != null) {
            surfaceViewRenderer.removeFrameListener(onFrameListener);
        }
    }

    public void playOrPause(boolean play) {
        if (remoteVideoTrack != null) {
            remoteVideoTrack.setEnabled(play);
        }
    }

    public boolean isSpeaker() {
        return isSpeaker;
    }

    public void enableSpeaker(boolean isSpeaker) {
        this.isSpeaker = isSpeaker;
        audioManager.setSpeakerphoneOn(isSpeaker);
    }

    public boolean isMute() {
        return isMute;
    }

    public void muteAudio(boolean isMute) {
        this.isMute = isMute;
        if (remoteAudioTrack != null) {
            remoteAudioTrack.setEnabled(!isMute);
        }
    }


    public void createOffer(String url) {
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        if (peerConnection != null) {
            peerConnection.createOffer(new SdpObserverAdapter() {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    if (sessionDescription.type == SessionDescription.Type.OFFER) {
                        executor.execute(() -> {
                            String sdpDescription = sessionDescription.description;
                            sdpDescription = SDPUtils.preferCodec(sdpDescription, "H264", false);
                            final SessionDescription sdp = new SessionDescription(sessionDescription.type, sdpDescription);
                            if (peerConnection != null) {
                                peerConnection.setLocalDescription(this, sdp);
                                String result = signalingClient.send(url, sdp.description);
                                if (result != null) {
                                    try {
                                        JSONObject jsonResult = new JSONObject(result);
                                        String sdpRemote = jsonResult.getString("sdp");
                                        peerConnection.setRemoteDescription(this, new SessionDescription(SessionDescription.Type.ANSWER, sdpRemote));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }

                }
            }, mediaConstraints);
        }
    }

    public void release() {
        // 停止媒体流轨道
        if (remoteMediaStream != null) {
            for (AudioTrack track : remoteMediaStream.audioTracks) {
                track.setEnabled(false);
                track.dispose();
            }
            for (VideoTrack track : remoteMediaStream.videoTracks) {
                track.setEnabled(false);
                track.dispose();
            }
            peerConnection.removeStream(remoteMediaStream);
            remoteMediaStream.dispose();
            remoteMediaStream = null;
        }

        removeOnFrameListener();

        // 关闭 PeerConnection
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
            peerConnection = null;
        }

        // 释放 PeerConnectionFactory
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        if (surfaceViewRenderer != null) {
            surfaceViewRenderer.release();
        }

        // 重置音频设置
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_NORMAL);
    }

    public boolean isPlaying() {
        return remoteVideoTrack != null && remoteVideoTrack.enabled();
    }
}
