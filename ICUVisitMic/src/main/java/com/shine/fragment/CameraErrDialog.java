package com.shine.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.shine.visitsystem.HomeActivity;

/**
 * author:
 * 时间:2017/9/27
 * qq:1220289215
 * 类描述：相机错误对话框
 */

public class CameraErrDialog extends DialogFragment {

    private static final String MSG_TITLE = "msg_title";

    public static CameraErrDialog newInstance(String title) {
        Bundle args = new Bundle();
        CameraErrDialog fragment = new CameraErrDialog();
        args.putString(MSG_TITLE,title);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final HomeActivity activity = (HomeActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String title = getArguments().getString(MSG_TITLE,"");
        builder.setTitle(title);
        builder.setPositiveButton(android.R.string.ok,(dialog,which)->{
            dismiss();
            activity.stopVideoTalk();
//            activity.finish();
        });
        builder.setNegativeButton(android.R.string.cancel,(dialog,which)->{
            dismiss();
        });
        return builder.create();
    }
}
