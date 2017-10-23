package com.shine.visitsystem;

import android.app.Application;

public class MyApplication extends Application {
	private String TAG = "MyApplication";
	private static MyApplication instance;
//	private NetworkNative mNetworkNative=new NetworkNative();

	public  static MyApplication getInstance() {
		return instance;
	}


	/*public NetworkNative getNetworkNative() {
		return mNetworkNative;
	}*/

	@Override
	public void onCreate() {
		super.onCreate();
		instance=this;
//		CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
	}



	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
	


}
