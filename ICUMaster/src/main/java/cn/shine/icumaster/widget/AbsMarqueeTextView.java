package cn.shine.icumaster.widget;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.TextView;

public abstract class AbsMarqueeTextView extends TextViewVertical {

	public static final int MODE_SCROLL = 0;
	public static final int MODE_LAYOUT = 1;
	protected int speed = 1;
	protected int rate = 15;
	protected Handler mHandler = new Handler();
	protected Runnable runner;
	protected int mode;
	protected int windowWidth;
	protected int windowHeight;

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public AbsMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public AbsMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	private void init(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point();
		wm.getDefaultDisplay().getSize(outSize);
		windowWidth = outSize.x;
		windowHeight = outSize.y;
	}

	public abstract void startMarquee(int mode);

	public abstract void stopMarquee();

}
