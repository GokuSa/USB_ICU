package cn.shine.icumaster;

import android.app.Application;

public class MyApplication extends Application {
	private static MyApplication sMyApplication;

	public static MyApplication getInstance() {
		return sMyApplication;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sMyApplication=this;
		

	}


}
