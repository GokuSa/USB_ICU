package com.shine.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.shine.tools.Contast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPSever {
    private static final String TAG = "TCPSever";
    public static TCPSever instance = null;
    private ServerSocket server = null;
    private boolean stoped = false;
    private Handler handler = null;
    private Boolean lock = false;

    private TCPSever() {
    }

    public static TCPSever getInstance() {
        if (null == instance) {
            instance = new TCPSever();
        }
        return instance;
    }

    public void startTCPServer(Handler handler) {
        setHandler(handler);
        new Thread() {
            @Override
            public void run() {
                Socket client = null;
                if (server == null) {
                    try {
                        server = new ServerSocket(33334);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                while (!isStoped()) {
                    if (server != null) {
                        Log.d(TAG, "server=" + server);
                        try {
                            client = server.accept();
                            Log.d(TAG, "server accept " + client);
                            new ClientThread(client).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (isStoped() || Thread.interrupted()) {
                            break;
                        }
                    }
                }
                if (server != null) {
                    try {
                        server.close();
                        server = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }

            ;
        }.start();
    }

    public void stopServer() {
        setStoped(true);
        if (server != null) {
            try {
                server.close();
                server = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean isStoped() {
        return stoped;
    }

    public void setStoped(boolean stoped) {
        this.stoped = stoped;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    class ClientThread extends Thread {
        private Socket client = null;
        private InputStream input = null;
        private OutputStream output = null;
        private byte[] revBytes = new byte[512];
        private String[] infos = null;
        private String returnStr = "0";

        public ClientThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            if (client != null) {
                try {
                    input = client.getInputStream();
                    output = client.getOutputStream();
                    if (input != null) {
                        int length = input.read(revBytes);
                        if (length > 0) {
                            String info = new String(revBytes, 0, length);
                            infos = info.split("\\|");
                            for (String s : infos) {
                                Log.d(TAG, "run: " + s);
                            }
                            Message msg = new Message();
                            if (infos[0].equals("START")) {
                                Log.d(TAG, "==   TCP START  ==============");
                                msg.what = Contast.STARTMEETING;
                                if (infos.length > 3) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("url",infos[1]);
                                    bundle.putString("serverIpAndPort",infos[2]);
                                    bundle.putString("over_time",infos[3]);
                                    msg.setData(bundle);
                                }
                            } else if (infos[0].equals("STOP")) {
                                Log.d(TAG, "==   TCP STOP  ==============");
                                msg.what = Contast.STOPMEETING;
                            }
//                            bundle.putStringArray("startInfo", infos);
                            if (handler != null) {
                                handler.sendMessage(msg);
                            }
                            synchronized (lock) {
                                try {
                                    lock.wait(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (lock) {
                                returnStr = "0";
                            } else {
                                returnStr = "1";
                            }
                            Log.d(TAG, "TCP return = " + returnStr);
                            lock = false;
                            output.write(returnStr.getBytes());
                            output.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.d(TAG, "finally");
                    if (client != null) {
                        try {
                            client.close();
                            client = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (input != null) {
                        try {
                            input.close();
                            input = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (output != null) {
                        try {
                            output.close();
                            output = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void nitifyLock(boolean isLock) {
        synchronized (lock) {
            lock.notifyAll();
            lock = isLock;
        }
    }

}
