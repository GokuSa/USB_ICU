package cn.shine.icumaster.util;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;

public class BitmapUtil {

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * 改变了BitmapFactory.decode的方式
	 * 
	 * @param imgFile
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	public static Bitmap tryGetBitmap(String imgFile, int minSideLength,
			int maxNumOfPixels) {
		// Looger.i(IConstantValues.TAG, "可用内存：" +
		// MemoryManager.getAvailableInternalMemorySize());
		if (imgFile == null || imgFile.length() == 0)
			return null;
		FileInputStream fis = null;
		Bitmap bmp = null;
		try {
			fis = new FileInputStream(imgFile);
			FileDescriptor fd = fis.getFD();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// BitmapFactory.decodeFile(imgFile, options);
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			options.inSampleSize = computeSampleSize(options, minSideLength,
					maxNumOfPixels);
			// Looger.i(IConstantValues.TAG, "options.inSampleSize: " +
			// options.inSampleSize);
			try {
				// 这里一定要将其设置回false，因为之前我们将其设置成了true
				// 设置inJustDecodeBounds为true后，decodeFile并不分配空间，即，BitmapFactory解码出来的Bitmap为Null,但可计算出原始图片的长度和宽度
				options.inJustDecodeBounds = false;
				bmp = BitmapFactory.decodeStream(fis, null, options);
			} catch (OutOfMemoryError err) {
				err.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != fis) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return bmp;
	}

	public static Bitmap tryGetBitmap_old(String imgFile, int minSideLength,
			int maxNumOfPixels) {

		if (imgFile == null || imgFile.length() == 0)
			return null;
		try {
			FileDescriptor fd = new FileInputStream(imgFile).getFD();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// BitmapFactory.decodeFile(imgFile, options);
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			options.inSampleSize = computeSampleSize(options, minSideLength,
					maxNumOfPixels);
			try {
				// 这里一定要将其设置回false，因为之前我们将其设置成了true
				// 设置inJustDecodeBounds为true后，decodeFile并不分配空间，即，BitmapFactory解码出来的Bitmap为Null,但可计算出原始图片的长度和宽度
				options.inJustDecodeBounds = false;
				Bitmap bmp = BitmapFactory.decodeFile(imgFile, options);
				return bmp == null ? null : bmp;
			} catch (OutOfMemoryError err) {
				err.printStackTrace();
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 从view 得到图片
	 * 
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromView(View view) {
		view.destroyDrawingCache();
//		view.measure(View.MeasureSpec.makeMeasureSpec(0,
//				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
//				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = view.getDrawingCache(true);
		return bitmap;
	}

	public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	// http://dyh7077063.iteye.com/blog/970672
	// ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * Drawable 转 bitmap
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof NinePatchDrawable) {
			Bitmap bitmap = Bitmap
					.createBitmap(
							drawable.getIntrinsicWidth(),
							drawable.getIntrinsicHeight(),
							drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
									: Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
					drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			return bitmap;
		} else {
			return null;
		}
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, w, h);
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) {
		final int reflectionGap = 4;
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, h / 2, w,
				h / 2, matrix, false);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(w, (h + h / 2),
				Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null);
		Paint deafalutPaint = new Paint();
		canvas.drawRect(0, h, w, h + reflectionGap, deafalutPaint);

		canvas.drawBitmap(reflectionImage, 0, h + reflectionGap, null);

		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
				0x00ffffff, TileMode.CLAMP);
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, h, w, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
	}

	public static Bitmap createReflectedBitmap(Bitmap srcBitmap) {
		if (null == srcBitmap) {
			return null;
		}

		// The gap between the reflection bitmap and original bitmap.
		final int REFLECTION_GAP = 4;

		int srcWidth = srcBitmap.getWidth();
		int srcHeight = srcBitmap.getHeight();
		int reflectionWidth = srcBitmap.getWidth();
		int reflectionHeight = srcBitmap.getHeight() / 2;

		if (0 == srcWidth || srcHeight == 0) {
			return null;
		}

		// The matrix
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		try {
			// The reflection bitmap, width is same with original's, height is
			// half of original's.
			Bitmap reflectionBitmap = Bitmap.createBitmap(srcBitmap, 0,
					srcHeight / 2, srcWidth, srcHeight / 2, matrix, false);

			if (null == reflectionBitmap) {
				return null;
			}

			// Create the bitmap which contains original and reflection bitmap.
			Bitmap bitmapWithReflection = Bitmap.createBitmap(reflectionWidth,
					srcHeight + reflectionHeight + REFLECTION_GAP,
					Config.ARGB_8888);

			if (null == bitmapWithReflection) {
				return null;
			}

			// Prepare the canvas to draw stuff.
			Canvas canvas = new Canvas(bitmapWithReflection);

			// Draw the original bitmap.
			canvas.drawBitmap(srcBitmap, 0, 0, null);

			// Draw the reflection bitmap.
			canvas.drawBitmap(reflectionBitmap, 0, srcHeight + REFLECTION_GAP,
					null);

			Paint paint = new Paint();
			paint.setAntiAlias(true);
			LinearGradient shader = new LinearGradient(0, srcHeight, 0,
					bitmapWithReflection.getHeight() + REFLECTION_GAP,
					0x70FFFFFF, 0x00FFFFFF, TileMode.MIRROR);
			paint.setShader(shader);
			paint.setXfermode(new PorterDuffXfermode(
					android.graphics.PorterDuff.Mode.DST_IN));

			// Draw the linear shader.
			canvas.drawRect(0, srcHeight, srcWidth,
					bitmapWithReflection.getHeight() + REFLECTION_GAP, paint);

			return bitmapWithReflection;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		// drawable转换成bitmap
		Bitmap oldbmp = drawableToBitmap(drawable);
		// 创建操作图片用的Matrix对象
		Matrix matrix = new Matrix();
		// 计算缩放比例
		float sx = ((float) w / width);
		float sy = ((float) h / height);
		// 设置缩放比例
		matrix.postScale(sx, sy);
		// 建立新的bitmap，其内容是对原bitmap的缩放后的图
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return new BitmapDrawable(newbmp);
	}

	// ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	// http://dyh7077063.iteye.com/blog/970672

	/**
	 * 计算图片的宽高
	 * 
	 * @author 宋疆疆
	 * @date 2014年7月2日 上午11:34:03
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static int[] getBitmapSize(String path) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		return new int[] { opts.outWidth, opts.outHeight };
	}

	/**
	 * 计算图片的宽高
	 * 
	 * @author 宋疆疆
	 * @date 2014年7月2日 上午11:34:03
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static int[] getBitmapSize(Resources res, int id) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, id, opts);
		return new int[] { opts.outWidth, opts.outHeight };
	}
}
