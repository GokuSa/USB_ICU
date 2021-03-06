package com.shine.fragment;


import android.databinding.DataBindingUtil;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.shine.tools.DialogManager;
import com.shine.visitsystem.HomeActivity;
import com.shine.visitsystem.R;
import com.shine.visitsystem.databinding.FragmentPreviewBinding;

import java.io.File;

import static com.shine.visitsystem.R.id.im_mac;
import static com.shine.visitsystem.R.id.im_volmue_minus;
import static com.shine.visitsystem.R.id.im_volmue_plus;


/**
 * A simple {@link Fragment} subclass.
 * 预览页面 需要看到自己的摄像头预览
 */
public class PreviewFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "PreviewFragment";

    private FragmentPreviewBinding mBinding;
    private Camera mCamera;
    private DialogManager mDialogManager = new DialogManager();

    public PreviewFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: inflater ");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated:");
        mBinding.textureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mBinding.ivBack.setOnClickListener(this);
        mBinding.imVolmueMinus.setOnClickListener(this);
        mBinding.imVolmuePlus.setOnClickListener(this);
        mBinding.imMac.setOnClickListener(this);

        File file = new File(HomeActivity.PATH_BG);
        if (file.exists()) {
            Glide.with(this)
                    .load(HomeActivity.PATH_BG)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            mBinding.srlPreview.setBackground(resource);
                        }
                    });
        } else {
            mBinding.srlPreview.setBackgroundResource(R.drawable.login_bg);
        }

        mDialogManager.showWaitingDialog(getActivity(), "正在打开摄像头");

    }

    private void openCamera() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mCamera = Camera.open();
                    if (mCamera != null) {
                        mCamera.setPreviewTexture(mBinding.textureView.getSurfaceTexture());
                        mCamera.startPreview();
                        getActivity().runOnUiThread(() ->
                                mDialogManager.closeWaitingDialog(getActivity())
                        );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "打开摄像头失败", Toast.LENGTH_SHORT).show());
                    Log.e(TAG, "打开摄像头失败");
                }

            }
        }.start();

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        mDialogManager.closeWaitingDialog(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");

    }


    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable");
            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                HomeActivity homeActivity = (HomeActivity) getActivity();
                if (homeActivity != null) {
                    homeActivity.showHome();
                }
                break;
            //开关麦克风
            case im_mac:
                break;
            case im_volmue_minus:
                break;
            case im_volmue_plus:

                break;
        }
    }


}
