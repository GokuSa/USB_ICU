package cn.shine.icumaster.engine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 32* 1024
 * 
 * @author 蔡玮
 * @version 2013_5_22 更改：判断xml如果没有标签，就不写入数据库
 */
public class UpToShowClientSyncInfoThread extends Thread {
	
	private static final int BUFFER_SIZE = 32 * 1024;
	private byte[] buffer = new byte[BUFFER_SIZE];

	@Override
	public void run() {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < 32768; i++) {
			if (i == 1 || i == 3) {
				str.append(":");
			} else {
				str.append("0");
			}

		}
		String string = str.toString();
		socketMethod(string);

	}

	/**
	 * socket传递，udp
	 * 
	 * @param str
	 */
	private void socketMethod(String str) {
		DatagramSocket udpSocket = null;
		DatagramPacket dataPacket = null;
		try {
			dataPacket = new DatagramPacket(buffer, BUFFER_SIZE);
			byte[] out = str.getBytes(); // 把传输内容分解成字节
			dataPacket.setData(out);
			dataPacket.setLength(BUFFER_SIZE);
			dataPacket.setPort(5002);
			InetAddress broadcastAddr = InetAddress.getLocalHost();
			dataPacket.setAddress(broadcastAddr);
			udpSocket = new DatagramSocket();
			// 进行udp发送
			udpSocket.send(dataPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
