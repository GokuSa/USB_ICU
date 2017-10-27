package com.shine.visitsystem;

import android.app.Application;
import android.preference.PreferenceManager;

import com.shine.fragment.SettingFragment;

public class MyApplication extends Application {
	private String TAG = "MyApplication";
	private static MyApplication instance;

	public  static MyApplication getInstance() {
		return instance;
	}


	@Override
	public void onCreate() {
		super.onCreate();
		instance=this;
//		CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
	}

	  //判断视频流是否是高清的 720
	  public boolean is720P() {
		return PreferenceManager.getDefaultSharedPreferences(this).getInt(SettingFragment.CAMERA_PREVIEW_SIZE, 0) == 0;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
	


}
