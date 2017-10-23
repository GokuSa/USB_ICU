package cn.shine.icumaster;

import cn.shine.icumaster.util.AndroidInfoUtils;
import cn.shine.icumaster.util.MstarInfoUtil;

public class Global {

	public static String SERVER_URL_PRE;
	public static String LOCAL_IP;
	static {
		MstarInfoUtil.initServiceInfo();
		String address = MstarInfoUtil.getServerAddress();
		SERVER_URL_PRE = "http://" + address + "/interface/";
		LOCAL_IP= AndroidInfoUtils.getLocalIpAddress();
	}

}
