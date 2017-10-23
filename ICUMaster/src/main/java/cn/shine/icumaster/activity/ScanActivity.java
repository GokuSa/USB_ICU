package cn.shine.icumaster.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.Locale;

import cn.shine.icumaster.R;
import cn.shine.icumaster.bean.ChatDevice;
import cn.shine.icumaster.bean.ChatRoom;
import cn.shine.icumaster.engine.RoomManager;
import cn.shine.icumaster.util.TalkManager_Exp;
import cn.shine.icumaster.widget.AudioBar;

import static cn.shine.icumaster.util.AudioUtil.getVolume;

public class ScanActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "ScanActivity";
    private static final int MSG_PLAY_LIVE = 1;
    private static final int MSG_START_TALK = 2;
    private static final int MSG_STOP_TALK = 3;
    private static final int MSG_ENABLE_MAC_BUTTON = 4;
    private static final int MSG_SHOW_TOOLS_BAR = 5;
    //    private SurfaceView sv_main;
    // private SurfaceView sv_sub;
    private String url;
    //    private LivePlayer livePlayer;
    private Button btn_mac;
    private Button btn_interrupt;
    private Button btn_exchange;
    private Button btn_exit;
    private Button btn_audio_up;
    private Button btn_audio_down;
    //    private AudioMIX phone;
    private boolean isMacOpen;
    private ChatRoom mRoom;
    private ChatDevice mDevice;
    private TextView tv_scan_time;
    private TextView tv_audio;
    private AudioBar ab;
    private VideoView mVideoView;
    private MediaPlayer mMediaPlayer;
    private ScanHandler mHandler;
    private TalkManager_Exp mTalkManager = new TalkManager_Exp();
    private LinearLayout mToolsBar;

    private class ScanHandler extends Handler {
        public ScanHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLAY_LIVE:
                    showVideo();
                    break;
                case MSG_START_TALK:
                    if (!TextUtils.isEmpty(mRoom.audioAddress)) {
                        mTalkManager.setCanTalk(false);
                        mTalkManager.startTalkProcess(mRoom.audioAddress);
                    }
                    break;
                case MSG_STOP_TALK:
                    mTalkManager.stopTalkProcess();
                    break;
                case MSG_SHOW_TOOLS_BAR:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mToolsBar.setVisibility(View.VISIBLE);
                        }
                    });
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sacn);
        findView();
        registerListener();
        HandlerThread handlerThread = new HandlerThread("work");
        handlerThread.start();
        mHandler = new ScanHandler(handlerThread.getLooper());
        mTalkManager.initialize();
        mTalkManager.closeTelephoneSpeak();
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_TOOLS_BAR, 8 * 1000);
        init();


        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.android.server.PhoneWindowManager.action.EXTKEYEVENT");
        intentFilter.addAction("com.android.timeup");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    protected void findView() {
        mVideoView = (VideoView) findViewById(R.id.scan_main);
        tv_scan_time = (TextView) findViewById(R.id.scan_time_tv);
        tv_audio = (TextView) findViewById(R.id.scan_audio_tv);
        ab = (AudioBar) findViewById(R.id.ab);
        mToolsBar = (LinearLayout) findViewById(R.id.ll_tools);
        btn_mac = (Button) findViewById(R.id.scan_mac);
        btn_interrupt = (Button) findViewById(R.id.scan_interrupt);
        btn_exchange = (Button) findViewById(R.id.scan_exchange);
        btn_exit = (Button) findViewById(R.id.scan_exit);
        btn_audio_up = (Button) findViewById(R.id.scan_audio_up);
        btn_audio_down = (Button) findViewById(R.id.scan_audio_down);
    }

    @Override
    protected void registerListener() {
        btn_mac.setOnClickListener(this);
        btn_interrupt.setOnClickListener(this);
        btn_exchange.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
        btn_audio_up.setOnClickListener(this);
        btn_audio_down.setOnClickListener(this);
    }

    @Override
    protected void init() {
        Intent intent = getIntent();
        int id = intent.getIntExtra("id", -1);
        mDevice = getDevice(id);
        mRoom = getRoom(mDevice.roomId);
        //临时计策
        url = String.format(Locale.CHINA, "%s?dec=hard?mode=quene?cache=300?fps=25", mDevice.videoAddress);

        if (url.contains("shine_av_stream://")) {
            url = url.replaceAll("shine_av_stream://", "shine_net://tcp");
        }
        mRoom.timer.setTv(tv_scan_time);
        mHandler.sendEmptyMessage(MSG_PLAY_LIVE);

    }

    //
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int scanCode = intent.getIntExtra("scanCode", 0);
            //电话机监听 scanCode: 185挂机   186摘机
            Log.d(TAG, "Action: " + intent.getAction() + " scanCode: " + scanCode);
            if (scanCode == 185) {
            } else if (scanCode == 186) {
            } else if (139 == scanCode) {
            }
            Log.d(TAG, "get broadcast");
            stopPlayStream();
            mTalkManager.stopTalkProcess();
            finish();
        }
    };

    private void showVideo() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        LibVLC mLibVLC = new LibVLC(this);
        Media media = new Media(mLibVLC, Uri.parse(url));
        media.setHWDecoderEnabled(true, true);
        mMediaPlayer = new MediaPlayer(media);

        IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.setVideoView(mVideoView);
        vlcVout.setWindowSize(dm.widthPixels, dm.heightPixels);
//        vlcVout.setWindowSize(dm.widthPixels, (int)(dm.widthPixels*0.75f));
        vlcVout.attachViews();

        mMediaPlayer.setVideoTrackEnabled(true);
        mMediaPlayer.setEventListener(mEventListener);

        mMediaPlayer.play();

    }

    private void stopPlayStream() {
        Log.d(TAG, "stopPlayStream() called");
        if (mMediaPlayer != null) {
            mMediaPlayer.setEventListener(null);
            mMediaPlayer.getVLCVout().detachViews();
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
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
                        mHandler.sendEmptyMessageDelayed(MSG_PLAY_LIVE, 3000);
                    } else {
                        // TODO: 2017/8/25 show dialog fail to play video
                    }
                    break;
                case MediaPlayer.Event.Playing:
                    count = 3;
                    Log.d(TAG, "playing");
                    mHandler.sendEmptyMessage(MSG_START_TALK);
                    break;

            }
        }
    };

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        mTalkManager.exit();
        mHandler.removeCallbacks(null);
        mHandler.getLooper().quit();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_audio_up:
                audioUp();
                break;
            case R.id.scan_audio_down:
                audioDown();
                break;
            case R.id.scan_mac:
                changeMacState();
                btn_mac.setEnabled(false);
                Toast.makeText(this, "5秒后可再次切换", Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btn_mac.setEnabled(true);
                    }
                }, 5 * 1000);
                break;
            case R.id.scan_interrupt:
//                stopPlayStream();
//                mTalkManager.stopTalkProcess();
                interrupt();
                break;
            case R.id.scan_exchange:

                break;
            case R.id.scan_exit:
                stopPlayStream();
                mTalkManager.stopTalkProcess();
                finish();
                break;
        }
    }

    //停止陈功后退出
    private void interrupt() {
        RoomManager.getInstance().stopRoom(getApplicationContext(), mVQueue, mRoom.id);
    }

    private void changeMacState() {
        if (isMacOpen) {
            btn_mac.setBackgroundResource(R.drawable.mac_close);
        } else {
            btn_mac.setBackgroundResource(R.drawable.mac_open);
        }
        isMacOpen = !isMacOpen;
        mTalkManager.resetTalkMode(isMacOpen);
    }

    private void audioDown() {
        ab.down();
        int volume = getVolume(this);
        Log.d(TAG, "volume:" + volume);
        tv_audio.setText(String.valueOf(volume));
        mTalkManager.setVolume(volume * 2);

    }

    private void audioUp() {
        ab.up();
        int volume = getVolume(this);
        Log.d(TAG, "volume:" + volume);
        tv_audio.setText(String.valueOf(volume));
        mTalkManager.setVolume(volume * 2);
//        tv_audio.setText(getVolume(this) + "");
    }


}
