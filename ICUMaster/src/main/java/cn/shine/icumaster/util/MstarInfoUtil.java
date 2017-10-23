package cn.shine.icumaster.util;

import java.io.FileNotFoundException;

public class MstarInfoUtil {
	private static final String TAG = "MstarInfoUtil";
	private static final String SERVER_FILE = "/extdata/work/show/system/network.ini";
	private static String ftpAddress;
	private static String ftpPort;
	private static String ftpName;
	private static String ftpPassword;
	private static boolean getInfoSuccess;
	private static String serverAddress;
	private static String serverPort;

	public static String getServerPort() {
		if (!getInfoSuccess) {
			return null;
		}
		return serverPort;
	}

	public static String getFtpAddress() {
		if (!getInfoSuccess) {
			return null;
		}
		return ftpAddress;
	}

	public static String getFtpPort() {
		if (!getInfoSuccess) {
			return null;
		}
		return ftpPort;
	}

	public static String getFtpName() {
		if (!getInfoSuccess) {
			return null;
		}
		return ftpName;
	}

	public static String getFtpPassword() {
		if (!getInfoSuccess) {
			return null;
		}
		return ftpPassword;
	}

	public static String getServerAddress() {
		if (!getInfoSuccess) {
			return null;
		}
		return serverAddress;
	}





	public static boolean isMSTB() {
		String string = "";
		try {
			string = FileRWEngine.readFile("/data/shineVersion");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (string != null && string.contains("MSTB")) {
			return true;
		}
		return false;
	}



	public static void initServiceInfo() {
		try {
			String readFile = FileRWEngine.readFile(SERVER_FILE);
			serverAddress = readFile.substring(
					readFile.indexOf("commuip=") + 8,
					readFile.indexOf("\n", readFile.indexOf("commuip=")))
					.trim();
			serverPort = readFile.substring(
					readFile.indexOf("commuport=") + 10,
					readFile.indexOf("\n", readFile.indexOf("commuport=")))
					.trim();
			ftpAddress = readFile.substring(readFile.indexOf("ftpip=") + 6,
					readFile.indexOf("\n", readFile.indexOf("ftpip="))).trim();
			ftpPort = readFile.substring(readFile.indexOf("ftpport=") + 8,
					readFile.indexOf("\n", readFile.indexOf("ftpport=")))
					.trim();
			ftpName = readFile.substring(readFile.indexOf("ftpusr=") + 7,
					readFile.indexOf("\n", readFile.indexOf("ftpusr="))).trim();
			ftpPassword = readFile.substring(
					readFile.indexOf("ftppasswd=") + 10,
					readFile.indexOf("\n", readFile.indexOf("ftppasswd=")))
					.trim();

			getInfoSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			getInfoSuccess = false;
		}
	}

}
