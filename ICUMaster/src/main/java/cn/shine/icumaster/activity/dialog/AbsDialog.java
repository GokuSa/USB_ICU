package cn.shine.icumaster.activity.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Window;
import android.widget.LinearLayout;
import cn.shine.icumaster.R;

public class AbsDialog extends Dialog {

	public AbsDialog(Context context, int theme) {
		super(context, theme);
		init();
	}

	protected void init() {
		Window window = getWindow();
		window.setBackgroundDrawable(null);
	}

}
