package cn.shine.icumaster.util;

import android.content.Context;

public class DensityUtil {

	public static int dip2px(Context context, int dip) {
		float density = context.getResources().getDisplayMetrics().density;
		int px = (int) (dip * density);
		return px;
	}
	
	public static int px2Dip(Context context, int px) {
		float density = context.getResources().getDisplayMetrics().density;
		int dip = (int) (px / density);
		return dip;
	}
}
