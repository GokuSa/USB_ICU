/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shine.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.shine.tools.HttpGetUtils;
import com.shine.usbcameralib.gles.CameraUtils;
import com.shine.usbcameralib.gles.FullFrameRect;
import com.shine.usbcameralib.gles.Texture2dProgram;
import com.shine.usbcameralib.gles.TextureMovieEncoder;
import com.shine.visitsystem.HomeActivity;
import com.shine.visitsystem.MyApplication;
import com.shine.visitsystem.R;
import com.shine.visitsystem.databinding.FragmentVideoTalkBinding;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.shine.fragment.VideoTalkFragment.CameraHandler.MSG_PLAY_LIVE;
import static com.shine.fragment.VideoTalkFragment.CameraHandler.MSG_START_COUNT;

/**
 * 视频对讲页面，Camera预览和编码部分参考Grafika
 * 使用VLC播放自定义视频流，在Camera开始编码后一段时间才开始，最大限度保证转发服务器已经接受到编码流
 * 如果失败 隔几秒重新拨
 */
public class VideoTalkFragment extends Fragment
        implements SurfaceTexture.OnFrameAvailableListener {
    public static final String TAG = "VideoTalkFragment";
    private static final String ARG_KEY_URL = "url";
    private static final String ARG_KEY_OVER_TIME = "over_time";
    private AudioManager mAudioManager;
    /**
     * 当前音量
     */
    private int mCurrentVolume;
    /**
     * 最大音量
     */
    private int mMaxVolume = 15;
    /**
     * 是否能说话 与静音切换
     */
    private boolean canTalk = true;
    private GLSurfaceView mGLView;
    private CameraSurfaceRenderer mRenderer;
    private Camera mCamera;

    private CameraHandler mCameraHandler;

    private boolean mRecordingEnabled;
    private int mCameraPreviewWidth, mCameraPreviewHeight;
    private TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();
    private int mWScreen;
    private int mHScreen;
    private FragmentVideoTalkBinding mBinding;
    private MediaPlayer mMediaPlayer;
    private boolean isCameraPreviewVisible = true;
    private VideoView mCurrentVideo;

    public static VideoTalkFragment newInstance(String url, String overTime) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY_URL, url);
        args.putString(ARG_KEY_OVER_TIME, overTime);
        VideoTalkFragment fragment = new VideoTalkFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //停止获取会议信息
        HttpGetUtils.getInstance().isStop_notify();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_talk, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //没有使用，仅为了与参考项目保持类结构一致
        File outputFile = new File("extdata/test.mp4");
        mCameraHandler = new CameraHandler(this);
        mRecordingEnabled = sVideoEncoder.isRecording();

        mGLView = mBinding.cameraPreviewSurfaceView;
        mGLView.setZOrderMediaOverlay(true);

        mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        mRenderer = new CameraSurfaceRenderer(mCameraHandler, sVideoEncoder, outputFile);
        mGLView.setRenderer(mRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mBinding.ivVolumeMinus.setOnClickListener(mClickListener);
        mBinding.ivVolumePlus.setOnClickListener(mClickListener);
        mBinding.ivMac.setOnClickListener(mClickListener);
        mBinding.ivHidden.setOnClickListener(mClickListener);
        mBinding.ivSwitch.setOnClickListener(mClickListener);
        setupMediaPlayer();
        Log.d(TAG, "onCreate complete: " + this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //转化成百分制格式
        mCurrentVolume = volume * 100 / mMaxVolume;
        //结束时间
        String over_time = getArguments().getString(ARG_KEY_OVER_TIME, "");
        try {
            long over = Long.parseLong(over_time);
            int time_margin = (int) ((over - System.currentTimeMillis()) / 1000);
            Log.d(TAG, "time_margin:" + time_margin);
            if (time_margin > 0) {
                mCameraHandler.obtainMessage(MSG_START_COUNT, time_margin, 0).sendToTarget();
            } else {
                Log.d(TAG, "onActivityCreated: get old time");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "onActivityCreated: 解析结束时间错误");
        }
        //延迟播放对方视频，因为需要等待摄像头编码发送成功
        mCurrentVideo = mBinding.videoView;
        mCameraHandler.sendEmptyMessageDelayed(MSG_PLAY_LIVE, 8000);
        //显示等待对方视频对话框
        showWaitingDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        openCamera(1280, 720);
        mGLView.onResume();
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.setCameraPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);
            }
        });
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume -- ");
        super.onResume();
        Log.d(TAG, "onResume complete: " + this);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause -- ");
        super.onPause();
        Log.d(TAG, "onPause complete");
    }

    @Override
    public void onStop() {
        super.onStop();
        closeWaitingDialog();
        releaseCamera();
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                // Tell the renderer that it's about to be paused so it can clean up.
                mRenderer.notifyPausing();
            }
        });
        mGLView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mCameraHandler.invalidateHandler();
        mCameraHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public void stopRecording() {
        Log.d(TAG, "stopRecording() called");
        //如果是相机出错关闭页面 不再处理消息，如尝试打开直播流
        mCameraHandler.invalidateHandler();
        mCameraHandler.removeCallbacksAndMessages(null);
        stopPlayStream();
        closeWaitingDialog();
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(false);
            }
        });
    }


    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_volume_minus:
                    mCurrentVolume = Math.max(0, mCurrentVolume - 5);
                    handleVolume();
                    break;
                case R.id.iv_volume_plus:
                    mCurrentVolume = Math.min(100, mCurrentVolume + 5);
                    handleVolume();
                    break;
                //静音切换
                case R.id.iv_mac:
                    canTalk = !canTalk;
                    if (canTalk) {
                        mBinding.ivMac.setImageResource(R.drawable.open_mac);
                    } else {
                        mBinding.ivMac.setImageResource(R.drawable.close_mac);
                    }
                    HomeActivity activity = (HomeActivity) getActivity();
                    activity.resetTalk(canTalk);
                    //防止频繁点击 先禁用 5秒后解禁
                    mBinding.ivMac.setEnabled(false);
                    Toast.makeText(getActivity(), "5秒后可再次切换", Toast.LENGTH_SHORT).show();
                    mCameraHandler.sendEmptyMessageDelayed(CameraHandler.MSG_MIC_ENABLE, 5 * 1000);
                    break;

                case R.id.iv_hidden:
                    isCameraPreviewVisible = !isCameraPreviewVisible;
                    if (isCameraPreviewVisible) {
                        mBinding.ivHidden.setImageResource(R.drawable.nohidden);
                    } else {
                        mBinding.ivHidden.setImageResource(R.drawable.hidden_ico);
                    }
                    float density = getResources().getDisplayMetrics().density;
                    RelativeLayout.LayoutParams layoutParams;
                    if (isCameraPreviewVisible) {
                        layoutParams = new RelativeLayout.LayoutParams((int) (320 * density), (int) (240 * density));
                    } else {
                        layoutParams = new RelativeLayout.LayoutParams(1, 1);
                    }
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    mGLView.setLayoutParams(layoutParams);
                    break;
                case R.id.iv_switch:
                    handleSwitch();
                    break;

            }
        }
    };


    private void handleSwitch() {
        Log.d(TAG, "handleSwitch: ");
    }

    /**
     * 调节音量，步长为5，最大为100，最小为0
     */
    private void handleVolume() {
        Log.d(TAG, "handlePlusVolume() called " + mCurrentVolume);
        int volume = mCurrentVolume * mMaxVolume / 100;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                volume, AudioManager.FLAG_SHOW_UI);
        HomeActivity activity = (HomeActivity) getActivity();
        //底层音量范围是0-30，AudioManager是0-15
        activity.setVolume(volume * 2);
    }


    /**
     * Opens a camera, and attempts to establish preview mode at the specified width and height.
     * <p>
     * Sets mCameraPreviewWidth and mCameraPreviewHeight to the actual width/height of the preview.
     */
    private void openCamera(int desiredWidth, int desiredHeight) {
        try {
            if (mCamera != null) {
                throw new RuntimeException("camera already initialized");
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            // Try to find a front-facing camera (e.g. for videoconferencing).
            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCamera = Camera.open(i);
                    break;
                }
            }
            if (mCamera == null) {
                Log.d(TAG, "No front-facing camera found; opening default");
                mCamera = Camera.open();    // opens first back-facing camera
            }
            if (mCamera == null) {
                throw new RuntimeException("Unable to open camera");
            }
            Camera.Parameters parms = mCamera.getParameters();
            CameraUtils.choosePreviewSize(parms, desiredWidth, desiredHeight);
            parms.setRecordingHint(true);
            mCamera.setParameters(parms);
            int[] fpsRange = new int[2];
            Camera.Size mCameraPreviewSize = parms.getPreviewSize();
            parms.getPreviewFpsRange(fpsRange);
            mCameraPreviewWidth = mCameraPreviewSize.width;
            mCameraPreviewHeight = mCameraPreviewSize.height;
        } catch (RuntimeException e) {
            e.printStackTrace();
            /*Toast.makeText(MyApplication.getInstance(), "相机故障，请检查摄像头", Toast.LENGTH_LONG).show();
            getActivity().finish();*/
            showCameraErr();
        }
    }

    private void showCameraErr() {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        CameraErrDialog camera_err = (CameraErrDialog) getActivity().getSupportFragmentManager().findFragmentByTag("camera_err");
        if (camera_err != null) {
            fragmentTransaction.remove(camera_err);
        }
        camera_err = CameraErrDialog.newInstance("相机故障，请检查摄像头");
        camera_err.show(fragmentTransaction, "camera_err");
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    private void handleSetSurfaceTexture(SurfaceTexture st) {
        st.setOnFrameAvailableListener(this);
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(st);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            mCamera.startPreview();
        }

        Log.d(TAG, "handleSetSurfaceTexture: start preview");
        mGLView.queueEvent(new Runnable() {
            @Override
            public void run() {
                // notify the renderer that we want to change the encoder's state
                mRenderer.changeRecordingState(true);
            }
        });
    }


    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        mGLView.requestRender();
    }

    private void setupMediaPlayer() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mWScreen = dm.widthPixels;
        mHScreen = dm.heightPixels;
    }

    private void playVideoStream() {
        final String path = getArguments().getString(ARG_KEY_URL, "");
        Log.d(TAG, "playVideoStream: playVideoStream " + path);
        LibVLC mLibVLC = new LibVLC(getActivity());
        Media media = new Media(mLibVLC, Uri.parse(path));
        media.setHWDecoderEnabled(true, true);
        mMediaPlayer = new MediaPlayer(media);
        IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.setVideoView(mCurrentVideo);
        if (MyApplication.getInstance().is720P()) {
            vlcVout.setWindowSize(mWScreen, mHScreen);
        } else {
            vlcVout.setWindowSize(mWScreen, (int) (mWScreen * 0.75f));
        }
        vlcVout.attachViews();

        mMediaPlayer.setVideoTrackEnabled(true);
        mMediaPlayer.setEventListener(mEventListener);

        mMediaPlayer.play();

    }



    private void stopPlayStream() {
        Log.d(TAG, "stopPlayStream() called");
        if (mMediaPlayer != null) {
            mMediaPlayer.getVLCVout().detachViews();
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer.setEventListener(null);
            mMediaPlayer = null;
        }
    }

    private void enableSwitchMic() {
        mBinding.ivMac.setEnabled(true);
    }

    private int count = 3;
    private MediaPlayer.EventListener mEventListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.EncounteredError:
                case MediaPlayer.Event.EndReached:
                    Log.e(TAG, "onEvent: play error");
                    stopPlayStream();
                    if (count-- > 0) {
                        mCameraHandler.sendEmptyMessageDelayed(MSG_PLAY_LIVE, 3000);
                    } else {
                        // TODO: 2017/8/25 show dialog fail to play video
                    }
                    break;
                case MediaPlayer.Event.Playing:
                    count = 3;
                    Log.d(TAG, "playing");
                    HomeActivity homeActivity = (HomeActivity) getActivity();
                    homeActivity.startTalk();
                    closeWaitingDialog();
                    break;

            }
        }
    };


    /**
     * Handles camera operation requests from other threads.  Necessary because the Camera
     * must only be accessed from one thread.
     * <p>
     * The object is created on the UI thread, and all handlers run there.  Messages are
     * sent from other threads, using sendMessage().
     */
    static class CameraHandler extends Handler {
        public static final int MSG_SET_SURFACE_TEXTURE = 0;
        public static final int MSG_COUNT_DOWN = 1;
        public static final int MSG_PLAY_LIVE = 2;
        public static final int MSG_START_COUNT = 3;
        public static final int MSG_MIC_ENABLE = 4;
        /**
         * 默认通话时长30分，换算成以秒为单位
         */
        private int mDefaultDuration = 30 * 60;
        private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("00:mm:ss", Locale.CHINA);

        // Weak reference to the Activity; only access this from the UI thread.
        private WeakReference<VideoTalkFragment> mWeakActivity;

        public CameraHandler(VideoTalkFragment talkFragment2) {
            mWeakActivity = new WeakReference<>(talkFragment2);
        }

        /**
         * Drop the reference to the activity.  Useful as a paranoid measure to ensure that
         * attempts to access a stale Activity through a handler are caught.
         */
        public void invalidateHandler() {
            //用来终止倒计时
            mDefaultDuration = -1;
            mWeakActivity.clear();
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;

            VideoTalkFragment fragment = mWeakActivity.get();
            if (fragment == null) {
                Log.w(TAG, "CameraHandler.handleMessage: activity is null");
                return;
            }

            switch (what) {
                case MSG_SET_SURFACE_TEXTURE:
                    fragment.handleSetSurfaceTexture((SurfaceTexture) inputMessage.obj);
                    break;
                case MSG_START_COUNT:
                    mDefaultDuration = inputMessage.arg1;
                    sendEmptyMessage(MSG_COUNT_DOWN);
                    break;
                case MSG_COUNT_DOWN:
                    if (--mDefaultDuration >= 0) {
                        String timeLeft = mSimpleDateFormat.format(mDefaultDuration * 1000);
                        fragment.changeTimeLeft(timeLeft);
                        sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);
                    }
                    break;
                case MSG_PLAY_LIVE:
                    fragment.playVideoStream();
                    break;
                case MSG_MIC_ENABLE:
                    fragment.enableSwitchMic();
                    break;
            }
        }
    }


    /**
     * 显示视频倒计时
     *
     * @param timeLeft
     */
    private void changeTimeLeft(String timeLeft) {
        mBinding.tvDownTime.setText(String.format(Locale.CHINA, "剩余时间   %s", timeLeft));
    }


    /**
     * Renderer object for our GLSurfaceView.
     * <p>
     * Do not call any methods here directly from another thread -- use the
     * GLSurfaceView#queueEvent() call.
     */
    class CameraSurfaceRenderer implements GLSurfaceView.Renderer {
        private static final String TAG = "CameraSurfaceRenderer";
        private static final boolean VERBOSE = false;

        private static final int RECORDING_OFF = 0;
        private static final int RECORDING_ON = 1;
        private static final int RECORDING_RESUMED = 2;

        private CameraHandler mCameraHandler;
        private TextureMovieEncoder mVideoEncoder;
        private File mFileOutput;

        private FullFrameRect mFullScreen;

        private final float[] mSTMatrix = new float[16];
        private int mTextureId;

        private SurfaceTexture mSurfaceTexture;
        private boolean mRecordingEnabled;
        private int mRecordingStatus;

        // width/height of the incoming camera preview frames
        private boolean mIncomingSizeUpdated;
        private int mIncomingWidth;
        private int mIncomingHeight;


        /**
         * Constructs CameraSurfaceRenderer.
         * <p>
         *
         * @param cameraHandler Handler for communicating with UI thread
         * @param movieEncoder  video encoder object
         */
        public CameraSurfaceRenderer(CameraHandler cameraHandler,
                                     TextureMovieEncoder movieEncoder, File fileOutput) {
            mCameraHandler = cameraHandler;
            mVideoEncoder = movieEncoder;
            mFileOutput = fileOutput;

            mTextureId = -1;

            mRecordingStatus = -1;
            mRecordingEnabled = false;
//            mFrameCount = -1;

            mIncomingSizeUpdated = false;
            mIncomingWidth = mIncomingHeight = -1;

        }


        /**
         * Notifies the renderer thread that the activity is pausing.
         * <p>
         * For best results, call this *after* disabling Camera preview.
         */
        public void notifyPausing() {
            if (mSurfaceTexture != null) {
                Log.d(TAG, "renderer pausing -- releasing SurfaceTexture");
                mSurfaceTexture.release();
                mSurfaceTexture = null;
            }
            if (mFullScreen != null) {
                mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
                mFullScreen = null;             //  to be destroyed
            }
            mIncomingWidth = mIncomingHeight = -1;
        }

        /**
         * Notifies the renderer that we want to stop or start recording.
         */
        public void changeRecordingState(boolean isRecording) {
            Log.d(TAG, "changeRecordingState: was " + mRecordingEnabled + " now " + isRecording);
            mRecordingEnabled = isRecording;
        }

        /**
         * Records the size of the incoming camera preview frames.
         * <p>
         * It's not clear whether this is guaranteed to execute before or after onSurfaceCreated(),
         * so we assume it could go either way.  (Fortunately they both run on the same thread,
         * so we at least know that they won't execute concurrently.)
         */
        public void setCameraPreviewSize(int width, int height) {
            Log.d(TAG, "setCameraPreviewSize");
            mIncomingWidth = width;
            mIncomingHeight = height;
            mIncomingSizeUpdated = true;
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            Log.d(TAG, "onSurfaceCreated");

            // We're starting up or coming back.  Either way we've got a new EGLContext that will
            // need to be shared with the video encoder, so figure out if a recording is already
            // in progress.
            mRecordingEnabled = mVideoEncoder.isRecording();
            if (mRecordingEnabled) {
                mRecordingStatus = RECORDING_RESUMED;
            } else {
                mRecordingStatus = RECORDING_OFF;
            }

            // Set up the texture blitter that will be used for on-screen display.  This
            // is *not* applied to the recording, because that uses a separate shader.
            mFullScreen = new FullFrameRect(
                    new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));

            mTextureId = mFullScreen.createTextureObject();

            // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
            // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
            // available messages will arrive on the main thread.
            mSurfaceTexture = new SurfaceTexture(mTextureId);

            // Tell the UI thread to enable the camera preview.
            mCameraHandler.sendMessage(mCameraHandler.obtainMessage(
                    CameraHandler.MSG_SET_SURFACE_TEXTURE, mSurfaceTexture));


        }


        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            Log.d(TAG, "onSurfaceChanged " + width + "x" + height);
        }

        @Override
        public void onDrawFrame(GL10 unused) {
//            if (VERBOSE) Log.d(TAG, "onDrawFrame tex=" + mTextureId);
//            boolean showBox = false;

            // Latch the latest frame.  If there isn't anything new, we'll just re-use whatever
            // was there before.
            mSurfaceTexture.updateTexImage();

            // If the recording state is changing, take care of it here.  Ideally we wouldn't
            // be doing all this in onDrawFrame(), but the EGLContext sharing with GLSurfaceView
            // makes it hard to do elsewhere.
            if (mRecordingEnabled) {
                switch (mRecordingStatus) {
                    case RECORDING_OFF:
                        Log.d(TAG, "START recording");
                        // start recording
                        mVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(mFileOutput, 640, 480, 1000000, EGL14.eglGetCurrentContext()));
                        Log.d(TAG, "1M编码");
                        mRecordingStatus = RECORDING_ON;
                        break;
                    case RECORDING_RESUMED:
                        Log.d(TAG, "RESUME recording");
                        mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                        mRecordingStatus = RECORDING_ON;
                        break;
                    case RECORDING_ON:
                        // yay
                        break;
                    default:
                        throw new RuntimeException("unknown status " + mRecordingStatus);
                }
            } else {
                switch (mRecordingStatus) {
                    case RECORDING_ON:
                    case RECORDING_RESUMED:
                        // stop recording
                        Log.d(TAG, "STOP recording");
                        mVideoEncoder.stopRecording();
                        mRecordingStatus = RECORDING_OFF;
                        break;
                    case RECORDING_OFF:
                        // yay
                        break;
                    default:
                        throw new RuntimeException("unknown status " + mRecordingStatus);
                }
            }

            // Set the video encoder's texture name.  We only need to do this once, but in the
            // current implementation it has to happen after the video encoder is started, so
            // we just do it here.
            //
            // TODO: be less lame.
            mVideoEncoder.setTextureId(mTextureId);

            // Tell the video encoder thread that a new frame is available.
            // This will be ignored if we're not actually recording.
            mVideoEncoder.frameAvailable(mSurfaceTexture);

            if (mIncomingWidth <= 0 || mIncomingHeight <= 0) {
                // Texture size isn't set yet.  This is only used for the filters, but to be
                // safe we can just skip drawing while we wait for the various races to resolve.
                // (This seems to happen if you toggle the screen off/on with power button.)
                Log.i(TAG, "Drawing before incoming texture size set; skipping");
                return;
            }

            if (mIncomingSizeUpdated) {
                mFullScreen.getProgram().setTexSize(mIncomingWidth, mIncomingHeight);
                mIncomingSizeUpdated = false;
            }

            // Draw the video frame.

            mSurfaceTexture.getTransformMatrix(mSTMatrix);
            mFullScreen.drawFrame(mTextureId, mSTMatrix);
        }
    }


    private void showWaitingDialog() {
        HomeActivity activity = (HomeActivity) getActivity();
        if (activity == null) {
            return;
        }
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        WaitingDialog waitingDialog = (WaitingDialog) getActivity().getSupportFragmentManager().findFragmentByTag("waiting_info");
        if (waitingDialog != null) {
            fragmentTransaction.remove(waitingDialog);
        }
        waitingDialog = WaitingDialog.newInstance("正在连接对方");
        waitingDialog.show(fragmentTransaction, "waiting_info");
    }

    //关闭等待对话框，不要修改tag，要确保和showWaitingDialog（）中tag一致
    private void closeWaitingDialog() {
        HomeActivity activity = (HomeActivity) getActivity();
        if (activity == null) {
            return;
        }
        WaitingDialog waitingDialog = (WaitingDialog) activity.getSupportFragmentManager().findFragmentByTag("waiting_info");
        if (waitingDialog != null) {
            waitingDialog.dismiss();
        }
    }
}


