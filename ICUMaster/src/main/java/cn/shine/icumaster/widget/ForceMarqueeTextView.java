package cn.shine.icumaster.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 强制自动开启跑马灯
 */
public class ForceMarqueeTextView extends TextView {


    public ForceMarqueeTextView(Context context) {
        super(context);
        init();
    }

    public ForceMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForceMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
//        super.onFocusChanged( focused,  direction,  previouslyFocusedRect);
    }
}
