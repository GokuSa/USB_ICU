package com.shine.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.shine.visitsystem.R;
import com.shine.visitsystem.databinding.FragmentSettingBinding;

/**
 * author:
 * 时间:2017/9/14
 * qq:1220289215
 * 类描述：关于音频输入输出的参数配置
 */

public class SettingFragment extends DialogFragment  {
    private static final String TAG = "SettingFragment";
    public static final String AUDIO_EFFECT = "audio_effect";
    public static final String AUDIO_INPUT_TYPE = "audio_input_type";
    public static final String AUDIO_INPUT_DISTANCE = "audio_input_distance";
    public static final String AUDIO_OUTPUT_TYPE = "audio_output_type";
    public static final String AUDIO_DELAY = "audio_delay";
    public static final String CAMERA_ENCODE_FRAME = "camera_encode_frame";
    public static final String CAMERA_TYPE = "camera_type";
    //对方摄像头编码大小 为了vlc全屏播放
    public static final String CAMERA_PREVIEW_SIZE = "camera_preview_size";

    private FragmentSettingBinding mBinding;

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SettingViewModel settingViewModel = new SettingViewModel(this);
        mBinding.setViewModel(settingViewModel);


    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
            window.setLayout(displayMetrics.widthPixels/2, displayMetrics.heightPixels*4/5);
        }
    }

}
