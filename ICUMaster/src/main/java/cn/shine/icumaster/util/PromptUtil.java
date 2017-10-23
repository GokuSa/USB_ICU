package cn.shine.icumaster.util;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 缂佺喍绔寸粻锛勬倞閺勫墽銇氶崘鍛啇
 *
 * @author Administrator
 */
public class PromptUtil {

    private static Toast toast = null;

    public static void showToastAtCenter(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }


    public static void showToastAtCenter(Context context, String msg,
                                         int textSize) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(msg);
        }
        ViewGroup view = (ViewGroup) toast.getView();
        TextView tv = (TextView) view.getChildAt(0);
        tv.setTextSize(textSize);
        toast.show();
    }
}
