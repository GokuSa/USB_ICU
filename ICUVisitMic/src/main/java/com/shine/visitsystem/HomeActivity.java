package com.shine.visitsystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.shine.audio.TalkManager_Exp;
import com.shine.fragment.HomeFragment;
import com.shine.fragment.PreviewFragment;
import com.shine.fragment.SettingFragment;
import com.shine.fragment.USBCameraPreviewFragment;
import com.shine.fragment.USBCameraVideoTalkFragment;
import com.shine.fragment.VideoTalkFragment;
import com.shine.service.TCPSever;
import com.shine.tools.Common;
import com.shine.tools.Contast;
import com.shine.tools.IniReaderNoSection;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Locale;

import static com.shine.tools.Contast.STARTMEETING;
import static com.shine.tools.Contast.STOPMEETING;
import static com.shine.usbcameralib.gles.TextureMovieEncoder.mNetworkNative;

/**
 * 主页面，管理欢迎视图，预览视图，对话视图
 * 获取camera权限 降低了编译版本，不会有动态权限问题，
 * 检查摄像头有效性，是否存在，是否有网络连接
 * 编码摄像头，发送到转发服务器
 */
public class HomeActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final String TAG = "HomeActivity";
    public static final String PATH_LOGO = "/extdata/work/show/system/logo.png";
    public static final String PATH_BG = "/extdata/work/show/system/bg.png";
    private TalkManager_Exp mTalkManager = new TalkManager_Exp();
    private Handler mBackgroundHandler;

    private HandlerThread mBackGroundThread;
    private String mServerIpAndPort = "172.16.11.110:7000";

    private String mHeartBeatingUrl = "";
    private volatile boolean mStartHeartBeating = true;
    private String mRoomInfoUrl;

    private boolean isVideoTalkStarted = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what:" + msg.what);
            switch (msg.what) {
                case STOPMEETING:
                    isVideoTalkStarted = false;
                    TCPSever.getInstance().nitifyLock(false);
                    if (isUsbCamera()) {
                        USBCameraVideoTalkFragment fragment = (USBCameraVideoTalkFragment) getSupportFragmentManager().findFragmentByTag("video_talk");
                        if (fragment != null) {
                            fragment.stopRecording();
                        }
                    }else{
                        VideoTalkFragment fragment = (VideoTalkFragment) getSupportFragmentManager().findFragmentByTag("video_talk");
                        if (fragment != null) {
                            fragment.stopRecording();
                        }
                    }
                    stopTalk();
                    postDelayed(() -> {
                        showHome();
                    }, 1500);
                    break;
                case Contast.STARTMEETING:
                    //进入视频对话
                    if (isVideoTalkStarted) {
                        Log.d(TAG, "MEETING is started");
                        return;
                    }
                    isVideoTalkStarted = true;
                    Log.d(TAG, "STARTMEETING");
                    Bundle startBundle = msg.getData();
                    String url = String.format(Locale.CHINA, "%s?dec=hard?mode=quene?cache=300?fps=25", startBundle.getString("url"));
                    mServerIpAndPort = startBundle.getString("serverIpAndPort");
                    String over_time = startBundle.getString("over_time");
                    TCPSever.getInstance().nitifyLock(true);
                    if (url.contains("shine_av_stream://")) {
                        url = url.replaceAll("shine_av_stream://", "shine_net://tcp");
                    }
                    startVideoTalk(url, over_time);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //设置后台线程
        setupBackGroudThread();
        //开始心跳并获取当前探视状态
        startHeartBeating();
        mNetworkNative.OpenSocket();
        mTalkManager.initialize();
        showHome();
        TCPSever.getInstance().startTCPServer(mHandler);

        requestCameraPermission();
    }

    //判断是否有摄像头权限
    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

//    请求摄像头权限
    private void requestCameraPermission() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() called with: requestCode = [" + requestCode + "], permissions = [" + permissions + "], grantResults = [" + grantResults + "]");
        if (requestCode == 0) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "没有摄像头权限", Toast.LENGTH_SHORT).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void startHeartBeating() {
        getLocalDeviceParameter();
        //无论有没有网络都发心跳，如果没网 ip地址会无效
        mBackgroundHandler.post(mHeartBeatingRunnable);
        mBackgroundHandler.post(mGetRoomInfoRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mNetWorkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetWorkReceiver);
    }

    /**
     * 网络状态监测
     */
    private BroadcastReceiver mNetWorkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Common.isNetworkAvailable(context)) {
                //更新网络路径
                getLocalDeviceParameter();
            } else {
                Toast.makeText(HomeActivity.this, "网络连接断开", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 设备mac参数 服务器地址
     */
    private void getLocalDeviceParameter() {
        IniReaderNoSection ini = new IniReaderNoSection("/extdata/work/show/system/network.ini");
        mRoomInfoUrl = String.format(Locale.CHINA, "http://%s/interface/getChatRoomInfo/getchatroominfo.php", ini.getValue("commuip"));
        Log.d(TAG, "mRoomInfoUrl " + mRoomInfoUrl);
        //这种方式如果断网将获取不到ip
        String ipAddress = Common.getIpAddress();
        if (!TextUtils.isEmpty(ipAddress)) {
            mHeartBeatingUrl = String.format(Locale.CHINA, "http://%s/interface/fresh/fresh.php?ip=%s", ini.getValue("commuip"), ipAddress);
        } else {
            //没网的时候使用本地存储的
            mHeartBeatingUrl = String.format(Locale.CHINA, "http://%s/interface/fresh/fresh.php?ip=%s", ini.getValue("commuip"), ini.getValue("ip"));
            Toast.makeText(this, "没有网络连接", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "getLocalDeviceParameter: no ip address");
        }
    }

    private void setupBackGroudThread() {
        mBackGroundThread = new HandlerThread("worker");
        mBackGroundThread.start();
        mBackgroundHandler = new Handler(mBackGroundThread.getLooper());
    }

    private void stopBackGroundThread() {
        if (mBackgroundHandler != null) {
            mBackgroundHandler.removeCallbacksAndMessages(null);
            mBackgroundHandler.getLooper().quitSafely();
            mBackgroundHandler = null;
            try {
                mBackGroundThread.join();
                mBackGroundThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 通知服务器设备上线 并根据返回值校时
     */
    private Runnable mHeartBeatingRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                HttpGet httpget = new HttpGet(mHeartBeatingUrl);
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpget);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    String info = EntityUtils.toString(httpResponse.getEntity());
                    Log.d(TAG, "info: " + info);
                    long time = Long.parseLong(info);
                    if (Math.abs(System.currentTimeMillis() - time) > 4 * 1000) {
                        if (time > 0 && time / 1000 < Integer.MAX_VALUE) {
                            ((AlarmManager) HomeActivity.this.getSystemService(Context.ALARM_SERVICE)).setTime(time);
                            mTalkManager.synLocalTime();
                        }
                    }
                }
            } catch (IOException | NumberFormatException e) {
                Log.e(TAG, "run: exception " + e.toString());
                e.printStackTrace();
            } finally {
                if (mStartHeartBeating) {
                    mBackgroundHandler.postDelayed(mHeartBeatingRunnable, 10 * 1000);
                }
            }
        }
    };

    /**
     * 设备启动后需要获取当前聊天室状态，如果在聊天中，需要进入
     */
    private Runnable mGetRoomInfoRunnable = new Runnable() {
        @Override
        public void run() {
            HttpGet httpget = new HttpGet(mRoomInfoUrl);
            try {
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpget);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    String response = EntityUtils.toString(httpResponse.getEntity());
                    Log.d(TAG, "room info : " + response);
                    String[] params = response.split("\\|");
                    if (params.length > 3 && "START".equals(params[0])) {
                        Message msg = new Message();
                        msg.what = STARTMEETING;
                        Bundle bundle = new Bundle();
                        bundle.putString("url", params[1]);
                        bundle.putString("serverIpAndPort", params[2]);
                        bundle.putString("over_time", params[3]);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 开启音频
     */
    public void startTalk() {
        mTalkManager.setCanTalk(true);
        mTalkManager.startTalkProcess(mServerIpAndPort);
    }

    /**
     * 开闭音频
     */
    public void stopTalk() {
        mTalkManager.stopTalkProcess();
    }

    public void setVolume(int value) {
        mTalkManager.setVolume(value);
    }

    /**
     * 重新设置音频 在静音之间切换
     */
    public void resetTalk(final boolean canTalk) {
        mTalkManager.resetTalkMode(canTalk);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mStartHeartBeating = false;
        stopBackGroundThread();
        mNetworkNative.CloseSocket();
        mTalkManager.exit();
        mHandler.removeCallbacksAndMessages(null);

    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);

    }*/

    //-------------------frgment 调用的方法 这种方式简单，但导致Activity和Fragment耦合性太强-----------------------
    public void beginPreview() {
        Log.d(TAG, "beginPreview() called");
        if (hasPermission()) {
            Fragment fragment;
            if (isUsbCamera()) {
                fragment = new USBCameraPreviewFragment();
            }else{
                fragment =  new PreviewFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }else{
            Toast.makeText(this, "没有摄像头权限", Toast.LENGTH_SHORT).show();
            requestCameraPermission();
        }
    }

    public void showHome() {
        Log.d(TAG, "showHome() called");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .commit();
    }

    public void startVideoTalk(String url, String over_time) {
        Log.d(TAG, "startVideoTalk() called");
        if (hasPermission()) {
            Fragment fragment;
            if (isUsbCamera()) {
                fragment = USBCameraVideoTalkFragment.newInstance(url, over_time);
            }else{
                fragment = VideoTalkFragment.newInstance(url, over_time);
            }
            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, USBCameraVideoTalkFragment.newInstance(url, over_time), "video_talk")
                    .replace(R.id.container, fragment, "video_talk")
                    .commit();
        }else{
            Toast.makeText(this, "没有摄像头权限", Toast.LENGTH_SHORT).show();
            requestCameraPermission();
        }
    }

    private boolean isUsbCamera() {
        return PreferenceManager.getDefaultSharedPreferences(this).getInt(SettingFragment.CAMERA_TYPE,0)==0;
    }



    public void stopVideoTalk() {
        mHandler.sendEmptyMessage(STOPMEETING);
    }
}
