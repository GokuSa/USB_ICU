package com.shine.fragment;


import android.databinding.DataBindingUtil;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.shine.tools.DialogManager;
import com.shine.visitsystem.HomeActivity;
import com.shine.visitsystem.R;
import com.shine.visitsystem.databinding.FragmentPreviewBinding;

import java.io.File;
import java.util.List;

import static com.shine.visitsystem.R.id.im_mac;
import static com.shine.visitsystem.R.id.im_volmue_minus;
import static com.shine.visitsystem.R.id.im_volmue_plus;


/**
 * A simple {@link Fragment} subclass.
 * 预览页面 需要看到自己的摄像头预览
 */
public class PreviewFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "PreviewFragment";
    private DialogManager mDialogManager=new DialogManager();
    private FragmentPreviewBinding mBinding;
    private USBMonitor mUSBMonitor;
    private UVCCamera mCamera;

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
        setBackground();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDialogManager.showWaitingDialog(getActivity(),"正在打开摄像头");
//        showWaitingDialog();
        mUSBMonitor = new USBMonitor(getActivity(), mOnDeviceConnectListener);
        mUSBMonitor.register();

    }

    //设置指定路径下的背景图，如果没有就显示默认的
    private void setBackground() {
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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mDialogManager.closeWaitingDialog(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUSBMonitor.unregister();
        Log.d(TAG, "onDestroyView() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        releaseCamera();
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.destroy();
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    //申请打开USB摄像头的回调，忽略USB的插拔
    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {}

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.d(TAG, "onConnect");
            if (mCamera != null) mCamera.destroy();
            mCamera = new UVCCamera();
            new Thread() {
                @Override
                public void run() {
                    mCamera.open(ctrlBlock);
                    try {
                        mCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_YUYV);
                        mCamera.setPreviewTexture(mBinding.textureView.getSurfaceTexture());
                        mCamera.startPreview();
                        getActivity().runOnUiThread(()->{ mDialogManager.closeWaitingDialog(getActivity());});
                    } catch (final IllegalArgumentException e) {
                        Log.e(TAG, "run: ", e);
                    }
                }
            }.start();

        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            if (mCamera != null) {
                mCamera.close();
            }
        }

        @Override
        public void onDetach(final UsbDevice device) {
            Log.d(TAG, "onDetach() called with: device = [" + device + "]");
        }

        @Override
        public void onCancel() {}
    };

    //    请求打开USB摄像头，SystemUI已修改 不会弹出对话框等待用户确认； 直接授权
    private void openUSUCamera() {
        final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(getActivity(), R.xml.device_filter);
        List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filter);
        if (deviceList.size() > 0) {
            mUSBMonitor.requestPermission(deviceList.get(0));
        } else {
//            showCameraErr("没有找到USB摄像头");
            Log.e(TAG, "onCreate: no camera device");
        }
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable");
//            当Surface可用的时候打开摄像头
            openUSUCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed");
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
