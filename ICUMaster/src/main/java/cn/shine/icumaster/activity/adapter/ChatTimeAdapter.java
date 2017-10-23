package cn.shine.icumaster.activity.adapter;

import java.util.List;

import cn.shine.icumaster.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatTimeAdapter extends CommonAdapter<Integer> {

	public ChatTimeAdapter(Context context, List<Integer> mDatas) {
		super(context, mDatas);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = ViewHolder.get(mContext, convertView, parent,
				R.layout.chat_time_item, position);
		TextView tv = holder.getView(R.id.room_name_tv);
		tv.setText(mDatas.get(position) + "");
		return holder.getConvertView();
	}

}
