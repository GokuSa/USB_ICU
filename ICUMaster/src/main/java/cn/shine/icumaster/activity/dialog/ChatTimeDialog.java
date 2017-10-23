package cn.shine.icumaster.activity.dialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.shine.icumaster.R;
import cn.shine.icumaster.activity.adapter.ChatTimeAdapter;
import cn.shine.icumaster.widget.PopupSpinner;

public class ChatTimeDialog extends AbsDialog {

	PopupSpinner popupSpinner;

	public ChatTimeDialog(PopupSpinner popupSpinner, Context context, int theme) {
		super(context, theme);
		this.popupSpinner = popupSpinner;
	}

	@Override
	protected void init() {
		super.init();
		setContentView(R.layout.chat_time);
		ListView lv = (ListView) findViewById(R.id.lv);
		List<Integer> mDatas = new ArrayList<Integer>();
		mDatas.add(15);
		mDatas.add(30);
		mDatas.add(45);
		mDatas.add(60);
		lv.setAdapter(new ChatTimeAdapter(getContext(), mDatas));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				popupSpinner.setSelection(position);
				dismiss();
			}
		});
	}

}
