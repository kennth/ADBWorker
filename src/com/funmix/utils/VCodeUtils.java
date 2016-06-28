package com.funmix.utils;

import com.funmix.common.YunSu;

public class VCodeUtils {
	public static void main(String[] args) {
		long start= System.currentTimeMillis();
		for(int i=1002;i<1010;i++){
			getVCodeTest("D:\\vcode\\" + i + ".jpg");
			System.out.println(System.currentTimeMillis() - start);
			start = System.currentTimeMillis();
		}
	}
	
	public static String getVCodeTest(String vcodeTmp) {
		String result = YunSu.createByPost("funmixvcode", "1qaz2wsx", "3010", "60", "11576", "55bfd8abd6914f56a1ca0ab5d607189e", vcodeTmp);
		if (result != null && result.length() > 0) {
			System.out.println(result);
			int pos = result.indexOf("<Result>");
			if (pos > -1) {
				result = result.substring(pos + 8, result.indexOf("</Result>"));
				if (result.length() == 4)
					return result;
			}
		}
		return null;
	}
	
	public static String getVCode(String vcodeTmp) {
		String result = YunSu.createByPost("funmixvcode", "1qaz2wsx", "2040", "60", "11576", "55bfd8abd6914f56a1ca0ab5d607189e", vcodeTmp);
		if (result != null && result.length() > 0) {
			System.out.println(result);
			int pos = result.indexOf("<Result>");
			if (pos > -1) {
				result = result.substring(pos + 8, result.indexOf("</Result>"));
				if (result.length() == 4)
					return result;
			}
		}
		return null;
	}
}
