package com.shine.service;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.shine.tools.Contast;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PlayerTCPServer {
	
	private static PlayerTCPServer instance=null;
	private ServerSocket server = null;
	private boolean stoped = false;
	private  Handler handler= null;
	
	private PlayerTCPServer() {
		// TODO Auto-generated constructor stub
	}
	
	public static PlayerTCPServer getInstance()
	{
		if(instance==null)
		{
			instance = new PlayerTCPServer();
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
						server = new ServerSocket(33335);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
//				MyApplication.whileNum++;
				while (!isStoped()) {
					if (server != null) {
						try {
							client = server.accept();
							new ClientThread(client).start();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (isStoped() || Thread.interrupted()) {
							break;
						}
					} 
				}
//				MyApplication.whileNum--;
				if (server != null) {
					try {
						server.close();
						server = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			};
		}.start();
	}

	
	public void stopServer()
	{
		setStoped(true);
		if (server != null) {
			try {
				server.close();
				server = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		// private DataInputStream input = null;
		private byte[] revBytes = new byte[512];
		private String info = null;
		private String[] infos = null;

		public ClientThread(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			if (client != null) {
				try {
					input = client.getInputStream();
					if (input != null) {
						// input.read(revBytess);
						int length = input.read(revBytes);
						if (length > 0) {
							info = new String(revBytes, 0, length);
							if (info == null) {
								return;
							}
							String getInfo = info.substring(0,info.indexOf('|'));
							Log.d("jiangcy",getInfo);
							Message msg = new Message();
							if(getInfo.equals("disconnect"))
							{
								msg.what = Contast.DISCONNECT;
							}else if(getInfo.equals("connect")){
								msg.what =Contast.CONNECT ;
							}
							getHandler().sendMessage(msg);
						}
					} else {
						return;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (client != null) {
						try {
							client.close();
							client = null;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (input != null) {
						try {
							input.close();
							input = null;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			} else {
				return;
			}
			super.run();
		}
	}

	
}
