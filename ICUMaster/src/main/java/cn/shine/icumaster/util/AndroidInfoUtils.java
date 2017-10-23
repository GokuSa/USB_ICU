package cn.shine.icumaster.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: 姜春雨(1055655886@qq.com)
 * Date: 2015/8/21
 * Time: 15:45
 */
public class AndroidInfoUtils {
    private static  final String TAG = "AndroidInfoUtils";
    /**
     * 获取IP
     *
     * @return
     * @throws Exception
     */
    public static String getLocalIpAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().toString().contains(".")) {
                        Log.e(TAG,"LocalIpAddress "+inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG,"LocalIpAddress 获取IP失败");
            ex.printStackTrace();
        }
        return  null;
    }

    public static String getNoMacAddress() {
        String mac = "";
        File macFile = new File("/sys/class/net/eth0/address");
        FileInputStream fis;

        byte[] b = new byte[17];

        try {
            fis = new FileInputStream(macFile);
            fis.read(b);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fis = null;
        macFile = null;
        try {
            mac = new String(b, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        mac=mac.replace(":","");
        mac=mac.toUpperCase();
        Log.e(TAG,"mac:" + mac);
        return mac;
    }
    public  String getWireMacAddress() {
        String mac = "";
        File macFile = new File("/sys/class/net/eth0/address");
        FileInputStream fis;

        byte[] b = new byte[17];

        try {
            fis = new FileInputStream(macFile);
            fis.read(b);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fis = null;
        macFile = null;
        try {
            mac = new String(b, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        Log.e(TAG,"mac:" + mac);
        return mac;
    }
    public  boolean isIpAddress(String address){

        String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pattern = Pattern.compile(ip);

        Matcher matcher = pattern.matcher(address);

        return matcher.matches();
    }



}
