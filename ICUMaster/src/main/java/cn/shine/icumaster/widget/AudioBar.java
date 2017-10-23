package cn.shine.icumaster.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import cn.shine.icumaster.util.AudioUtil;

/**
 * 简单封装了一个音量条的控件
 */
public class AudioBar extends SeekBar implements SeekBar.OnSeekBarChangeListener {

    private static final int HIDE_TIME = 3 * 1000;
    private static final int HIDE = 0;
    private static final float STEP_RANGE = 0.1f;
    private int mMaxVolume;
    private int mCurVolume;
    private boolean keepShow;

    public boolean isKeepShow() {
        return keepShow;
    }

    public void setKeepShow(boolean keepShow) {
        this.keepShow = keepShow;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE:
                    hideUI();
                    break;
            }
        }
    };

    public AudioBar(Context context) {
        super(context);
        init();
    }

    public AudioBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mMaxVolume = AudioUtil.getMaxVolume(getContext());
        mCurVolume = AudioUtil.getVolume(getContext());
        setMax(mMaxVolume);
        setProgress(mCurVolume);
        setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            showUI();
            AudioUtil.setVolume(getContext(), progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void up() {
        showUI();
        mCurVolume = (int) (mCurVolume + mMaxVolume * STEP_RANGE);
        mCurVolume = mCurVolume > mMaxVolume ? mMaxVolume : mCurVolume;
        AudioUtil.setVolume(getContext(), mCurVolume);
        setProgress(mCurVolume);
    }

    public void down() {
        showUI();
        mCurVolume = (int) (mCurVolume - mMaxVolume * STEP_RANGE);
        mCurVolume = mCurVolume < 0 ? 0 : mCurVolume;
        AudioUtil.setVolume(getContext(), mCurVolume);
        setProgress(mCurVolume);
    }

    private void showUI() {
        mHandler.removeMessages(HIDE);
        View p = (View) getParent();
        p.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(HIDE, HIDE_TIME);
    }

    private void hideUI() {
        View p = (View) getParent();
        p.setVisibility(View.GONE);
    }


}
