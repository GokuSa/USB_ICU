package cn.shine.icumaster.test;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

import android.test.AndroidTestCase;

public class Test extends AndroidTestCase {

	public void test() {
		RequestParams params = new RequestParams();
		params.put("room_id", 123+"");
		params.put("duration", 444+"");
		params.put("record", 1+"");
		System.out.println(params.toString());
		String string = AsyncHttpClient.getUrlWithQueryString(true, "http://www.baidu.com",
				params);
		System.out.println(string);
	}
}
