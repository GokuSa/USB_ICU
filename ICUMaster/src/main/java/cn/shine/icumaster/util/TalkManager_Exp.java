package cn.shine.icumaster.util;


import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import cn.shine.icumaster.MyApplication;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 增辉WebRtc,由底层talk 进程 执行音频采集 发送 接收 播放 等相关一切操作
 * 上层应用仅负责开启 关闭，异常处理
 * 通话类型为通过服务器转发，也有点对点 启动参数不一样
 * 启动talk前，先发送停止命令，防止重复调用导致多次连接
 * 启动后，通过UDP通信，开始监听底层talk反馈，
 * 不论是talk意外退出还是上层应用主动让talk退出，都返回无差别quit消息，上层应用需区分并负责意外退出时重新启动
 * <p>
 * <p>
 * 需要添加 <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>权限
 * <p>
 * <p>
 * 注意 如果传给talk的地址和端口不正确，目前无法获取启动结果
 * <p>
 * 使用RxJava 接受talk发来的音量数据，其作为talk存活的依据，如果对话过程中没有收到音量数据3秒内会重启talk
 *
 *
 * 使用HandlerThread 在子线程处理音频操作 ，也方便外部类使用
 *
 * 音频有板载的声卡和USB麦克风，其talk启动参数不一样，需要手动设置，默认是板载声卡
 * <p>
 *
 */

public class TalkManager_Exp {
    private static final String TAG = TalkManager_Exp.class.getSimpleName();
    private Handler mTalkHandler;
    //对UDP端口12310的订阅
    private Disposable mDisposable;

    /**
     * talk是否已启用
     */
    private volatile boolean isTalking = false;
    /**
     * 启动talk进程的命令行工具
     */
    private RootCommand mRootCommand = new RootCommand();


    /**
     * 监听12310UDP端口与talk通讯的socket
     */
    private DatagramSocket mSocket;
    /**
     * 连接服务器的地址和端口
     */
    private String mServerIpAndPort;
    /**
     * 麦克风是否已开启
     */
    private boolean mCanTalk=true;

    /**
     * 子线程处理耗时操作，结合Handler控制时延
     */
    private HandlerThread mHandlerThread;


    public TalkManager_Exp() {
        setUpBackgroundThread();
    }

    private void setUpBackgroundThread() {
        mHandlerThread = new HandlerThread("audio");
        mHandlerThread.start();
        mTalkHandler = new Handler(mHandlerThread.getLooper());

    }

    private void finishBackgroundThread() {
        if (mTalkHandler != null) {
            mTalkHandler.getLooper().quit();
            mTalkHandler.removeCallbacksAndMessages(null);
            mTalkHandler = null;
        }

        if (mHandlerThread != null) {
            try {
                mHandlerThread.join();
                mHandlerThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否使用的外置USB MIC（1920*X） 还是板载的声卡（1600*X）
     * 宽度为1920的使用usb 麦克风，启动talk的命令不一样
     * 提供对话框供工程人员选择 0--板载声卡 默认  1 usb麦克风
     *
     */
    private boolean isUSBMic() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        int audio_input_type = preferences.getInt("audio_input_type", 0);
        return audio_input_type!=0;
    }

    //修改当前麦克风状态，CanTalk能说能听，否则静音；
    public void setCanTalk(boolean canTalk) {
        mCanTalk = canTalk;
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
                sendStopOrder();
                // 开始监听talk 的通知，
                byte[] buffer = new byte[64];
                DatagramPacket datagramPacketReceive = new DatagramPacket(buffer, 0, buffer.length);
                //取消订阅，接到消息receive不再阻塞才会退出循环
                while (!e.isDisposed()) {
                    //阻塞
                    mSocket.receive(datagramPacketReceive);
//                    String result = new String(buffer);
                    e.onNext("");
                }
                Log.d(TAG, "end of listening");
                sendStopOrder();
//                关闭socket，停止与talk服务的通信
                mSocket.close();
                Log.d(TAG, "finish talk");
            }
        }).subscribeOn(Schedulers.io())
                //1秒钟采样一次
                .sample(1, TimeUnit.SECONDS)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String result) throws Exception {
                        //在对话过程中只要收到音频数据就每次都移除重启命令并重新设置，所以没有收到数据，重启就会执行
                        if (isTalking) {
                            mTalkHandler.removeCallbacks(mRestartRunnable);
                            mTalkHandler.postDelayed(mRestartRunnable, 3000);
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.d(TAG, "throwable:" + throwable.toString());
                    }
                });

    }
    //AudioManager 设置音量失败 ，使用底层的tinymix  13表示板载麦克风
    public void setVolume(final int value) {
        mTalkHandler.post(new Runnable() {
            @Override
            public void run() {
                mRootCommand.executeCommands(String.format(Locale.CHINA, "tinymix 13 %d &", value));
            }
        });
    }
    /**
     * 启动talk进程
     */
    public void startTalkProcess(final String serverIpAndPort) {
        mTalkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isTalking) {
                    Log.d(TAG, "talk is starting");
                    return;
                }
                isTalking = true;
                //重启talk进程使用
                mServerIpAndPort = serverIpAndPort;
                //是否静音
                String mute = mCanTalk ? "" : "-silent";
                //-ic 参数表示录音卡号，usb麦克风为2，默认为0 每次启动都需要判断，可能使用过程修改
                int ic = isUSBMic() ? 2 : 0;
                String commmand = String.format(Locale.CHINA, "talk -m %s -ic %d -il 1 %s -ns 1 -agc 1 &", serverIpAndPort, ic, mute);
                //启动talk进程，command中含&，加&是把命令交给linux内核去运行一个进程任务，在后台运行
                Log.d(TAG, "startTalkProcess " + commmand);
                mRootCommand.executeCommands(commmand);
            }
        });


    }

    //同步时间，需要系统签名
    public void synLocalTime() {
        Log.d(TAG, "synLocalTime: ");
//        checkThread("需要在子线程中调用");
        mTalkHandler.post(new Runnable() {
            @Override
            public void run() {
                mRootCommand.executeCommands("busybox hwclock -f /dev/rtc1 -w", "busybox hwclock -f /dev/rtc0 -w");
            }
        });
    }

    /**
     * 重启talk
     * 在对话过程如果没有收到talk的音量数据 就会3秒内重启
     */

    private Runnable mRestartRunnable=new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "restart");
            //talk异常终止，此时标记依然是isTalking依然是true，如果要重启 需要先修改标记为false
            isTalking = false;
            startTalkProcess(mServerIpAndPort);
        }
    };
    /**
     * 退出talk进程
     * 只要不exit，还能重新启动
     *
     */
    public void stopTalkProcess() {
        Log.d(TAG, "stopTalkProcess() called");
        //移除重启命令
        mTalkHandler.removeCallbacks(mRestartRunnable);
        mTalkHandler.post(new Runnable() {
            @Override
            public void run() {
               sendStopOrder();
            }
        });
    }

    /**
     * 在静音和有声间切换
     * 改变启动参数，关闭当前talk，3s后根据新参数重启talk
     * @param canTalk
     */
    public void resetTalkMode(boolean canTalk) {
        mCanTalk = canTalk;
        mTalkHandler.post(new Runnable() {
            @Override
            public void run() {
                sendStopOrder();
            }
        });
    }

    //关闭电话机的板载声卡,打开听筒关开
    public void closeTelephoneSpeak() {
        mTalkHandler.post(new Runnable() {
            @Override
            public void run() {
                mRootCommand.executeCommands("tinymix 14 1 &"/*,"tinymix 112 1 &"*/);
            }
        });
    }


    private void sendStopOrder() {
        if (mSocket != null) {
            isTalking = false;
            String send = "quit";
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                DatagramPacket datagramPacket = new DatagramPacket(send.getBytes(), send.length(), localHost, 12300);
                mSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        mTalkHandler.post(new Runnable() {
            @Override
            public void run() {
                isTalking=false;
                if (mDisposable != null) {
                    mDisposable.dispose();
                    mDisposable = null;
                }
//                由于UDP接受是传统的阻塞式，dispose后不能退出，所以模拟talk发退出命令
                try {
                    stopListen(12310);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mRootCommand.close();
                finishBackgroundThread();
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
        DatagramSocket socket = new DatagramSocket();
        String send = "exit";
        InetAddress localHost = InetAddress.getLocalHost();
        DatagramPacket datagramPacket = new DatagramPacket(send.getBytes(), send.length(), localHost, port);
        socket.send(datagramPacket);
        socket.close();
    }
}
