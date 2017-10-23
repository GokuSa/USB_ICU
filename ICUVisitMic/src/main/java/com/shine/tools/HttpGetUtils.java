package com.shine.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpGetUtils {
    private static final String TAG = "HttpGetUtils";
    private Boolean isStop = false;
    private int num = 0;
    private static HttpGetUtils instance;

    private HttpGetUtils() {

    }

    public synchronized static HttpGetUtils getInstance() {
        if (instance == null) {
            instance = new HttpGetUtils();
        }
        return instance;
    }

    public void HttpGet(final String url, final Handler handler) {
        num = 0;
        new Thread() {
            private String info = null;
            private String[] infos = null;

            @Override
            public void run() {
                while (!isStop) {
                    try {
                        if (isStop) {
                            break;
                        }
                        HttpGet httpget = new HttpGet(url);
                        HttpResponse httpResponse = new DefaultHttpClient().execute(httpget);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            info = EntityUtils.toString(httpResponse.getEntity());
                            infos = info.split("\\|");
                            for (String s : infos) {
                                Log.d(TAG, "run: " +s);
                            }
                            if (infos[0].equals("START")) {
                                Log.d(TAG, "==============   Connect to the server  ==============");
                                synchronized (isStop) {
                                    isStop.wait(3000);
                                }
                                if (isStop) {
                                    break;
                                }
                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                msg.what = Contast.STARTMEETING;
                                bundle.putStringArray("startInfo", infos);
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            }
                            isStop = true;
                            num = 0;
                            break;
                        }
                        num++;
                        Log.d(TAG, "run: num="+num);
                        if (num == 20) {
                            handler.sendEmptyMessage(Contast.DISCONNECTSEVER);
                            isStop = true;
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    public void isStop_notify() {
        isStop = true;
        synchronized (isStop) {
            isStop.notify();
        }
    }

    public Boolean getIsStop() {
        return isStop;
    }

    public void setIsStop(Boolean isStop) {
        this.isStop = isStop;
    }
}
