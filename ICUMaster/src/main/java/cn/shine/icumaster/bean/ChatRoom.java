package cn.shine.icumaster.bean;

import java.io.Serializable;
import java.util.List;

import android.widget.TextView;
import cn.shine.icumaster.engine.LeftTimeCountDownTimer;

public class ChatRoom extends BaseBean implements Serializable {

	private static final long serialVersionUID = -5468436566644882508L;
	public int id;
	public String name;
	public String audioAddress;
	public List<Integer> devicesIds;
	public int duration;
	public int startTime;
	public long endTime;
	// 0代表不录制，1代表录制
	public int isRecord;
	public LeftTimeCountDownTimer timer;
	public Object tv;
	public boolean isStop;
	public boolean isEnd;

}
