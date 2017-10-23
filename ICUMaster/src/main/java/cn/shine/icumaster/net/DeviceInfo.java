package cn.shine.icumaster.net;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.Map;

import cn.shine.icumaster.bean.ChatDevice;
import cn.shine.icumaster.statics.IStatics;
import cn.shine.icumaster.util.FileRWEngine;
import cn.shine.icumaster.util.Logger;

public class DeviceInfo {
	private static final String TAG = "DeviceInfo";
	private ResponsListener<Map<Integer, ChatDevice>> listener;

	public DeviceInfo(ResponsListener<Map<Integer, ChatDevice>> listener) {
		super();
		this.listener = listener;
	}

	public void getAllDeviceTest() {
		ObjectMapper mapper = new ObjectMapper();
		MapType type = mapper.getTypeFactory().constructMapType(HashMap.class,
				Integer.class, ChatDevice.class);
		try {
			String readFile = FileRWEngine
					.readFile(IStatics.TEST_GET_DEVICE_URL);
			Map<Integer, ChatDevice> readValue = mapper.readValue(readFile,
					type);
			listener.onSuccess(readValue);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			listener.onFailure(e);
		}
	}
	public void getAllDevice(RequestQueue mVQueue,String ip) {
		RequestParams params = new RequestParams();
		params.put("nursingIP", ip );
		String url = IStatics.GET_DEVICE_URL;
		url = AsyncHttpClient.getUrlWithQueryString(true, url, params);
		Log.e(TAG, "getAllDevice  url : "+url);
		StringUTF8Request request = new StringUTF8Request(
				url, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				Logger.v("device info: " + response);
				if (response.equals(IStatics.NO_ERROR)) {
					listener.onSuccess(null);
					return;
				}
				ObjectMapper mapper = new ObjectMapper();
				MapType type = mapper.getTypeFactory()
						.constructMapType(HashMap.class, Integer.class,
								ChatDevice.class);
				try {
					Map<Integer, ChatDevice> readValue = mapper
							.readValue(response, type);
					listener.onSuccess(readValue);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					listener.onFailure(e);
				}
			}
		}, new ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				listener.onFailure(error);
			}
		});
		mVQueue.add(request);
	}


	public void getAllDevice(RequestQueue mVQueue) {
		StringUTF8Request request = new StringUTF8Request(
				IStatics.GET_DEVICE_URL, new Listener<String>() {

					@Override
					public void onResponse(String response) {
						Logger.v("device info: " + response);
						if (response.equals(IStatics.NO_ERROR)) {
							listener.onSuccess(null);
							return;
						}
						ObjectMapper mapper = new ObjectMapper();
						MapType type = mapper.getTypeFactory()
								.constructMapType(HashMap.class, Integer.class,
										ChatDevice.class);
						try {
							Map<Integer, ChatDevice> readValue = mapper
									.readValue(response, type);
							listener.onSuccess(readValue);
							return;
						} catch (Exception e) {
							e.printStackTrace();
							listener.onFailure(e);
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						listener.onFailure(error);
					}
				});
		mVQueue.add(request);
	}

	public void getAllDevice() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(IStatics.GET_DEVICE_URL, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				listener.onFailure(error);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				ObjectMapper mapper = new ObjectMapper();
				MapType type = mapper.getTypeFactory().constructMapType(
						HashMap.class, Integer.class, ChatDevice.class);
				try {
					String string = new String(responseBody);
					Map<Integer, ChatDevice> readValue = mapper.readValue(
							string, type);
					listener.onSuccess(readValue);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					listener.onFailure(e);
				}
			}
		});
	}

	public Map<Integer, ChatDevice> getDebugInfo() {
		HashMap<Integer, ChatDevice> map = new HashMap<Integer, ChatDevice>();
		ChatDevice device1 = new ChatDevice();
		device1.id = 1;
		device1.ip = "10.0.1.206";
		device1.name = "探视间1";
		device1.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		device1.roomId = -1;
		device1.type = 0;
		ChatDevice device2 = new ChatDevice();
		device2.id = 2;
		device2.ip = "172.168.66.79";
		device2.name = "2";
		device2.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		device2.roomId = -1;
		device2.type = 0;
		ChatDevice device3 = new ChatDevice();
		device3.id = 3;
		device3.ip = "172.168.66.80";
		device3.name = "3";
		device3.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		device3.roomId = -1;
		device3.type = 0;
		ChatDevice device4 = new ChatDevice();
		device4.id = 4;
		device4.ip = "172.168.66.81";
		device4.name = "手推车A";
		device4.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		device4.roomId = -1;
		device4.type = 1;
		ChatDevice device5 = new ChatDevice();
		device5.id = 5;
		device5.ip = "172.168.66.82";
		device5.name = "哈哈哦d";
		device5.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		device5.roomId = -1;
		device5.type = 1;
		ChatDevice device6 = new ChatDevice();
		device6.id = 6;
		device6.ip = "172.168.66.83";
		device6.name = "c";
		device6.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		device6.roomId = -1;
		device6.type = 1;

		map.put(1, device1);
		// map.put(2, device2);
		// map.put(3, device3);
		map.put(4, device4);
		// map.put(5, device5);
		// map.put(6, device6);
		return map;
	}
}
