package com.shine.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.shine.visitsystem.HomeActivity;
import com.shine.visitsystem.R;
import com.shine.visitsystem.databinding.FragmentHomeBinding;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * 如果背景图片放在不合适的低分辨率的drawable文件中，会拉伸图片占用巨大内存
 */
public class HomeFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding mBinding;
    private AlertDialog mAlertDialog;

    public HomeFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: ");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called with: view ");
        mBinding.btnShutDown.setOnClickListener(this);
        mBinding.textView2.setOnClickListener(this);
        mBinding.tvStartICU.setOnClickListener(this);
        mBinding.imageView2.setOnLongClickListener(this);
        //如果指定目录下有客户自定义图片就使用，否则使用默认的，不使用硬盘缓存 否则不能及时更新
        Glide.with(this)
                .load(HomeActivity.PATH_LOGO)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.logo_shine)
                .into(mBinding.imageView2);
        File file = new File(HomeActivity.PATH_BG);
        if (file.exists()) {
            Glide.with(this)
                    .load(HomeActivity.PATH_BG)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            mBinding.srlStart.setBackground(resource);
                        }
                    });
        } else {
            mBinding.srlStart.setBackgroundResource(R.drawable.login_bg);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeShutdownDialog();
        SettingFragment fragment = (SettingFragment) getActivity().getSupportFragmentManager().findFragmentByTag("audio_setting");
        if (fragment != null) {
            fragment.dismiss();
        }

        Log.d(TAG, "onDestroyView() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_shutDown:
            case R.id.textView2:
                showShutDownDialog();
                break;
            case R.id.tv_startICU:
                HomeActivity homeActivity = (HomeActivity) getActivity();
                if (homeActivity != null) {
                    homeActivity.beginPreview();
                }
                break;
        }
    }

    private void showShutDownDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("是否关机");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shutdown();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {mAlertDialog.dismiss();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void closeShutdownDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    private void shutdown() {
        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        System.exit(0);
    }

    @Override
    public boolean onLongClick(View v) {


        SettingFragment fragment = (SettingFragment) getActivity().getSupportFragmentManager().findFragmentByTag("audio_setting");
        if (fragment != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        fragment = SettingFragment.newInstance();
        fragment.show(getChildFragmentManager(), "audio_setting");
        return true;
    }


}
