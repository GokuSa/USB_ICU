package com.shine.fragment;

import android.content.SharedPreferences;
import android.databinding.ObservableField;
import android.databinding.ObservableFloat;
import android.databinding.ObservableInt;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;

import com.shine.visitsystem.MyApplication;
import com.shine.visitsystem.R;

import static com.shine.fragment.SettingFragment.AUDIO_DELAY;
import static com.shine.fragment.SettingFragment.AUDIO_EFFECT;
import static com.shine.fragment.SettingFragment.AUDIO_INPUT_DISTANCE;
import static com.shine.fragment.SettingFragment.AUDIO_INPUT_TYPE;
import static com.shine.fragment.SettingFragment.AUDIO_OUTPUT_TYPE;
import static com.shine.fragment.SettingFragment.CAMERA_ENCODE_FRAME;

/**
 * author:
 * 时间:2017/9/14
 * qq:1220289215
 * 类描述：音频相关设置 拾音距离 拾音效果 拾音类型 放音类型
 */

public class AudioSettingViewModel {
    private static final String TAG = "AudioSettingViewModel";
    public final ObservableInt mAudioEffect = new ObservableInt(0);
    public final ObservableInt mAudioInputType = new ObservableInt(0);
    public final ObservableInt mAudioOutputType = new ObservableInt(0);
    public final ObservableInt mAudioInputDistance = new ObservableInt(0);
    //摄像头编码码率率
    public final ObservableFloat mCameraEncodeBitRate = new ObservableFloat(0);
    //音频延时 在USB 摄像头编码时，视频帧延迟与音频不同步 所以添加此设置 使口型一致
    public final ObservableField<String> mAudioDelay=new ObservableField<>();
    private DialogFragment mDialogFragment;

    public AudioSettingViewModel(DialogFragment dialogFragment) {
        mDialogFragment = dialogFragment;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        //设置音频效果
        int audioEffect = sharedPreferences.getInt(AUDIO_EFFECT, 0);
        mAudioEffect.set(audioEffect);
        //获取音频输入类型
        int audioInputType = sharedPreferences.getInt(AUDIO_INPUT_TYPE, 0);
        mAudioInputType.set(audioInputType);
        //        获取音频输出类型
        int audioOutputType = sharedPreferences.getInt(AUDIO_OUTPUT_TYPE, 0);
        mAudioOutputType.set(audioOutputType);
        //获取拾音距离
        int audioInputDistance = sharedPreferences.getInt(AUDIO_INPUT_DISTANCE, 20);
        mAudioInputDistance.set(audioInputDistance);
//        摄像头编码码率
        float cameraEncodeFrame = sharedPreferences.getFloat(CAMERA_ENCODE_FRAME, 1f);
        mCameraEncodeBitRate.set(cameraEncodeFrame);

        int audioDelay = sharedPreferences.getInt(AUDIO_DELAY, 160);
        mAudioDelay.set(String.valueOf(audioDelay));

    }

    public void onRadioButtonClick(View view) {
        switch (view.getId()) {
            //增益
            case R.id.rb_audio_gain:
                mAudioEffect.set(1);
                break;
            //正常
            case R.id.rb_audio_normal:
                mAudioEffect.set(0);
                //板载输入
                break;
            case R.id.rb_audio_input_default:
                mAudioInputType.set(0);
                mAudioOutputType.set(0);
                break;
            //usb声卡
            case R.id.rb_audio_input_usb:
                mAudioInputType.set(1);
                break;
            //板载输出
            case R.id.rb_audio_output_default:
                mAudioOutputType.set(0);
                break;
            //usb输出
            case R.id.rb_audio_output_usb:
                mAudioOutputType.set(1);
                break;
            case R.id.rb_half_m:
                mCameraEncodeBitRate.set(0.5f);
                break;
            case R.id.rb_one_m:
                mCameraEncodeBitRate.set(1f);
                break;
            case R.id.rb_two_m:
                mCameraEncodeBitRate.set(2f);
                break;
            case R.id.rb_three_m:
                mCameraEncodeBitRate.set(3f);
                break;
        }

    }

    //拾音距离调节监听
    public void onAudioDistanceBarChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mAudioInputDistance.set(progress);
    }

    //    保存用户设置的参数到本地
    public void onConfirmClick(View view) {
        saveAudioSetting();
        mDialogFragment.dismiss();
    }

    public void onCancleClick(View view) {
        mDialogFragment.dismiss();
    }

    private void saveAudioSetting() {
        int audioDelay=-1;
        try {
             audioDelay = Integer.parseInt(mAudioDelay.get());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (audioDelay< 0) {
           audioDelay=0;
        }
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance());
        sharedPreferences.edit().
                putInt(AUDIO_INPUT_TYPE, mAudioInputType.get())
                .putInt(AUDIO_OUTPUT_TYPE, mAudioOutputType.get())
                .putInt(AUDIO_EFFECT, mAudioEffect.get())
                .putInt(AUDIO_INPUT_DISTANCE, mAudioInputDistance.get())
                .putFloat(CAMERA_ENCODE_FRAME, mCameraEncodeBitRate.get())
                .putInt(AUDIO_DELAY, audioDelay)
                .apply();
    }

}
