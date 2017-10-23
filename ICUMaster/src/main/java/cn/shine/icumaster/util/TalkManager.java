package cn.shine.icumaster.util;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 增辉WebRtc,由底层talk 进程 执行音频采集 发送 接收 播放 等相关一切操作
 * 上层应用仅负责开启 关闭，异常处理
 * 通话类型为通过服务器转发，也有点对点 启动参数不一样
 * 启动talk前，先发送停止命令，防止重复调用导致多次连接
 * 启动后，通过UDP通信，开始监听底层talk反馈，
 * 根据talk返回的音量信息判断talk是否还存活
 * <p>
 * talk进程和android应用占用声卡问题，无法播放音频，
 * 解决方案：底层修改声卡使用，在使用talk进程之前，禁止android程序使用；talk退出，恢复声卡使用
 * 需要添加 <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>权限
 * <p>
 * <p>
 * 切换声卡导致子码流失常问题->使用talk完成混音和转发两个功能，不再切换声卡的使用
 * <p>
 * 注意 如果传给talk的地址和端口不正确，目前无法获取启动结果
 * <p>
 * 从本地获取音频输入设备 0-麦克风 1-共达 2-桌面麦克风，默认为0
 */
@Deprecated
public class TalkManager {
    private static final String TAG = TalkManager.class.getSimpleName();
    /**
     * 麦克风设备
     */
    private static final int DEVICE_MIC = 0;
    /**
     * 共达设备
     */
    private static final int DEVICE_GONGDA = 1;
    /**
     * 桌面麦克风
     */
    private static final int DEVICE_DESKTOP_MIC = 2;
    /**
     * 没有音频输入设备
     */
    private static final int DEVICE_NONE = 3;
    /**
     * 调节声卡占用问题
     */
    //对UDP端口12310的订阅
    private Disposable mDisposable;

    /**
     * talk是否已启用
     */
    private volatile boolean isTalking = false;
    /**
     * 启动talk进程的命令行工具
     */
    private RootCommand mRootCommand=new RootCommand();
    /**
     * 当前音频输入设备代号
     */
    private int mVoiceDeviceType;
    /**
     * 监听12310UDP端口与talk通讯的socket
     */
    private DatagramSocket mSocket;
    /**
     * 连接服务器的地址和端口
     */
    private String mServerIpAndPort;

    private boolean mCanSpeak;
    private Handler mBackgroundHandler;
    public TalkManager() {

        HandlerThread handlerThread = new HandlerThread("audio");
        handlerThread.start();
        mBackgroundHandler=new Handler(handlerThread.getLooper());

    }


    /**
     * 主要是开启UDP端口12310监听talk进程发送的退出消息，还有音频数据来动态显示声音波动
     * 在oncreate时调用，
     */
    public void initialize() {
        if (mDisposable != null) {
            return;
        }
        mDisposable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                // 监听12310端口，接受talk的通知
                Log.d(TAG, "start listen n 12310");
                mSocket = new DatagramSocket(12310, InetAddress.getLocalHost());
                //如果程序异常退出，talk可能没退出，先发送关闭进程命令
                stopTalkProcess();
                // 开始监听talk 的通知，
                byte[] buffer = new byte[64];
                DatagramPacket datagramPacketReceive = new DatagramPacket(buffer, 0, buffer.length);
                //取消订阅，接到消息receive不再阻塞才会退出循环
                while (!e.isDisposed()) {
                    //阻塞
                    mSocket.receive(datagramPacketReceive);
                    String result = new String(buffer).trim();
                    e.onNext(result);
                }
                Log.d(TAG, "end of listening");
                stopTalkProcess();
//                关闭socket，停止与talk服务的通信
                mSocket.close();
                Log.d(TAG, "finish talk");
            }
        }).subscribeOn(Schedulers.io())
                .sample(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String result) throws Exception {

                        //在对话过程中只要收到音频数据就每次都移除重启命令并重新设置
                        if (isTalking) {
                            mBackgroundHandler.removeCallbacks(mRestartRunnable);
                            mBackgroundHandler.postDelayed(mRestartRunnable, 3000);
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.d(TAG, "throwable:" + throwable.toString());
                    }
                });

    }


    private Runnable mRestartRunnable=new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "restart");
            isTalking = false;
            try {
                startTalkProcess(mServerIpAndPort, mCanSpeak);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 启动talk进程
     * 使用麦克风的时候要关闭音量增强，共达需要打开
     *
     * @param serverIpAndPort
     */
    public void startTalkProcess(String serverIpAndPort,boolean canSpeak) throws IOException {
        checkThread("启动talk进程需要在子线程中调用");
        //不能说就只能听，不会显示声音波形
        mCanSpeak = canSpeak;
        if (isTalking) {
            Log.d(TAG, "talk is starting");
            return;
        }
        isTalking = true;
        //重启talk进程使用
        mServerIpAndPort = serverIpAndPort;
        //静音参数
        String muteParam = canSpeak ? "" : "-silent";
        String commmand = String.format(Locale.CHINA, "talk -m %s -ic 0 -il 1 %s &", serverIpAndPort,muteParam);

        //启动talk进程，command中含&，加&是把命令交给linux内核去运行一个进程任务，在后台运行
        Log.d(TAG, "startTalkProcess " + commmand);
        mRootCommand.executeCommands(commmand);

    }


    /**
     * 退出talk进程
     * 只要不exit，还能重新启动
     *
     * @throws IOException
     */
    public void stopTalkProcess() throws IOException {
        checkThread("关闭talk进程需要在子线程中调用");
        Log.d(TAG, "stopTalkProcess() called");
        if (mSocket != null) {
            isTalking = false;
            String send = "quit";
            InetAddress localHost = InetAddress.getLocalHost();
            DatagramPacket datagramPacket = new DatagramPacket(send.getBytes(), send.length(), localHost, 12300);
            mSocket.send(datagramPacket);

        } else {
            Log.d(TAG, "socket is null");
        }
    }

    private void checkThread(String info) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            throw new IllegalStateException(info);
        }
    }


    /**
     * 关闭talk进程，退出talk通讯监听
     * 一般在页面销毁时条用
     */
    public void exit() {
        Log.d(TAG, "exit() called");
        Observable.just("").map(new Function<Object, Object>() {
            @Override
            public Object apply(@NonNull Object o) throws Exception {

                if (mDisposable != null) {
                    mDisposable.dispose();
                    mDisposable = null;
                }
//                由于UDP接受是传统的阻塞式，dispose后不能退出，所以模拟talk发退出命令
                stopListen(12310);
                mBackgroundHandler.getLooper().quit();
                mBackgroundHandler.removeCallbacks(null);
                mRootCommand.close();
                return "";
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object o) throws Exception {
                        Log.d(TAG, "exit on ");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.d(TAG, throwable.toString());
                    }
                });
    }


    /**
     * 必须先mDisposable.dispose()
     * 发送命令给12310端口，让其从阻塞恢复，退出监听
     * 如果不想配合mDisposable.dispose()使用，在循环中自定义处理接受到的exit信息
     *
     * @param port
     * @throws IOException
     */
    private void stopListen(int port) throws IOException {
        Log.d(TAG, "stopListen");
        checkThread("必须在子线程发送关闭命令");
        isTalking=false;
        DatagramSocket socket = new DatagramSocket();
        String send = "exit";
        InetAddress localHost = InetAddress.getLocalHost();
        DatagramPacket datagramPacket = new DatagramPacket(send.getBytes(), send.length(), localHost, port);
        socket.send(datagramPacket);
        socket.close();
    }





    public int getVoiceDeviceType() {
        return mVoiceDeviceType;
    }
}
