package com.shine.tools;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.shine.fragment.CameraErrDialog;
import com.shine.fragment.WaitingDialog;

/**
 * author:
 * 时间:2017/10/17
 * qq:1220289215
 * 类描述：
 */

public class DialogManager {
    private static final String TAG = "DialogManager";

    public void showWaitingDialog(FragmentActivity fragmentActivity, String info) {
        if (fragmentActivity == null) {
            Log.e(TAG, "showWaitingDialog: activity is null");
            return;
        }
        Log.d(TAG, "showWaitingDialog: ");
        FragmentTransaction fragmentTransaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        WaitingDialog waitingDialog = (WaitingDialog)fragmentActivity.getSupportFragmentManager().findFragmentByTag("waiting_info");
        if (waitingDialog != null) {
            fragmentTransaction.remove(waitingDialog);
        }
        waitingDialog = WaitingDialog.newInstance(info);
        waitingDialog.show(fragmentTransaction, "waiting_info");
    }

    public void closeWaitingDialog(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null) {
            Log.e(TAG, "closeWaitingDialog: activity is null");
            return;
        }
        WaitingDialog waitingDialog = (WaitingDialog) fragmentActivity.getSupportFragmentManager().findFragmentByTag("waiting_info");
        if (waitingDialog != null) {
            waitingDialog.dismiss();
        }
    }

    public void showCameraErrDialog(FragmentActivity fragmentActivity,String info) {
        if (fragmentActivity == null) {
            Log.e(TAG, "showCameraErrDialog: activity is null");
            return;
        }
        FragmentTransaction fragmentTransaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        CameraErrDialog camera_err = (CameraErrDialog)  fragmentActivity.getSupportFragmentManager().findFragmentByTag("camera_err");
        if (camera_err != null) {
            fragmentTransaction.remove(camera_err);
        }
        camera_err = CameraErrDialog.newInstance(info);
        camera_err.show(fragmentTransaction, "camera_err");
    }

    public void closeCameraErrDialog(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null) {
            return;
        }
        CameraErrDialog waitingDialog = (CameraErrDialog) fragmentActivity.getSupportFragmentManager().findFragmentByTag("camera_err");
        if (waitingDialog != null) {
            waitingDialog.dismiss();
        }
    }
}
