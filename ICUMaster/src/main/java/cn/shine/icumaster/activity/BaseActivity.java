package cn.shine.icumaster.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Calendar;
import java.util.Map;

import cn.shine.icumaster.bean.ChatDevice;
import cn.shine.icumaster.bean.ChatRoom;
import cn.shine.icumaster.engine.DeviceManager;
import cn.shine.icumaster.engine.LeftTimeCountDownTimer;
import cn.shine.icumaster.engine.RoomManager;
import cn.shine.icumaster.statics.IStatics;
import cn.shine.icumaster.util.Logger;

public abstract class BaseActivity extends Activity {

    protected static final boolean DEBUG = IStatics.DEBUG;
    protected static final boolean UI_DEBUG = IStatics.UI_DEBUG;
    protected RequestQueue mVQueue;

    protected abstract void findView();

    protected abstract void registerListener();

    protected abstract void init();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVQueue = Volley.newRequestQueue(this);

    }

    protected String[] getDeviceIpAndPort(int id) {
        ChatDevice device = getDevice(id);
        String[] split = device.videoAddress.split("@");
        return split[1].split(":");
    }

    protected ChatDevice getDevice(int id) {
        Map<Integer, ChatDevice> allDevice = DeviceManager.getInstance()
                .getAllDevice();
        return allDevice.get(id);
    }

    protected Map<Integer, ChatDevice> getAllDevice() {
        return DeviceManager.getInstance().getAllDevice();
    }

    protected void setRoom(ChatRoom chatRoom) {
        RoomManager.getInstance().getAllRoom().put(chatRoom.id, chatRoom);
    }

    protected ChatRoom getRoom(int id) {
        Map<Integer, ChatRoom> allRoom = RoomManager.getInstance().getAllRoom();
        return allRoom.get(id);
    }

    protected Map<Integer, ChatRoom> getAllRoom() {
        return RoomManager.getInstance().getAllRoom();
    }

    protected void startCountTime(ChatRoom chatRoom, final TextView tv) {
        if (chatRoom.isStop) {
            return;
        }
        long time = chatRoom.endTime - Calendar.getInstance().getTimeInMillis();
        Logger.d(chatRoom.name + " left time: " + time);
        LeftTimeCountDownTimer start = new LeftTimeCountDownTimer(time + 1000, 1000, tv, chatRoom) {
            @Override
            public void onFinish() {

                RoomManager.getInstance().stopRoom(BaseActivity.this, mVQueue, getChatRoom().id);
            }
        };
        start.start();
        chatRoom.timer = start;
        chatRoom.isStop = false;
    }
}
