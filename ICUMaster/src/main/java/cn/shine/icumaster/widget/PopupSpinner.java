package cn.shine.icumaster.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;
import cn.shine.icumaster.activity.dialog.AbsDialog;
import cn.shine.icumaster.activity.dialog.ChatTimeDialog;

public class PopupSpinner extends Spinner {

	Context context;

	@Override
	public boolean performClick() {
		// Builder builder = new AlertDialog.Builder(context);
		// builder.setTitle("title");
		// TextView tv = new TextView(context);
		// tv.setText("haha");
		// builder.setView(tv);
		// AlertDialog dialog = builder.create();
		// Window window = dialog.getWindow();
		// android.view.WindowManager.LayoutParams a = window.getAttributes();
		// window.setGravity(Gravity.LEFT | Gravity.TOP);
		// a.x = 100; // 新位置X坐标
		// a.y = 100; // 新位置Y坐标
		// a.width = 100; // 宽度
		// a.height = 100; // 高度
		// a.alpha = 0.7f; // 透明度
		// window.setAttributes(a);
		// dialog.show();
		// AbsDialog dialog = new AbsDialog(context,
		// cn.shine.icumaster.R.style.NoTitleDialog);
		ChatTimeDialog dialog = new ChatTimeDialog(this, context,
				cn.shine.icumaster.R.style.NoTitleDialog);
		dialog.show();
		return true;
	}

	public PopupSpinner(Context context, AttributeSet attrs, int defStyle,
			int mode) {
		super(context, attrs, defStyle, mode);
		this.context = context;
	}

	public PopupSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public PopupSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public PopupSpinner(Context context, int mode) {
		super(context, mode);
		this.context = context;
	}

	public PopupSpinner(Context context) {
		super(context);
		this.context = context;
	}

}
