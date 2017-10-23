package cn.shine.icumaster.engine;

import java.util.HashMap;
import java.util.Map;

import cn.shine.icumaster.bean.ChatDevice;

public class DeviceManager {
	
	private Map<Integer, ChatDevice> mAllDevice;

	public Map<Integer, ChatDevice> getAllDevice() {
		return mAllDevice;
	}

	public void setAllDevice(Map<Integer, ChatDevice> mAllDevice) {
		this.mAllDevice = mAllDevice;
	}

	private static class DeviceManagerHolder {
		private static final DeviceManager mInstance = new DeviceManager();
	}

	public static DeviceManager getInstance() {
		return DeviceManagerHolder.mInstance;
	}

	private DeviceManager() {
		init();
	}

	private void init() {
        mAllDevice = new HashMap<Integer, ChatDevice>();
	}
	
	public ChatDevice getDevice(int id) {
		return mAllDevice.get(id);
	}
}
