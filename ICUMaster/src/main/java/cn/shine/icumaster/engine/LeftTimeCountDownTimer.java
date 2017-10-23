package cn.shine.icumaster.engine;

import java.util.Calendar;

import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.shine.icumaster.R;
import cn.shine.icumaster.bean.ChatRoom;

public abstract class LeftTimeCountDownTimer extends CountDownTimer {

    private TextView tv;
    private ChatRoom chatRoom;
    private boolean isMarginChanged;

    public void setTv(TextView tv) {
        this.tv = tv;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public LeftTimeCountDownTimer(long millisInFuture, long countDownInterval,
                                  TextView tv, ChatRoom chatRoom) {
        super(millisInFuture, countDownInterval);
        this.tv = tv;
        this.chatRoom = chatRoom;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        long time = chatRoom.endTime - Calendar.getInstance().getTimeInMillis();
        if (time > chatRoom.duration) {
            time = chatRoom.duration;
        }
        String inFormat = "k:mm:ss";
        if (time <= 0) {
            cancel();
            onFinish();
            tv.setText("00:00");
            return;
        }
        if (time < 3600 * 1000) {
            changeMargin(30);
            inFormat = "mm:ss";
        }
        time -= 8 * 60 * 60 * 1000;
        CharSequence text = DateFormat.format(inFormat, time);
        tv.setText(text);
    }

    private void changeMargin(int size) {
        if (isMarginChanged) {
            return;
        }
        if (tv.getId() == R.id.scan_time_tv) {
            LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) tv
                    .getLayoutParams();
            params.leftMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, size, tv.getContext()
                            .getResources().getDisplayMetrics());
            tv.setLayoutParams(params);
            isMarginChanged = true;
        }
    }
}
