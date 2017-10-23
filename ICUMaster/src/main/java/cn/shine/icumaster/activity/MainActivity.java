package cn.shine.icumaster.activity;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cn.shine.icumaster.Global;
import cn.shine.icumaster.R;
import cn.shine.icumaster.bean.ChatDevice;
import cn.shine.icumaster.bean.ChatRoom;
import cn.shine.icumaster.engine.DeviceManager;
import cn.shine.icumaster.engine.RoomManager;
import cn.shine.icumaster.net.ChatRoomInfo;
import cn.shine.icumaster.net.DeviceInfo;
import cn.shine.icumaster.net.ResponsListener;
import cn.shine.icumaster.net.StringUTF8Request;
import cn.shine.icumaster.util.BitmapUtil;
import cn.shine.icumaster.util.Common;
import cn.shine.icumaster.util.DensityUtil;
import cn.shine.icumaster.util.IniReaderNoSection;
import cn.shine.icumaster.util.Logger;
import cn.shine.icumaster.util.PromptUtil;
import cn.shine.icumaster.util.RootCommand;
import cn.shine.icumaster.widget.MarqueeVerticalTextview;

public class MainActivity extends BaseActivity implements OnClickListener,
        OnHierarchyChangeListener {
    private static final String TAG = "MainActivity";
    public static final String PATH_LOGO = "/extdata/work/show/system/logo.png";
    private PopupWindow pw;
    private RelativeLayout rl_main;
    private LinearLayout ll_family;
    private LinearLayout ll_patient;
    private LinearLayout ll_chat_room;
    private LayoutInflater mInflater;
    private ViewGroup[] mSelView = new ViewGroup[2];
    private ImageView[] mSelImageView = new ImageView[2];
    private TextView tv_patient_null;
    private TextView tv_family_null;
    private TextView tv_room_null;
    private HorizontalScrollView hsv_patient;
    private HorizontalScrollView hsv_family;
    private HorizontalScrollView hsv_room;
    private ImageView iv_refresh;
    private PatientDeviceListener mPatientDeviceListener;
    private FamilyDeviceListener mFamilyDeviceListener;
//    private String[] times = new String[]{"10分钟", "20分钟", "30分钟", "60分钟",};
    private String[] times = new String[]{"5分钟", "10分钟", "15分钟",
        "20分钟","25分钟", "30分钟", "35分钟", "40分钟","45分钟", "50分钟", "55分钟", "60分钟"};
//    private int[] timesInt = new int[]{10 * 60 * 1000, 20 * 60 * 1000, 30 * 60 * 1000, 60 * 60 * 1000};
    private int[] timesInt = new int[]{5 * 60 * 1000, 10 * 60 * 1000,15 * 60 * 1000, 20 * 60 * 1000, 25 * 60 * 1000,
        30 * 60 * 1000, 35* 60 * 1000,40 * 60 * 1000, 45 * 60 * 1000,
       50 * 60 * 1000, 55 * 60 * 1000, 60 * 60 * 1000};
    private int time_index = 2;
    private ChatRoom mWaitAddRoom;
    private boolean isCreateAnimationEnd;
    private Object mLock = new Object();
    private CheckBox mCb_record;

    private Handler mBackgroundHandler;
    private HandlerThread mBackGroundThread;
    private String mHeartBeatingUrl="";
    private volatile boolean mStartHeartBeating = true;
    private RootCommand mRootCommand=new RootCommand();

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            MarqueeVerticalTextview tv = (MarqueeVerticalTextview) msg.obj;
            tv.startMarquee(MarqueeVerticalTextview.MODE_SCROLL);
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBackGroudThread();
        startHeartBeating();
        findView();
        registerListener();
        init();
    }

    private void startHeartBeating() {
        getLocalDeviceParameter();
       /* if (Common.isNetworkAvailable(this)) {
        } else {
            Toast.makeText(this, "没有网络连接", Toast.LENGTH_SHORT).show();
        }*/
        //无论有没有网络都发心跳，如果没网 ip地址会无效
        mBackgroundHandler.post(mHeartBeatingRunnable);
    }

    /**
     * 网络状态监测
     */
    private BroadcastReceiver mNetWorkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新本地IP参数
            getLocalDeviceParameter();
           /* if (Common.isNetworkAvailable(context)) {
            }else{
                Toast.makeText(MainActivity.this, "没有网络连接", Toast.LENGTH_SHORT).show();
            }*/
        }
    };

//获取本地IP参数，分有网络和无网络两种情况
    private void getLocalDeviceParameter() {
        IniReaderNoSection ini = new IniReaderNoSection("/extdata/work/show/system/network.ini");
        String ipAddress = Common.getIpAddress();
//        String ipAddress = ini.getValue("ip");
        if (!TextUtils.isEmpty(ipAddress)) {
            mHeartBeatingUrl = String.format(Locale.CHINA, "http://%s/interface/fresh/fresh.php?ip=%s", ini.getValue("commuip"), ipAddress);
            Global.LOCAL_IP=ipAddress;
        } else {
            Toast.makeText(this, "没有网络连接", Toast.LENGTH_SHORT).show();
            mHeartBeatingUrl = String.format(Locale.CHINA, "http://%s/interface/fresh/fresh.php?ip=%s", ini.getValue("commuip"), ini.getValue("ip"));
            Global.LOCAL_IP=ini.getValue("ip");
            Log.e(TAG, "getLocalDeviceParameter: no ip address");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
        refreshInfo();
    }

    @Override
    protected void findView() {
        ll_family = (LinearLayout) findViewById(R.id.ll_family);
        ll_patient = (LinearLayout) findViewById(R.id.ll_patient);
        ll_chat_room = (LinearLayout) findViewById(R.id.ll_chat_room);
        rl_main = (RelativeLayout) findViewById(R.id.rl_main);

        tv_patient_null = (TextView) findViewById(R.id.tv_patient);
        tv_family_null = (TextView) findViewById(R.id.tv_family);
        tv_room_null = (TextView) findViewById(R.id.tv_chat_room);

        hsv_patient = (HorizontalScrollView) findViewById(R.id.hsv_patient);
        hsv_family = (HorizontalScrollView) findViewById(R.id.hsv_family);
        hsv_room = (HorizontalScrollView) findViewById(R.id.hsv_room);

        iv_refresh = (ImageView) findViewById(R.id.main_refresh_iv);

        ImageView logo = (ImageView) findViewById(R.id.iv_logo);
        Glide.with(this)
                .load(PATH_LOGO)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.logo3)
                .into(logo);

    }

    @Override
    protected void registerListener() {
        iv_refresh.setOnClickListener(this);
        findViewById(R.id.room_name_tv).setOnClickListener(this);
        ll_patient.setOnHierarchyChangeListener(this);
        ll_family.setOnHierarchyChangeListener(this);
        ll_chat_room.setOnHierarchyChangeListener(this);
    }

    @Override
    protected void init() {
//        new UpToShowClientSyncInfoThread().start();
        mInflater = LayoutInflater.from(this);
        mFamilyDeviceListener = new FamilyDeviceListener();
        mPatientDeviceListener = new PatientDeviceListener();
        mSelImageView[0] = new ImageView(this);
        mSelImageView[1] = new ImageView(this);
        rl_main.addView(mSelImageView[0]);
        rl_main.addView(mSelImageView[1]);
        getDevice();
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



    private void setupBackGroudThread() {
        mBackGroundThread = new HandlerThread("worker");
        mBackGroundThread.start();
        mBackgroundHandler = new Handler(mBackGroundThread.getLooper());
    }

    private void stopBackGroundThread() {
        if (mBackgroundHandler != null) {
            mBackgroundHandler.removeCallbacksAndMessages(null);
            mBackgroundHandler.getLooper().quit();
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
            StringUTF8Request request = new StringUTF8Request(mHeartBeatingUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String info) {
                    try {
                        long time = Long.parseLong(info);
                        if (Math.abs(System.currentTimeMillis() - time) > 4 * 1000) {
                            if (time > 0 && time / 1000 < Integer.MAX_VALUE) {
                                ((AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE)).setTime(time);
                                mBackgroundHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRootCommand.executeCommands("busybox hwclock -f /dev/rtc1 -w", "busybox hwclock -f /dev/rtc0 -w");
                                    }
                                });
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.d(TAG, volleyError.toString());
                }
            });
            mVQueue.add(request);
            if (mStartHeartBeating) {
                mBackgroundHandler.postDelayed(mHeartBeatingRunnable, 10 * 1000);
            }
        }
    };

    private void getDevice() {
        DeviceInfo deviceInfo = new DeviceInfo(
                new ResponsListener<Map<Integer, ChatDevice>>() {

                    @Override
                    public void onSuccess(Map<Integer, ChatDevice> t) {
                        if (t == null) {
                            getChatRoom();
                            return;
                        }
                        //Log.e("jiangcy", "onSuccess: t "+t.toString());
                        DeviceManager.getInstance().setAllDevice(t);
                        Set<Integer> keySet = getAllDevice().keySet();
                        Log.d(TAG, "onSuccess: keySet " + keySet.toString());
                        keySet = sortSetByKey(keySet);
                        Log.d(TAG, "onSuccess: keySet " + keySet.toString());
                        for (Integer id : keySet) {
                            ChatDevice device = getAllDevice().get(id);
                            Log.d(TAG, "onSuccess: device " + device.name + "   id " + id);
                            if (device.roomId != -1) {
                                continue;
                            }
                            ViewGroup view = (ViewGroup) mInflater.inflate(R.layout.device, null);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    DensityUtil.dip2px(getApplicationContext(), 156), DensityUtil.dip2px(getApplicationContext(), 112));
//                                    DensityUtil.dip2px(getApplicationContext(), 194), DensityUtil.dip2px(getApplicationContext(), 152));
                            params.rightMargin = 5;
                            view.setLayoutParams(params);
                            view.setBackgroundColor(0);
                            view.setTag(device);
                            MarqueeVerticalTextview tv = (MarqueeVerticalTextview) view
                                    .findViewById(R.id.room_name_tv);
                            Message msg = Message.obtain();
                            msg.obj = tv;
                            mHandler.sendMessageDelayed(msg, 2000);
                            tv.setMaxLines(1);
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                            tv.setTextColor(Color.WHITE);
                            tv.setText(device.name);
                            if (device.type == 0) {
                                view.getChildAt(0).setBackgroundResource(R.drawable.device_family_bg);
                                ll_family.addView(view);
                                view.setOnClickListener(mFamilyDeviceListener);
                            } else {
                                view.getChildAt(0).setBackgroundResource(R.drawable.device_patient_bg);
                                ll_patient.addView(view);
                                view.setOnClickListener(mPatientDeviceListener);
                            }
                        }
                        getChatRoom();
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                        PromptUtil.showToastAtCenter(getApplicationContext(),
                                "获取设备信息失败");
                        onRefreshEnd();
                    }
                });
        deviceInfo.getAllDevice(mVQueue, Global.LOCAL_IP);
    }

    /**
     * 使用 Map按key进行排序
     *
     * @param map
     * @return
     */
    public static Set<Integer> sortSetByKey(Set<Integer> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Set<Integer> sortMap = new TreeSet<Integer>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer integer, Integer t1) {
                        return integer.compareTo(t1);
                    }
                });

//        sortMap.putAll(map);
        sortMap.addAll(map);
        return sortMap;
    }

    private void getChatRoom() {
        ChatRoomInfo<List<ChatRoom>> chatRoomInfo = new ChatRoomInfo<List<ChatRoom>>(
                new ResponsListener<List<ChatRoom>>() {

                    @Override
                    public void onSuccess(List<ChatRoom> t) {
                        if (t == null) {
                            onRefreshEnd();
                            return;
                        }
                        HashMap<Integer, ChatRoom> mAllRoom = new HashMap<Integer, ChatRoom>();
                        for (ChatRoom chatRoom : t) {
                            List<Integer> devicesIds = chatRoom.devicesIds;
                            for (Integer id : devicesIds) {
                                if (getAllDevice().containsKey(id)) {
                                    getAllDevice().get(id).roomId = chatRoom.id;
                                }
                            }
                            mAllRoom.put(chatRoom.id, chatRoom);
                        }
                        RoomManager.getInstance().setAllRoom(mAllRoom);
                        for (Integer id : mAllRoom.keySet()) {
                            newRoom(getRoom(id));
                        }
                        onRefreshEnd();
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                        PromptUtil.showToastAtCenter(getApplicationContext(),
                                "获取房间信息失败");
                        onRefreshEnd();
                    }
                });
        chatRoomInfo.getAllRoom(mVQueue, Global.LOCAL_IP);
    }

    private void newRoom(ChatRoom chatRoom) {
        View view = mInflater.inflate(R.layout.room2, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                DensityUtil.dip2px(this, 380), DensityUtil.dip2px(this, 182));
                DensityUtil.dip2px(this, 280), DensityUtil.dip2px(this, 124));
        params.rightMargin = 25;
        view.setLayoutParams(params);
        view.setTag(chatRoom.id);
        view.setOnClickListener(this);
        MarqueeVerticalTextview tv = (MarqueeVerticalTextview) view
                .findViewById(R.id.room_name_tv);
        Message msg = Message.obtain();
        msg.obj = tv;
        mHandler.sendMessageDelayed(msg, 2000);
        TextView tv_time = (TextView) view.findViewById(R.id.room_time_tv);
        TextView tv_patient = (TextView) view
                .findViewById(R.id.room_patient_tv);
        TextView tv_family = (TextView) view.findViewById(R.id.room_family_tv);
        TextView tv_start = (TextView) view.findViewById(R.id.room_start_tv);
        TextView tv_stop = (TextView) view.findViewById(R.id.room_stop_tv);
        TextView tv_delete = (TextView) view.findViewById(R.id.room_delete_tv);
        chatRoom.tv = tv_time;
        tv_time.setText("");
        startCountTime(chatRoom, tv_time);
        Button scanPat = (Button) view.findViewById(R.id.room_scan_patient_btn);
        Button scanFam = (Button) view.findViewById(R.id.room_scan_family_btn);
        Button btn_start = (Button) view.findViewById(R.id.room_start_btn);
        Button btn_stop = (Button) view.findViewById(R.id.room_stop_btn);
        Button btn_delete = (Button) view.findViewById(R.id.room_delete_btn);
        tv.setMaxLines(1);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tv.setTextColor(Color.WHITE);
        tv.setText(chatRoom.name);
        scanPat.setTag(-1);
        scanFam.setTag(-1);
        tv_patient.setTag(-1);
        tv_family.setTag(-1);
        for (int id : chatRoom.devicesIds) {
            if (!getAllDevice().containsKey(id)) {
                continue;
            }
            ChatDevice chatDevice = getAllDevice().get(id);
            if (chatDevice.type == 0) {
                tv_family.setText(chatDevice.name);
                tv_family.setTag(chatDevice.id);
                scanFam.setTag(chatDevice.id);
            } else {
                tv_patient.setText(chatDevice.name);
                tv_patient.setTag(chatDevice.id);
                scanPat.setTag(chatDevice.id);
            }
        }
        OnClickListener l = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ScanActivity.class);
                int tag = (Integer) v.getTag();
                if (!getAllDevice().containsKey(tag)) {
                    PromptUtil.showToastAtCenter(getApplicationContext(),
                            "设备异常，无法开启监控");
                    return;
                }
                ChatDevice device = getDevice(tag);
                ChatRoom room = getRoom(device.roomId);
                if (room.isStop) {
                    PromptUtil.showToastAtCenter(getApplicationContext(),
                            "房间没有开始探视");
                    return;
                }
                intent.putExtra("id", tag);
                startActivity(intent);
            }
        };
        tv_family.setOnClickListener(l);
        tv_patient.setOnClickListener(l);
        scanFam.setOnClickListener(l);
        scanPat.setOnClickListener(l);
        btn_start.setTag(chatRoom.id);
        tv_start.setTag(chatRoom.id);
        OnClickListener clickStartListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = (Integer) v.getTag();
                ChatRoom chatRoom = getRoom(id);
                if (!chatRoom.isStop) {
                    PromptUtil.showToastAtCenter(getApplicationContext(),
                            "房间已经开启对讲");
                    return;
                }
                if (chatRoom.isEnd) {
                    showStartRoomSetting(v, chatRoom);
                } else {
                    Log.d("MainActivity", "send start");
                    sendStartRoomRequest(chatRoom);
                }
            }

        };
        btn_start.setOnClickListener(clickStartListener);
        tv_start.setOnClickListener(clickStartListener);
        btn_stop.setTag(chatRoom.id);
        tv_stop.setTag(chatRoom.id);
        OnClickListener clickStopListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = (Integer) v.getTag();
                RoomManager.getInstance().stopRoom(getApplicationContext(),
                        mVQueue, id);
            }
        };
        btn_stop.setOnClickListener(clickStopListener);
        tv_stop.setOnClickListener(clickStopListener);
        btn_delete.setTag(chatRoom.id);
        tv_delete.setTag(chatRoom.id);
        OnClickListener clickDeleteListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = (Integer) v.getTag();
                ChatRoom chatRoom = getRoom(id);
                if (!chatRoom.isStop) {
                    PromptUtil.showToastAtCenter(getApplicationContext(),
                            "请先停止房间");
                    return;
                }
                ChatRoomInfo<View> chatRoomInfo = new ChatRoomInfo<View>(
                        (View) v.getParent().getParent(),
                        new ResponsListener<View>() {
                            @Override
                            public void onSuccess(View t) {
                                ll_chat_room.removeView(t);
                                refreshInfo();
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                e.printStackTrace();
                                PromptUtil.showToastAtCenter(
                                        getApplicationContext(), "删除房间失败");
                            }

                        });
                chatRoomInfo.deleteRoom(mVQueue, chatRoom);
            }
        };
        btn_delete.setOnClickListener(clickDeleteListener);
        tv_delete.setOnClickListener(clickDeleteListener);
        ll_chat_room.addView(view, 0);
    }

    private void sendStartRoomRequest(ChatRoom chatRoom) {
        Log.d(TAG, "sendStartRoomRequest() called with: chatRoom = ");
        ChatRoomInfo<List<ChatRoom>> chatRoomInfo = new ChatRoomInfo<List<ChatRoom>>(
                (TextView) chatRoom.tv,
                (new ResponsListener<List<ChatRoom>>() {

                    @Override
                    public void onSuccess(List<ChatRoom> t) {
                        if (t.size() != 1) {
                            onFailure(new Exception(
                                    "start room fail, size != 1"));
                            return;
                        }
                        ChatRoom chatRoom = t.get(0);
                        chatRoom.isStop = false;
                        startCountTime(chatRoom, (TextView) chatRoom.tv);
                        getAllRoom().remove(chatRoom.id);
                        getAllRoom().put(chatRoom.id, chatRoom);
                        PromptUtil.showToastAtCenter(getApplicationContext(), "开始房间成功");
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                        PromptUtil.showToastAtCenter(
                                getApplicationContext(), "开始房间失败");
                    }
                }));
        chatRoomInfo.startRoom(mVQueue, chatRoom);
//        chatRoomInfo.startRoom(chatRoom);
    }

    private void onAnimEnd() {
        for (int i = 0; i < 2; i++) {
            ViewGroup view = mSelView[i];
            if (i == 0) {
                ll_family.removeView(view);
                synchronized (mLock) {
                    isCreateAnimationEnd = true;
                    if (mWaitAddRoom != null) {
                        newRoom(mWaitAddRoom);
                        mWaitAddRoom = null;
                    }
                }
            } else {
                ll_patient.removeView(view);
            }
            mSelView[i] = null;
            // mSelImageView[i].setVisibility(View.GONE);
        }
    }
    private void playNewRoomAnim() {
        for (int i = 0; i < 2; i++) {
            ViewGroup view = mSelView[i];
            if (i == 0) {
                view.setBackgroundColor(0);
                view.getChildAt(0).setBackgroundResource(R.drawable.device_family_sel);
            }

            Bitmap bitmap = BitmapUtil.getBitmapFromView(view);
            if (bitmap != null) {
                Log.d(TAG, bitmap.toString());
            }
            ImageView iv = mSelImageView[i];
//            ImageView iv = new ImageView(this);
            iv.setImageBitmap(bitmap);
           /* if (i == 0) {
                iv.setImageResource(R.drawable.device_family_sel);
            }else{
                iv.setImageResource(R.drawable.device_patient_sel);
            }*/
            iv.setVisibility(View.VISIBLE);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            WindowManager wm = getWindowManager();
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            Rect outRect = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    view.getWidth(), view.getHeight());
            params.setMargins(location[0] - rl_main.getPaddingLeft(),
                    location[1] - rl_main.getPaddingTop() - outRect.top,
                    outMetrics.widthPixels - location[0] - view.getWidth()
                            - rl_main.getPaddingLeft(),
                    outMetrics.heightPixels - location[1] - view.getHeight()
                            - rl_main.getPaddingTop());
            iv.setLayoutParams(params);
            int[] ll_location = new int[2];
            hsv_room.getLocationOnScreen(ll_location);
            TranslateAnimation ta = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE,
                    ll_location[0] - location[0], Animation.RELATIVE_TO_SELF,
                    0, Animation.ABSOLUTE, ll_location[1] - location[1]);
            ta.setDuration(2000);
            AlphaAnimation aa1 = new AlphaAnimation(0.9999999f, 1f);
            aa1.setDuration(1);
            AlphaAnimation aa2 = new AlphaAnimation(1f, 0f);
            aa2.setDuration(500);
            aa2.setStartOffset(1500);
            AnimationSet as = new AnimationSet(false);
            as.addAnimation(ta);
            as.addAnimation(aa1);
            as.addAnimation(aa2);
            as.setFillAfter(true);
            as.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    isCreateAnimationEnd = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    for (int i = 0; i < 2; i++) {
                        ViewGroup view = mSelView[i];
                        if (i == 0) {
                            ll_family.removeView(view);
                            synchronized (mLock) {
                                isCreateAnimationEnd = true;
                                if (mWaitAddRoom != null) {
                                    newRoom(mWaitAddRoom);
                                    mWaitAddRoom = null;
                                }
                            }
                        } else {
                            ll_patient.removeView(view);
                        }
                        mSelView[i] = null;
                        // mSelImageView[i].setVisibility(View.GONE);
                    }
//                    releaseBitmap();
                }
            });
            iv.startAnimation(as);
            view.setVisibility(View.INVISIBLE);
        }
    }

    private void releaseBitmap() {
        if (mSelImageView!=null) {
            for (int i = 0; i < mSelImageView.length; i++) {
                Drawable drawable= mSelImageView[i].getDrawable();
                if(drawable instanceof BitmapDrawable){
                    Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
                    if (bmp != null && !bmp.isRecycled()){
                        mSelImageView[i].setImageBitmap(null);
                        bmp.recycle();
                        bmp=null;
                    }
                }
            }
        }
    }

    private void createRoom(int time, boolean record) {
        isCreateAnimationEnd = false;
        ChatRoomInfo<List<ChatRoom>> chatRoomInfo = new ChatRoomInfo<List<ChatRoom>>(
                new ResponsListener<List<ChatRoom>>() {

                    @Override
                    public void onSuccess(List<ChatRoom> t) {
                        if (t.size() != 1) {
                            onFailure(new Exception("create room fail, size != 1"));
                            return;
                        }
                        synchronized (mLock) {
                            ChatRoom chatRoom = t.get(0);
                            RoomManager.getInstance().addRoom(chatRoom);
                            if (isCreateAnimationEnd) {
                                newRoom(chatRoom);
                            } else {
                                mWaitAddRoom = chatRoom;
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                        PromptUtil.showToastAtCenter(getApplicationContext(),
                                "创建房间失败");
                    }
                });
        int pid = -1;
        int fid = -1;
        for (View view : mSelView) {
            ChatDevice device = (ChatDevice) view.getTag();
            if (device.type == 0) {
                fid = device.id;
            } else {
                pid = device.id;
            }
        }
        chatRoomInfo.createRoom(mVQueue, pid, fid, time, record);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_refresh_iv:
            case R.id.room_name_tv:
                refreshInfo();
                break;
        }
    }

    private void refreshInfo() {
        iv_refresh.setClickable(false);
        Map<Integer, ChatRoom> allRoom = RoomManager.getInstance().getAllRoom();
        if (allRoom != null) {
            Set<Integer> keySet = allRoom.keySet();
            for (int id : keySet) {
                ChatRoom room = allRoom.get(id);
                if (room.timer != null) {
                    room.timer.cancel();
                }
            }
        }
        ll_patient.removeAllViews();
        ll_family.removeAllViews();
        ll_chat_room.removeAllViews();
        getDevice();
    }

    private final class PatientDeviceListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mSelView[1] != null) {
                mSelView[1].getChildAt(0).setBackgroundResource(
                        R.drawable.device_patient_bg);
            }
            ViewGroup vp = (ViewGroup) v;
            vp.getChildAt(0).setBackgroundResource(
                    R.drawable.device_patient_sel);
            mSelView[1] = (ViewGroup) v;
        }

    }

    private final class FamilyDeviceListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mSelView[1] == null) {
                PromptUtil.showToastAtCenter(getApplicationContext(),
                        getString(R.string.patient_device_null), 30);
                return;
            }
            v.setBackgroundResource(R.drawable.device_family_sel_bg);
            mSelView[0] = (ViewGroup) v;
            showNewRoomSetting(v);
        }


    }

    private void showNewRoomSetting(View v) {
        View view = mInflater.inflate(R.layout.room_setting, null);
        mCb_record = (CheckBox) view
                .findViewById(R.id.room_setting_record_cb);
        TextView tv = (TextView) view
                .findViewById(R.id.room_setting_record_tv);
        TextView tv_time = (TextView) view
                .findViewById(R.id.room_setting_time_tv);
        Button btn_up = (Button) view
                .findViewById(R.id.room_setting_time_up_btn);
        Button btn_down = (Button) view
                .findViewById(R.id.room_setting_time_down_btn);
        Button btn = (Button) view
                .findViewById(R.id.room_setting_confirm_btn);
        time_index = 2;
        tv_time.setText(times[time_index]);
        mCb_record.setTag(tv);
        tv.setTag(mCb_record);
        btn_up.setTag(tv_time);
        btn_down.setTag(tv_time);
        OnClickListener time_listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.room_setting_time_up_btn:
                        ++time_index;
                        break;
                    case R.id.room_setting_time_down_btn:
                        --time_index;
                        break;
                }
                if (time_index < 0) {
                    time_index = times.length - 1;
                }
                if (time_index > times.length - 1) {
                    time_index = 0;
                }
                TextView tv = (TextView) v.getTag();
                tv.setText(times[time_index]);
            }
        };
        btn_up.setOnClickListener(time_listener);
        btn_down.setOnClickListener(time_listener);
        tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.getTag();
                cb.performClick();
            }
        });
        mCb_record
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        TextView tv = (TextView) buttonView.getTag();
                        if (isChecked) {
                            tv.setText(R.string.room_setting_open);
                        } else {
                            tv.setText(R.string.room_setting_close);
                        }
                    }
                });
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick() called with: v =  create new room + ");
                createRoom(timesInt[time_index], mCb_record.isChecked());
//                playNewRoomAnim();
                onAnimEnd();
                pw.dismiss();
            }
        });
        pw = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);
        pw.setOutsideTouchable(true);
        pw.setBackgroundDrawable(new ColorDrawable(0));
        pw.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    pw.dismiss();
                    return true;
                }
                return false;
            }
        });
        pw.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
//                mSelView[0].setBackgroundColor(0);
            }
        });
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        pw.showAtLocation(v, Gravity.NO_GRAVITY,
                location[0] + v.getWidth(), location[1] + v.getHeight() / 2);
    }

    private void showStartRoomSetting(View v, final ChatRoom oldRoom) {
        View view = mInflater.inflate(R.layout.room_setting, null);
        mCb_record = (CheckBox) view
                .findViewById(R.id.room_setting_record_cb);
        TextView tv = (TextView) view
                .findViewById(R.id.room_setting_record_tv);
        TextView tv_time = (TextView) view
                .findViewById(R.id.room_setting_time_tv);
        Button btn_up = (Button) view
                .findViewById(R.id.room_setting_time_up_btn);
        Button btn_down = (Button) view
                .findViewById(R.id.room_setting_time_down_btn);
        Button btn = (Button) view
                .findViewById(R.id.room_setting_confirm_btn);
        time_index = 2;
        tv_time.setText(times[time_index]);
        mCb_record.setTag(tv);
        tv.setTag(mCb_record);
        btn_up.setTag(tv_time);
        btn_down.setTag(tv_time);
        btn.setText("开始");
        OnClickListener time_listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.room_setting_time_up_btn:
                        ++time_index;
                        break;
                    case R.id.room_setting_time_down_btn:
                        --time_index;
                        break;
                }
                if (time_index < 0) {
                    time_index = times.length - 1;
                }
                if (time_index > times.length - 1) {
                    time_index = 0;
                }
                TextView tv = (TextView) v.getTag();
                tv.setText(times[time_index]);
            }
        };
        btn_up.setOnClickListener(time_listener);
        btn_down.setOnClickListener(time_listener);
        tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v.getTag();
                cb.performClick();
            }
        });
        mCb_record
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        TextView tv = (TextView) buttonView.getTag();
                        if (isChecked) {
                            tv.setText(R.string.room_setting_open);
                        } else {
                            tv.setText(R.string.room_setting_close);
                        }
                    }
                });
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                oldRoom.duration = timesInt[time_index];
                oldRoom.isRecord = mCb_record.isChecked() ? 1 : 0;
                Log.d("MainActivity", "sendStartRoomRequest");
                sendStartRoomRequest(oldRoom);
                pw.dismiss();
            }
        });
        pw = new PopupWindow(view, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);
        pw.setOutsideTouchable(true);
        pw.setBackgroundDrawable(new ColorDrawable(0));
        pw.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    pw.dismiss();
                    return true;
                }
                return false;
            }
        });
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        pw.showAtLocation(v, Gravity.NO_GRAVITY,
                location[0] + v.getWidth(), location[1] + v.getHeight() / 2 - 75);
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        ViewGroup vp = (ViewGroup) parent;
        Logger.v("add count: " + vp.getChildCount());
        if (vp.getChildCount() != 1) {
            return;
        }
        switch (parent.getId()) {
            case R.id.ll_family:
                tv_family_null.setVisibility(View.GONE);
                hsv_family.setBackgroundResource(R.drawable.device_bg);
                break;
            case R.id.ll_patient:
                tv_patient_null.setVisibility(View.GONE);
                hsv_patient.setBackgroundResource(R.drawable.device_bg);
                break;
            case R.id.ll_chat_room:
                tv_room_null.setVisibility(View.GONE);
                hsv_room.setBackgroundResource(R.drawable.device_bg);
                break;
        }
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        ViewGroup vp = (ViewGroup) parent;
        Logger.v("remove count: " + vp.getChildCount());
        if (vp.getChildCount() > 1) {
            return;
        }
        switch (parent.getId()) {
            case R.id.ll_family:
                tv_family_null.setVisibility(View.VISIBLE);
                hsv_family.setBackgroundResource(R.drawable.device_bg_null);
                break;
            case R.id.ll_patient:
                tv_patient_null.setVisibility(View.VISIBLE);
                hsv_patient.setBackgroundResource(R.drawable.device_bg_null);
                break;
            case R.id.ll_chat_room:
                tv_room_null.setVisibility(View.VISIBLE);
                hsv_room.setBackgroundResource(R.drawable.device_bg_null);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStartHeartBeating=false;
        stopBackGroundThread();
        mRootCommand.close();
    }

    private void onRefreshEnd() {
        iv_refresh.setClickable(true);
    }

}
