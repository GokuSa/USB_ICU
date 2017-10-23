package cn.shine.icumaster.engine;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.shine.icumaster.activity.ScanActivity;
import cn.shine.icumaster.bean.ChatRoom;
import cn.shine.icumaster.net.ChatRoomInfo;
import cn.shine.icumaster.net.ResponsListener;
import cn.shine.icumaster.util.PromptUtil;

public class RoomManager {

    private Map<Integer, ChatRoom> mAllRoom;

    public Map<Integer, ChatRoom> getAllRoom() {
        return mAllRoom;
    }

    public void setAllRoom(Map<Integer, ChatRoom> mAllRoom) {
        this.mAllRoom = mAllRoom;
    }

    private static class RoomManagerHolder {
        private static final RoomManager mInstance = new RoomManager();
    }

    public static RoomManager getInstance() {
        return RoomManagerHolder.mInstance;
    }

    private RoomManager() {
        init();
    }

    private void init() {
        mAllRoom = new HashMap<Integer, ChatRoom>();
    }

    public void addRoom(ChatRoom room) {
        mAllRoom.put(room.id, room);
        for (int id : room.devicesIds) {
            DeviceManager.getInstance().getDevice(id).roomId = room.id;
        }
    }

    public void removeRoom(ChatRoom room) {
        mAllRoom.remove(room.id);
        for (int id : room.devicesIds) {
            DeviceManager.getInstance().getDevice(id).roomId = -1;
        }
    }

    public void stopRoom(final Context context, RequestQueue mVQueue, int id) {
        ChatRoom chatRoom = mAllRoom.get(id);
        if (chatRoom.isStop) {
            return;
        }
        ChatRoomInfo<List<ChatRoom>> chatRoomInfo = new ChatRoomInfo<List<ChatRoom>>(
                chatRoom, new ResponsListener<List<ChatRoom>>() {

            @Override
            public void onSuccess(List<ChatRoom> t) {
                ChatRoom chatRoom = t.get(0);
                if (chatRoom.timer != null) {
                    chatRoom.timer.cancel();
                }
                Log.d("RoomManager", "send broadcast ");
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.android.timeup"));
                PromptUtil.showToastAtCenter(context, "停止房间成功");
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                PromptUtil.showToastAtCenter(context, "停止房间失败");
            }
        });
        chatRoomInfo.stopRoom(mVQueue, chatRoom);
    }

    public void stopRoomInScan(final Context context, RequestQueue mVQueue, int id, final ScanActivity activity) {
        ChatRoom chatRoom = mAllRoom.get(id);
        if (chatRoom.isStop) {
            return;
        }
        ChatRoomInfo<List<ChatRoom>> chatRoomInfo = new ChatRoomInfo<List<ChatRoom>>(
                chatRoom, new ResponsListener<List<ChatRoom>>() {

            @Override
            public void onSuccess(List<ChatRoom> t) {
                ChatRoom chatRoom = t.get(0);
                if (chatRoom.timer != null) {
                    chatRoom.timer.cancel();
                }

                PromptUtil.showToastAtCenter(context, "停止房间成功");
                activity.finish();

            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                PromptUtil.showToastAtCenter(context, "停止房间失败");
            }
        });
        chatRoomInfo.stopRoom(mVQueue, chatRoom);
    }

}
