package cn.shine.icumaster.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * 用来对文件进行读写操作
 * 
 * @author 宋疆疆
 * 
 */
public class FileRWEngine {
	
	public static String readFile(InputStream is) throws Exception {
		StringBuilder sb = new StringBuilder();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
//		System.out.println(sb.toString());
		br.close();
		isr.close();
		return sb.toString();
	}

	public static String readFile(String filePath) throws Exception {
		StringBuilder sb = new StringBuilder();
		File file = new File(filePath);

		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		//System.out.println(sb.toString());
		br.close();
		isr.close();
		fis.close();
		return sb.toString();
	}

	public static boolean writeFile(String filePath, String contents)
			throws Exception {
		File file = new File(filePath);

		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);

		String[] split = contents.split("\n");
		for (String string : split) {
			// System.out.println(string);
			bw.write(string + "\n");
		}

		bw.close();
		osw.close();
		fos.close();
		return true;
	}

	public static boolean writeFileAppend(String filePath, String contents)
			throws Exception {
		File file = new File(filePath);

		FileOutputStream fos = new FileOutputStream(file, true);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);

		String[] split = contents.split("\n");
		for (String string : split) {
			// System.out.println(string);
			bw.write(string + "\n");
		}

		bw.close();
		osw.close();
		fos.close();
		return true;

	}

	public static boolean deleteText(String filePath, String contents)
			throws Exception {
		String readFile = readFile(filePath);
		File file = new File(filePath);

		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw);

		String[] split = readFile.split("\n");
		for (String string : split) {
			// System.out.println(string);
			if (string.equals(contents))
				continue;
			bw.write(string + "\n");
		}

		bw.close();
		osw.close();
		fos.close();
		return true;

	}

}
