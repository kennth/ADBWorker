package com.funmix.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import com.funmix.common.Log4jLogger;
import com.funmix.model.FakePhone;

public class FakeUtils {
	protected static Logger		log			= Log4jLogger.getLogger(FakeUtils.class);

	private static String[][]	APhone		= { { "Samsung", "Galaxy Note 2", "GT-N7100", "N7100ZSUFNJ2" }, { "Samsung", "GT-N7100", "GT-N7100", "CHN-N7100ZCUEMK3" },
			{ "Samsung", "Galaxy Note", "GT-I9220", "I9220ZHLSF" }, { "Samsung", "Galaxy S3", "GT-I9300", "CHN-I9300ZCUBML1" },
			{ "Samsung", "Galaxy S3", "GT-I9300", "I9300ZCUBMK2" }, { "Samsung", "Galaxy S4 ", "GT-I9500", "I9500VZMBUNE2" },
			{ "Samsung", "Galaxy S2", "GT-I9100", "I9100GZCLSG " }, { "Samsung", "Galaxy Mega 5.8", "GT-I9152", "I9152ZCUAMG3" },
			{ "Samsung", "Galaxy S5", "SM-G9008V", "G9008VZMU1ANE4" }, { "Samsung", "Galaxy Grand 2", "SM-G7106", "G7106ZNUAND2" },
			{ "Xiaomi", "MI 2S", "MI 2S", "JLB39.0_20150209173249" }, { "Xiaomi", "MI 2A", "MI 2A", "JLB39.0_20150209173248" },
			{ "Xiaomi", "MI 1S", "MI 1S", "JHFCNBF17.0_20150209173940" }, { "Xiaomi", "MI 2", "MI 2", "JLB39.0_20150209172914" }, { "Xiaomi", "MI 3", "MI 3", "V6.3.10.0.KXDCNBL" },
			{ "Huawei", "H30-T10", "H30-T10", "V100R001CHNC00B141" }, { "Xiaomi", "HM 2A", "HM 2A", "JHFCNBF17.0_20150209173940" },
			{ "Xiaomi", "红米手机", "HM NOTE 1LTEW", "V6.3.11.0.KHKCNBL" }, { "Xiaomi", "红米手机", "2013022", "JHBCNBD16.0" },
			{ "Xiaomi", "红米手机", "HM 1SLTETD", "JHFCNBF17.0_20150209173940" } };
	/*
	 * private static String[] androidSDK =
	 * {"4.2.2","4.4.2","4.3","4.4.4","4.1.2","4.1.1","4.0.4","4.2.1"}; private
	 * static int[] androidApi = {17,19,18,19,16,16,15,17}; private static int[]
	 * androidVal = {22,17,17,17,12,5,5,5};
	 */
	private static String[]		androidSDK	= { "4.2.2", "4.2.1" };
	private static int[]		androidApi	= { 17, 17 };
	private static int[]		androidVal	= { 22, 17, 17, 17, 5 };

	public FakePhone getFakePhone(int pid, int ver) {
		if (pid < 0 || pid >= APhone.length) {
			pid = Utils.getRandom(0, APhone.length - 1);
		}
		if (ver < 0 || ver >= androidSDK.length) {
			ver = Utils.getRandom(0, androidSDK.length - 1);
		}
		FakePhone phone = new FakePhone();
		phone.setBrand(APhone[pid][0]);
		phone.setName(APhone[pid][1]);
		phone.setModel(APhone[pid][2]);
		phone.setDisplayid(APhone[pid][3]);
		phone.setRelease(androidSDK[ver]);
		phone.setSdkver("" + androidApi[ver]);
		// log.info(TypeUtil.typeToString("",phone));
		return phone;
	}

	public void writeBuildProp(String filename, FakePhone phone) {
		BufferedReader reader;
		FileOutputStream localFileOutputStream;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
			StringBuffer buf = new StringBuffer();
			while (true) {
				String str1 = reader.readLine();
				if (str1 == null) {
					break;
				}
				if (str1.contains("ro.build.display.id")) {
					buf.append("ro.build.display.id=");
					buf.append(phone.getDisplayid());
					buf.append("\n");
				} else if (str1.contains("apps.setting.product.release")) {
					buf.append("apps.setting.product.release=");
					buf.append(phone.getDisplayid());
					buf.append("\n");
				} else if (str1.contains("ro.build.version.release")) {
					buf.append("ro.build.version.release=");
					buf.append(phone.getRelease());
					buf.append("\n");
				} else if (str1.contains("ro.build.version.sdk")) {
					buf.append("ro.build.version.sdk=");
					buf.append(phone.getSdkver());
					buf.append("\n");
				} else if (str1.contains("ro.product.model")) {
					buf.append("ro.product.model=");
					buf.append(phone.getModel());
					buf.append("\n");
				} else if (str1.contains("ro.product.chivinproduct")) {
					buf.append("ro.product.chivinproduct=");
					buf.append(phone.getModel());
					buf.append("\n");
				} else if (str1.contains("ro.product.chivinversion")) {
					buf.append("ro.product.chivinversion=");
					buf.append(phone.getDisplayid());
					buf.append("\n");
				} else if (str1.contains("ro.product.name")) {
					buf.append("ro.product.name=");
					buf.append(phone.getName());
					buf.append("\n");
				} /*
					 * else if (str1.contains("ro.product.device")) {
					 * buf.append("ro.product.device="); buf.append(fakeName);
					 * buf.append("\n"); }
					 */else if (str1.contains("ro.product.brand")) {
					buf.append("ro.product.brand=");
					buf.append(phone.getBrand());
					buf.append("\n");
				} else if (str1.contains("ro.product.manufacturer")) {
					buf.append("ro.product.manufacturer=");
					buf.append(phone.getBrand());
					buf.append("\n");
				} else if (str1.contains("ro.software.version")) {
					buf.append("ro.software.version=");
					buf.append(phone.getDisplayid());
					buf.append("\n");
				} else {
					buf.append(str1);
					buf.append("\n");
				}
			}
			if (buf.indexOf("debug.sf.nobootanimation=1") == -1)
				buf.append("debug.sf.nobootanimation=1\n");
			reader.close();
			// log.info(buf.toString());
			localFileOutputStream = new FileOutputStream(new File(filename));
			localFileOutputStream.write(buf.toString().getBytes());
			localFileOutputStream.close();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void writeWIFIFile(String filename, byte[] mac) {
		File localFile;
		FileInputStream input;
		FileOutputStream output;
		try {
			localFile = new File(filename);
			input = new FileInputStream(localFile);
			byte[] data = new byte[(int) localFile.length()];
			input.read(data);
			input.close();
			for (int i = 4; i < 10; i++) {
				data[i] = mac[i - 4];
			}
			output = new FileOutputStream(localFile);
			output.write(data);
			output.close();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void writeWIFIFile(String filename) {
		File localFile;
		FileInputStream input;
		FileOutputStream output;
		try {
			localFile = new File(filename);
			input = new FileInputStream(localFile);
			byte[] data = new byte[(int) localFile.length()];
			input.read(data);
			input.close();
			for (int i = 4 + 3; i < 10; i++) {
				data[i] = (byte) (Utils.getRandom(100, 200));
				// log.info(data[i]);
			}
			// a(data);
			output = new FileOutputStream(localFile);
			output.write(data);
			output.close();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void writeWIFIFileFIX(String filename, int a, int b, int c) {
		File localFile;
		FileInputStream input;
		FileOutputStream output;
		try {
			localFile = new File(filename);
			input = new FileInputStream(localFile);
			byte[] data = new byte[(int) localFile.length()];
			input.read(data);
			input.close();
			// for (int i = 4+3; i < 10; i++) {
			data[7] = (byte) (a);
			data[8] = (byte) (b);
			data[9] = (byte) (c);
			// log.info(data[i]);
			// }
			// a(data);
			output = new FileOutputStream(localFile);
			output.write(data);
			output.close();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public static void a(byte[] arg5) {
		byte v0 = 0;
		int v2 = arg5.length - 2;

		if (arg5[v2] == -86) {
			int v1;
			for (v1 = 0; v1 < v2; ++v1) {
				v0 = (v1 & 1) == 1 ? ((byte) (v0 ^ arg5[v1])) : ((byte) (v0 + arg5[v1]));
			}

			arg5[v2 + 1] = v0;
		}
	}

	public void writeBTFile(String filename) {
		File localFile;
		FileInputStream input;
		FileOutputStream output;
		try {
			localFile = new File(filename);
			input = new FileInputStream(localFile);
			byte[] data = new byte[(int) localFile.length()];
			input.read(data);
			input.close();
			for (int i = 0; i < 6; i++) {
				data[i] = (byte) (Utils.getRandom(0, 255));
			}
			output = new FileOutputStream(localFile);
			output.write(data);
			output.close();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public String randIMEI() {
		return "860" + Utils.getRandom(1000, 9999) + "" + Utils.getRandom(1000, 9999) + "" + Utils.getRandom(1000, 9999);
	}

	public byte[] getMacBytes(String mac) {
		byte[] macBytes = new byte[6];
		String[] strArr = mac.split(":");

		for (int i = 0; i < strArr.length; i++) {
			int value = Integer.parseInt(strArr[i], 16);
			macBytes[i] = (byte) value;
		}
		return macBytes;
	}
}
