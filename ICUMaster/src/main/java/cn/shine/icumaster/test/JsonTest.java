package cn.shine.icumaster.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.test.AndroidTestCase;
import cn.shine.icumaster.bean.ChatDevice;
import cn.shine.icumaster.bean.ChatRoom;
import cn.shine.icumaster.net.ChatRoomInfo;
import cn.shine.icumaster.net.DeviceInfo;
import cn.shine.icumaster.util.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.loopj.android.http.RequestParams;

public class JsonTest extends AndroidTestCase {

	public void test() throws Exception {
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		arrayList.add(1);
		arrayList.add(2);
		ChatRoom room1 = new ChatRoom();
		room1.id = 1;
		room1.name = "a-1";
		room1.audioAddress = "";
		room1.devicesIds = arrayList;
		room1.duration = 30 * 60 * 1000;
		room1.startTime = 13 * 1000;
		room1.endTime = room1.startTime + room1.duration;
		room1.isRecord = 0;
		room1.isStop = false;
		ObjectMapper mapper = new ObjectMapper();
		String string = mapper.writeValueAsString(room1);
		System.out.println(string);
	}

	public void testRoom() throws IOException {
		List<ChatRoom> allRoom = new ChatRoomInfo(null).getDebugInfo();

		ObjectMapper mapper = new ObjectMapper();
		String string = mapper.writeValueAsString(allRoom);
		System.out.println(string);

		string = "[{       \"audioAddress\": \"123\",        \"devicesIds\": [            1,            2],\"name\": \"a-1\",       \"endTime\": \"1813000\",        \"id\": \"1\",\"isRecord\": 1, \"duration\": 1800000,\"isStop\": \"true\"}]";

		CollectionType constructMapType = mapper.getTypeFactory()
				.constructCollectionType(ArrayList.class, ChatRoom.class);
		List<ChatRoom> readValue = mapper.readValue(string, constructMapType);
		for (ChatRoom chatRoom : readValue) {
			System.out.println(chatRoom);
		}
	}

	public void testDevice2() throws IOException {
		Map<Integer, ChatDevice> allDevice = new DeviceInfo(null)
				.getDebugInfo();

		ObjectMapper mapper = new ObjectMapper();
		String string = mapper.writeValueAsString(allDevice);
		System.out.println(string);

		MapType constructMapType = mapper.getTypeFactory().constructMapType(
				HashMap.class, Integer.class, ChatDevice.class);
		Map<Integer, ChatDevice> readValue = mapper.readValue(string,
				constructMapType);
		Set<Integer> keySet = readValue.keySet();
		for (Integer integer : keySet) {
			ChatDevice chatDevice = readValue.get(integer);
			System.out.println(chatDevice);
		}
	}

	public void testDevice() throws JsonProcessingException {

		ChatDevice chatDevice = new ChatDevice();
		chatDevice.id = 1;
		chatDevice.ip = "172.138.66.8";
		chatDevice.name = "手推车1";
		chatDevice.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		chatDevice.roomId = 33;
		chatDevice.type = 0;

		ObjectMapper mapper = new ObjectMapper();
		String string = mapper.writeValueAsString(chatDevice);
		System.out.println(string);

		chatDevice = new ChatDevice();
		chatDevice.id = 2;
		chatDevice.ip = "172.138.66.78";
		chatDevice.name = "手推车3";
		chatDevice.videoAddress = "shine_av_stream://@10.0.1.153:5074";
		chatDevice.roomId = 35;
		chatDevice.type = 1;
		string = mapper.writeValueAsString(chatDevice);
		System.out.println(string);
	}
}
