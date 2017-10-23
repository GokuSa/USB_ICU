package cn.shine.icumaster.widget;

import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;

public class MarqueeVerticalTextview extends AbsMarqueeTextView {

	private int currY;
	private int targetY;
	private int rate = 30;

	public int getCurrY() {
		return currY;
	}

	public MarqueeVerticalTextview(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	public MarqueeVerticalTextview(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public boolean isNeedMarquee() {
		System.out.println(getTextHeight() + ", h: " + getHeight());
		return getTextHeight() * 0.95 > getHeight();
	}

	@Override
	public void startMarquee(int mode) {
		if (!isNeedMarquee()) {
			return;
		}
		targetY = getTextHeight();
		if (null == runner) {
			runner = new ScrollRunnbal();
		}
		mHandler.removeMessages(0);
		Message msg = Message.obtain(mHandler, runner);
		msg.what = 0;
		mHandler.sendMessageDelayed(msg, rate);
		// mHandler.postDelayed(runner, rate);
	}

	@Override
	public void stopMarquee() {
		mHandler.removeCallbacks(runner);
	}

	public void reset() {
		stopMarquee();
		currY = 0;
		scrollTo(0, 0);
	}

	private final class ScrollRunnbal implements Runnable {

		@Override
		public void run() {
			scrollTo(0, currY);

			if (currY < targetY) {
				currY += speed;
				mHandler.postDelayed(runner, rate);
			} else {
				currY = -getHeight();
				mHandler.postDelayed(runner, rate);
			}
		}

	}

}
