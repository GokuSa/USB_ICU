package cn.shine.icumaster.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import cn.shine.icumaster.util.Logger;

public class TcpSender {

	private String ip;
	private int port;
	private String msg;

	public TcpSender(String ip, int port, String msg) {
		Logger.d("send ip: " + ip + ",port: " + port + ", msg: " + msg);
		this.ip = ip;
		this.port = port;
		this.msg = msg;
	}

	public void sendMsg() {
		new Thread() {
			@Override
			public void run() {
				OutputStream os = null;
				Socket socket = null;
				try {
					socket = new Socket(ip, port);
					socket.setSoTimeout(3 * 1000);
					os = socket.getOutputStream();
					os.write(msg.getBytes());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					closeStream(os);
					closeSocket(socket);
				}
			}

		}.start();
	}

	private void closeSocket(Socket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeStream(OutputStream os) {
		try {
			if (os != null) {
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
