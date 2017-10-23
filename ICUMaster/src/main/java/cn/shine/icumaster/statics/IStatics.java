package cn.shine.icumaster.statics;

import cn.shine.icumaster.Global;

/**
 * 用来存放一些固定的静态变量
 * 
 * @author 宋疆疆
 * 
 */
public interface IStatics {

	boolean DEBUG = true;
	boolean UI_DEBUG = true;

	String NO_ERROR = "0";

	String GET_DEVICE_URL = Global.SERVER_URL_PRE + "getDevice/getdevice.php";
	String GET_ROOM_URL = Global.SERVER_URL_PRE + "getRoom/getroom.php";
	String CREATE_ROOM_URL = Global.SERVER_URL_PRE
			+ "createRoom/createroom.php";
	String START_ROOM_URL = Global.SERVER_URL_PRE + "startRoom/startroom.php";
	String STOP_ROOM_URL = Global.SERVER_URL_PRE + "stopRoom/stoproom.php";
	String DELETE_ROOM_URL = Global.SERVER_URL_PRE + "deleteRoom/deleteroom.php";
	String START_RECORD_URL = Global.SERVER_URL_PRE + "createRoom/startrecord.php";

	int VISIT_CLIENT_PORT = 33334;
	int AUDIO_SERVER_PORT = 6360;
	int VEDIO_SERVER_PORT = 4360;

	String VISIT_CLIENT_MSG_STOP = "STOP|";

	String RECORD_REMOTE_PATH = "D:/test/";
	String RECORD_VIDEO_START_PRE_MSG = "[SHCMD][START]<param>";
	String RECORD_VIDEO_START_FIX_MSG = "</param>";
	String RECORD_VIDEO_START_MSG = "<cam name=\"<!name>\""
			+ " url=\"<!url>:sout=#file{mux=ts,dst=<!path>}\"/>";
	String RECORD_VIDEO_STOP_PRE_MSG = "[SHCMD][STOP]";
	String RECORD_VIDEO_STOP_MSG = "<cam name=\"<!name>\"/>";
	String RECORD_AUDIO_START_MSG = "[SHCMD][START-REC]name=\"[MIX <!name>]\" file=\""
			+ RECORD_REMOTE_PATH + "<!path>\"";
	String RECORD_AUDIO_STOP_MSG = "[SHCMD][STOP-REC]name=\"[MIX <!name>]\"";

	String TEST_DIR = "/data/work/icu/";
	String TEST_GET_DEVICE_URL = TEST_DIR + "getdevice.php";
	String TEST_GET_ROOM_URL = TEST_DIR + "getroom.php";
	String TEST_CREATE_ROOM_URL = TEST_DIR + "createroom.php";
	String TEST_START_ROOM_URL = TEST_DIR + "startroom.php";
	String TEST_STOP_ROOM_URL = TEST_DIR + "stoproom.php";
	String TEST_DELETE_ROOM_URL = TEST_DIR + "deleteroom.php";
}
