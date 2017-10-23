package cn.shine.icumaster.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.util.Log;

/**
 * 对日志进行管理
 * 
 * @author 宋疆疆
 * 
 */
public class Logger {
	public static final String TAG = "icu_master";
	private static final String NOTE_PATH = "log###.txt";
	private static int useFileCount;

	private static Context mContext;

	public static final int LEVEL = 0;
	private static final int VERBOSE = Log.VERBOSE;
	private static final int DEBUG = Log.DEBUG;
	private static final int INFO = Log.INFO;
	private static final int WARN = Log.WARN;
	private static final int ERROR = Log.ERROR;

	public static void syso(String msg) {
		if (LEVEL <= VERBOSE) {
			System.out.println(msg);
		}
	}

	public static void v(String msg) {
		if (LEVEL <= VERBOSE) {
			Log.v(TAG, msg);
		}
	}

	public static void d(String msg) {
		if (LEVEL <= DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void i(String msg) {
		if (LEVEL <= INFO) {
			Log.i(TAG, msg);
		}
	}

	public static void w(String msg) {
		if (LEVEL <= WARN) {
			Log.w(TAG, msg);
		}
	}

	public static void e(String msg) {
		if (LEVEL <= ERROR) {
			Log.e(TAG, msg);
		}
	}

	public static void takeNotes(Throwable e) {
		FileOutputStream fos = null;
		try {
			fos = mContext
					.openFileOutput(getLogFileName(), Context.MODE_APPEND);
			String msg = "\n\n";
			msg += android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss",
					System.currentTimeMillis());
			msg += "====================================================";
			msg += "\n\n";
			msg += printStackTrace(e);
			byte[] buffer = msg.getBytes();
			fos.write(buffer);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				fos = null;
			}
		}
	}

	public static void takeNotes(String message) {
		FileOutputStream fos = null;
		try {
			fos = mContext
					.openFileOutput(getLogFileName(), Context.MODE_APPEND);
			String msg = "\n\n";
			msg += android.text.format.DateFormat.format("yyyy-MM-dd kk:mm:ss",
					System.currentTimeMillis());
			msg += "====================================================";
			msg += "\n\n";
			msg += message;
			byte[] buffer = msg.getBytes();
			fos.write(buffer);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				fos = null;
			}
		}
	}

	public static void setContext(Context context) {
		mContext = context;
	}

	public static String printStackTrace(Throwable e) {
		StringBuilder sb = new StringBuilder();
		sb.append(e.toString() + "\n");
		// Don't use getStackTrace() as it calls clone()
		// Get stackTrace, in case stackTrace is reassigned
		StackTraceElement[] stack = e.getStackTrace();
		for (java.lang.StackTraceElement element : stack) {
			sb.append("\tat " + element + "\n");
		}

		StackTraceElement[] parentStack = stack;
		Throwable throwable = e.getCause();
		while (throwable != null) {
			sb.append("Caused by: ");
			sb.append(throwable + "\n");
			StackTraceElement[] currentStack = throwable.getStackTrace();
			int duplicates = countDuplicates(currentStack, parentStack);
			for (int i = 0; i < currentStack.length - duplicates; i++) {
				sb.append("\tat " + currentStack[i] + "\n");
			}
			if (duplicates > 0) {
				sb.append("\t... " + duplicates + " more" + "\n");
			}
			parentStack = currentStack;
			throwable = throwable.getCause();
		}
		return sb.toString();
	}

	private static int countDuplicates(StackTraceElement[] currentStack,
			StackTraceElement[] parentStack) {
		int duplicates = 0;
		int parentIndex = parentStack.length;
		for (int i = currentStack.length; --i >= 0 && --parentIndex >= 0;) {
			StackTraceElement parentFrame = parentStack[parentIndex];
			if (parentFrame.equals(currentStack[i])) {
				duplicates++;
			} else {
				break;
			}
		}
		return duplicates;
	}

	/**
	 * 初始化日志文件
	 * 
	 * @param count
	 *            日志文件的最大数量
	 */
	public static void initLogFile(int count) {
		int notExistCount = checkNotExistLogFile(count);
		useFileCount = notExistCount;
		deleteLogFile(count, notExistCount);
	}

	/**
	 * 检查已经有了几个日志文件了
	 * 
	 * @param count
	 *            日志文件的最大数量
	 * @return 不存在的日志编号
	 */
	private static int checkNotExistLogFile(int count) {
		for (int i = 1; i <= count; i++) {
			File file = new File(mContext.getFilesDir(), NOTE_PATH.replace(
					"###", i + ""));
			if (!file.exists())
				return i;
		}
		return count + 1;
	}

	/**
	 * 根据最大日志量和当前不存在的日志编号，删除最旧的日志
	 * 
	 * @param count
	 * @param notExistCount
	 */
	private static void deleteLogFile(int count, int notExistCount) {
		int deleteCount = 0;
		if ((count + 1) == notExistCount) {
			deleteCount = 1;
		} else {
			deleteCount = notExistCount + 1;
		}
		File deleteFile = new File(mContext.getFilesDir(), NOTE_PATH.replace(
				"###", deleteCount + ""));
		deleteFile.delete();
	}

	/**
	 * 得到应该使用的日志文件的名字
	 * 
	 * @return
	 */
	private static String getLogFileName() {
		return NOTE_PATH.replace("###", useFileCount + "");
		// return NOTE_PATH.replace("###",
		// DateFormat.format("_yyyy_MM_dd", System.currentTimeMillis()));
	}

	public static void v(String tag, String msg) {
		if (LEVEL <= VERBOSE) {
			Log.v(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (LEVEL <= DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (LEVEL <= INFO) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (LEVEL <= WARN) {
			Log.w(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (LEVEL <= ERROR) {
			Log.e(tag, msg);
		}
	}

	public static void syso(boolean flag, String msg) {
		if (LEVEL <= VERBOSE && flag) {
			System.out.println(msg);
		}
	}

	public static void v(boolean flag, String msg) {
		if (LEVEL <= VERBOSE && flag) {
			Log.v(TAG, msg);
		}
	}

	public static void d(boolean flag, String msg) {
		if (LEVEL <= DEBUG && flag) {
			Log.d(TAG, msg);
		}
	}

	public static void i(boolean flag, String msg) {
		if (LEVEL <= INFO && flag) {
			Log.i(TAG, msg);
		}
	}

	public static void w(boolean flag, String msg) {
		if (LEVEL <= WARN && flag) {
			Log.w(TAG, msg);
		}
	}

	public static void e(boolean flag, String msg) {
		if (LEVEL <= ERROR && flag) {
			Log.e(TAG, msg);
		}
	}

}
