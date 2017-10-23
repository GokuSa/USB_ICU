package cn.shine.icumaster.net;

import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.shine.icumaster.bean.ChatRoom;
import cn.shine.icumaster.statics.IStatics;
import cn.shine.icumaster.util.FileRWEngine;
import cn.shine.icumaster.util.Logger;

public class ChatRoomInfo<T> {
    private static final String TAG = "ChatRoomInfo";
    private ResponsListener<T> listener;
    private View view;
    private ChatRoom mChatRoom;
    public ChatRoomInfo(ChatRoom chatRoom, ResponsListener<T> listener) {
        super();
        this.listener = listener;
        this.mChatRoom = chatRoom;
    }

    public ChatRoomInfo(View view, ResponsListener<T> listener) {
        super();
        this.listener = listener;
        this.view = view;
    }

    public ChatRoomInfo(ResponsListener<T> listener) {
        super();
        this.listener = listener;
    }

    public void getAllRoomTest() {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType type = mapper.getTypeFactory().constructCollectionType(
                ArrayList.class, ChatRoom.class);
        try {
            String readFile = FileRWEngine.readFile(IStatics.TEST_GET_ROOM_URL);
            List<ChatRoom> readValue = mapper.readValue(readFile, type);
            listener.onSuccess((T) readValue);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e);
        }
    }
    public void getAllRoom(RequestQueue mVQueue,String ip) {
        RequestParams params = new RequestParams();
        params.put("nursingIP", ip + "");
        String url = IStatics.GET_ROOM_URL;
        url = AsyncHttpClient.getUrlWithQueryString(true, url, params);
        StringUTF8Request request = new StringUTF8Request(
                url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.v("room info: " + response);
                if (response.equals(IStatics.NO_ERROR)) {
                    listener.onSuccess(null);
                    return;
                }
                ObjectMapper mapper = new ObjectMapper();
                CollectionType type = mapper.getTypeFactory()
                        .constructCollectionType(ArrayList.class,
                                ChatRoom.class);
                try {
                    List<ChatRoom> readValue = mapper.readValue(
                            response, type);
                    listener.onSuccess((T) readValue);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFailure(error);
            }
        });
        mVQueue.add(request);
    }

    public void getAllRoom(RequestQueue mVQueue) {
        StringUTF8Request request = new StringUTF8Request(
                IStatics.GET_ROOM_URL, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                Logger.v("room info: " + response);
                if (response.equals(IStatics.NO_ERROR)) {
                    listener.onSuccess(null);
                    return;
                }
                ObjectMapper mapper = new ObjectMapper();
                CollectionType type = mapper.getTypeFactory()
                        .constructCollectionType(ArrayList.class,
                                ChatRoom.class);
                try {
                    List<ChatRoom> readValue = mapper.readValue(
                            response, type);
                    listener.onSuccess((T) readValue);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFailure(error);
            }
        });
        mVQueue.add(request);
    }

    public void startRoomTest(ChatRoom chatRoom) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<ChatRoom> list = new ArrayList<ChatRoom>();
            String readFile = FileRWEngine
                    .readFile(IStatics.TEST_START_ROOM_URL);
            ChatRoom readValue = mapper.readValue(readFile, ChatRoom.class);
            readValue.tv = view;
            list.add(readValue);
            listener.onSuccess((T) list);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e);
        }
    }

    public void startRoom(RequestQueue mVQueue, ChatRoom chatRoom) {
        RequestParams params = new RequestParams();
        params.put("room_id", chatRoom.id + "");
        params.put("duration", chatRoom.duration + "");
        params.put("record", chatRoom.isRecord + "");
        String url = IStatics.START_ROOM_URL;
        url = AsyncHttpClient.getUrlWithQueryString(true, url, params);

        StringUTF8Request request = new StringUTF8Request(url,
                new Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            List<ChatRoom> list = new ArrayList<ChatRoom>();
                            ChatRoom readValue = mapper.readValue(response,ChatRoom.class);
                            readValue.tv = view;
                            list.add(readValue);
                            listener.onSuccess((T) list);

                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onFailure(e);
                        }
                    }
                }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFailure(error);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 1, 1.0f));
        mVQueue.add(request);

    }

    public void startRoom(ChatRoom chatRoom) {
        Log.d(TAG, "startRoom: one params");
        RequestParams params = new RequestParams();
        params.put("room_id", chatRoom.id + "");
        params.put("duration", chatRoom.duration + "");
        params.put("record", chatRoom.isRecord + "");
       final String url = AsyncHttpClient.getUrlWithQueryString(true, IStatics.START_ROOM_URL, params);

            new Thread(){
                @Override
                public void run() {
                    BufferedInputStream inputStream=null;
                    ByteArrayOutputStream os=null;
                    try {
                        URL url2 = new URL(url);
                        HttpURLConnection urlConnection = (HttpURLConnection) url2.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setReadTimeout(10000);
                        urlConnection.setConnectTimeout(10000);
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        os = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len=-1;
                        while ((len = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0,len);
                        }
                        String response = os.toString();
                        Log.d(TAG, "response "+response);
                        ObjectMapper mapper = new ObjectMapper();

                            List<ChatRoom> list = new ArrayList<ChatRoom>();
                            ChatRoom readValue = mapper.readValue(response, ChatRoom.class);
                            readValue.tv = view;
                            list.add(readValue);
                            listener.onSuccess((T) list);

                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailure(e);
                        Log.e(TAG, "createRoom: ioexception");
                    }finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }.start();

    }
    public void stopRoomTest(ChatRoom chatRoom) {
        String readFile = "-1";
        try {
            readFile = FileRWEngine.readFile(IStatics.TEST_STOP_ROOM_URL);
        } catch (Exception e) {
            listener.onFailure(e);
        }
        if (readFile.equals("0\n")) {
            List<ChatRoom> list = new ArrayList<ChatRoom>();
            list.add(mChatRoom);
            listener.onSuccess((T) list);
            return;
        } else {
            listener.onFailure(new Exception("stop room fail, code: "));
        }
    }

    public void stopRoom(RequestQueue mVQueue, ChatRoom chatRoom) {
        RequestParams params = new RequestParams();
        params.put("room_id", chatRoom.id + "");
        params.put("record", chatRoom.isRecord + "");
        String url = IStatics.STOP_ROOM_URL;
        url = AsyncHttpClient.getUrlWithQueryString(true, url, params);

        StringUTF8Request request = new StringUTF8Request(url,
                new Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            List<ChatRoom> list = new ArrayList<ChatRoom>();
                            ChatRoom readValue = mapper.readValue(response,
                                    ChatRoom.class);
                            if (readValue.isStop) {
                                mChatRoom.isStop = readValue.isStop;
                                mChatRoom.isEnd = readValue.isEnd;
                                list.add(mChatRoom);
                                listener.onSuccess((T) list);
                            } else {
                                listener.onFailure(new Exception("stop room failed"));
                            }
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onFailure(e);
                        }
                    }
                }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFailure(error);
            }
        });
        mVQueue.add(request);
    }

    public void deleteRoomTest(ChatRoom chatRoom) {
        String readFile = "-1";
        try {
            readFile = FileRWEngine.readFile(IStatics.TEST_DELETE_ROOM_URL);
        } catch (Exception e) {
            listener.onFailure(e);
        }
        if (readFile.equals("0\n")) {
            listener.onSuccess((T) view);
            return;
        } else {
            listener.onFailure(new Exception("delete room fail, code: "));
        }
    }

    public void deleteRoom(RequestQueue mVQueue, ChatRoom chatRoom) {
        RequestParams params = new RequestParams();
        params.put("room_id", chatRoom.id + "");
        String url = IStatics.DELETE_ROOM_URL;
        url = AsyncHttpClient.getUrlWithQueryString(true, url, params);

        StringUTF8Request request = new StringUTF8Request(url,
                new Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("0")) {
                            listener.onSuccess((T) view);
                            return;
                        } else {
                            listener.onFailure(new Exception(
                                    "delete room fail, code: " + response));
                        }
                    }
                }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFailure(error);
            }
        });
        mVQueue.add(request);
    }

    public void getAllRoom() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(IStatics.GET_ROOM_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {
                listener.onFailure(error);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] responseBody) {
                ObjectMapper mapper = new ObjectMapper();
                CollectionType type = mapper.getTypeFactory()
                        .constructCollectionType(ArrayList.class,
                                ChatRoom.class);
                try {
                    String string = new String(responseBody);
                    List<ChatRoom> readValue = mapper.readValue(string, type);
                    listener.onSuccess((T) readValue);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        });
    }

    public void createRoomTest(int pid, int fid, int duration, boolean record) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String readFile = FileRWEngine
                    .readFile(IStatics.TEST_CREATE_ROOM_URL);
            List<ChatRoom> list = new ArrayList<ChatRoom>();
            ChatRoom readValue = mapper.readValue(readFile, ChatRoom.class);
            list.add(readValue);
            listener.onSuccess((T) list);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e);
        }
    }

    public void createRoom(RequestQueue mVQueue, int pid, int fid,
                           int duration, final boolean record)  {
        Log.d("ChatRoomInfo", "createRoom() called with: mVQueue = ]");
        RequestParams params = new RequestParams();
        params.put("device_id", pid + "|" + fid);
        params.put("duration", duration + "");
        params.put("record", (record ? 1 : 0) + "");
        String url = IStatics.CREATE_ROOM_URL;
        url = AsyncHttpClient.getUrlWithQueryString(true, url, params);
        final String startRecord=AsyncHttpClient.getUrlWithQueryString(true, IStatics.START_RECORD_URL, params);

        StringUTF8Request request = new StringUTF8Request(url,
                new Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("ChatRoomInfo", response);
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            List<ChatRoom> list = new ArrayList<ChatRoom>();
                            ChatRoom readValue = mapper.readValue(response, ChatRoom.class);
                            list.add(readValue);
                            listener.onSuccess((T) list);
                            //创建成功后请求录制
                            if (record) {
                                request(startRecord);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onFailure(e);
                        }
                    }
                }, new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onFailure(error);
            }
        });

        mVQueue.add(request);
    }

//    基本的http请求
    private void request(final String url) {
        new Thread(){
            @Override
            public void run() {
                BufferedInputStream inputStream=null;
                ByteArrayOutputStream os=null;
                try {
                    URL url2 = new URL(url);
                    HttpURLConnection urlConnection = (HttpURLConnection) url2.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(5000);
                    urlConnection.setConnectTimeout(10000);
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    os = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len=-1;
                    while ((len = inputStream.read(buffer)) != -1) {
                        os.write(buffer, 0,len);
                    }
                    String result = os.toString();
                    Log.d(TAG, "result "+result);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "createRoom: ioexception");
                }finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }.start();
    }
    public void createRoom(int pid, int fid, int duration, boolean record) {
        RequestParams params = new RequestParams();
        params.put("device_id", pid + "|" + fid);
        params.put("duration", duration + "");
        params.put("record", (record ? 1 : 0) + "");

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(IStatics.CREATE_ROOM_URL, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        listener.onFailure(error);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          byte[] responseBody) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String string = new String(responseBody);
                            List<ChatRoom> list = new ArrayList<ChatRoom>();
                            ChatRoom readValue = mapper.readValue(string,
                                    ChatRoom.class);
                            list.add(readValue);
                            listener.onSuccess((T) list);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onFailure(e);
                        }
                    }
                });
    }

    public List<ChatRoom> getDebugInfo() {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        arrayList.add(1);
        arrayList.add(2);
        ArrayList<ChatRoom> list = new ArrayList<ChatRoom>();
        ChatRoom room1 = new ChatRoom();
        room1.id = 1;
        room1.name = "a-1";
        room1.audioAddress = "";
        room1.devicesIds = arrayList;
        room1.duration = 30 * 60 * 1000;
        room1.startTime = 13 * 1000;
        room1.endTime = room1.startTime + room1.duration;
        room1.isRecord = 0;
        ChatRoom room2 = new ChatRoom();
        room2.id = 1;
        room2.name = "b-2";
        room2.audioAddress = "";
        room2.devicesIds = arrayList;
        room2.duration = 30 * 60 * 1000;
        room2.startTime = 23 * 1000;
        room2.endTime = room2.startTime + room2.duration;
        room2.isRecord = 0;
        ChatRoom room3 = new ChatRoom();
        room3.id = 3;
        room3.name = "c-3";
        room3.audioAddress = "";
        room3.devicesIds = arrayList;
        room3.duration = 30 * 60 * 1000;
        room3.startTime = 33 * 1000;
        room3.endTime = room3.startTime + room3.duration;
        room3.isRecord = 1;

        list.add(room1);
        list.add(room2);
        list.add(room3);
        return list;
    }
}
