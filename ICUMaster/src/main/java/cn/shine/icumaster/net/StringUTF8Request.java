package cn.shine.icumaster.net;

import java.io.UnsupportedEncodingException;

import cn.shine.icumaster.util.Logger;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

public class StringUTF8Request extends StringRequest {

	public StringUTF8Request(String url, Listener<String> listener,
			ErrorListener errorListener) {
		super(url, listener, errorListener);
		Logger.d(url);
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed = null;
		try {
			parsed = new String(response.data, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        Logger.i("response: " + parsed);
		return Response.success(parsed,
				HttpHeaderParser.parseCacheHeaders(response));
	}

}
