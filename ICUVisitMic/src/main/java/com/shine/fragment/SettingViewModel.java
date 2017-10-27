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
import static com.shine.fragment.SettingFragment.CAMERA_PREVIEW_SIZE;
import static com.shine.fragment.SettingFragment.CAMERA_TYPE;

/**
 * author:
 * 时间:2017/9/14
 * qq:1220289215
 * 类描述：音频相关设置 拾音距离 拾音效果 拾音类型 放音类型
 */

public class SettingViewModel {
    private static final String TAG = "AudioSettingViewModel";
    public final ObservableInt mAudioEffect = new ObservableInt(0);
    public final ObservableInt mAudioInputType = new ObservableInt(0);
    public final ObservableInt mAudioOutputType = new ObservableInt(0);
    public final ObservableInt mAudioInputDistance = new ObservableInt(0);
    //摄像头类型 默认USB
    public final ObservableInt mCameraType = new ObservableInt(0);
    //摄像头编码类型 默认1280*720
    public final ObservableInt mCameraPerviewSize = new ObservableInt(0);
    //摄像头编码码率率
    public final ObservableFloat mCameraEncodeBitRate = new ObservableFloat(0);
    //音频延时 在USB 摄像头编码时，视频帧延迟与音频不同步 所以添加此设置 使口型一致
    public final ObservableField<String> mAudioDelay=new ObservableField<>();
    private DialogFragment mDialogFragment;

    public SettingViewModel(DialogFragment dialogFragment) {
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

        //摄像头类型 0 USB  1-》板载
        int cameraType = sharedPreferences.getInt(CAMERA_TYPE, 0);
        mCameraType.set(cameraType);

        //对方编码输出 0->1280*720  1->640*480
        int cameraPreviewSize = sharedPreferences.getInt(CAMERA_PREVIEW_SIZE, 0);
        mCameraPerviewSize.set(cameraPreviewSize);
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
            case R.id.rb_camera_usb:
                mCameraType.set(0);
                break;
            case R.id.rb_camera_internal:
                mCameraType.set(1);
                break;
            case R.id.rb_camera_previewsize1:
                mCameraPerviewSize.set(0);
                break;
            case R.id.rb_camera_previewsize2:
                mCameraPerviewSize.set(1);
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
                .putInt(CAMERA_TYPE, mCameraType.get())
                .putInt(CAMERA_PREVIEW_SIZE, mCameraPerviewSize.get())
                .apply();
    }

}
