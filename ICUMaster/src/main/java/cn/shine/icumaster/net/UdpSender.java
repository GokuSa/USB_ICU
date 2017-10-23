package cn.shine.icumaster.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import cn.shine.icumaster.engine.DeviceManager;

public class UdpSender {

	private static final int PORT = 4360;

	public static void send(final String ip, final byte[] out) {
		new Thread() {
			public void run() {
				DatagramSocket udpSocket = null;
				DatagramPacket dataPacket = null;
				try {
					dataPacket = new DatagramPacket(out, out.length);
					dataPacket.setData(out);
					dataPacket.setLength(out.length);
					dataPacket.setPort(PORT);
					InetAddress broadcastAddr = InetAddress.getByName(ip);
					dataPacket.setAddress(broadcastAddr);
					udpSocket = new DatagramSocket();
					udpSocket.send(dataPacket);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}
