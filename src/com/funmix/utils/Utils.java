package com.funmix.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Random;
import org.apache.log4j.Logger;
import com.funmix.common.Log4jLogger;

import net.sourceforge.pinyin4j.PinyinHelper;

public class Utils {
	public static String[][]	areas		= new String[][] { { "北京", "BEJ" }, { "湖南", "HUN" }, { "天津", "TAJ" }, { "湖北", "HUB" }, { "上海", "SHH" }, { "广东", "GUD" },
			{ "重庆", "CHQ" }, { "海南", "HAI" }, { "河北", "HEB" }, { "四川", "SCH" }, { "山西", "SHX" }, { "贵州", "GUI" }, { "辽宁", "LIA" }, { "云南", "YUN" }, { "吉林", "JIL" },
			{ "陕西", "SHA" }, { "黑龙江", "HLJ" }, { "甘肃", "GAN" }, { "江苏", "JSU" }, { "青海", "QIH" }, { "浙江", "ZHJ" }, { "台湾", "TAI" }, { "安徽", "ANH" }, { "西藏", "TIB" },
			{ "福建", "FUJ" }, { "内蒙古", "NMG" }, { "江西", "JXI" }, { "广西", "GXI" }, { "山东", "SHD" }, { "宁夏", "NXA" }, { "河南", "HEN" }, { "新疆", "XIN" }, { "香港", "HKG" },
			{ "澳门", "MAC" }, { "未知", "UKN" } };
	public static String[]		logincol	= new String[] { "_1st", "_2nd", "_3rd", "_4th", "_5th", "_6th", "_7th", "_8th", "_9th", "_10th", "_11st", "_12nd", "_13rd", "_14th",
			"_15th", "_16th", "_17th", "_18th", "_19th", "_20th", "_21st", "_22nd", "_23rd", "_24th", "_25th", "_26th", "_27th", "_28th", "_29th", "_30th" };
	public static String[]		enames		= { "Julie", "Sheri", "Peter", "Pam", "Alma", "Eula", "Janet", "Harry", "Alice", "Lee", "Phil", "Gene", "Sadie", "Tyler", "Raul",
			"Edgar", "James", "May", "Carl", "Agnes", "Ruben", "Faye", "Samue", "Lucy", "Danny", "Jane", "Evan", "Susie", "Susan", "Ellis", "Merle", "Sally", "Annie", "Ed",
			"Felix", "Ellen", "Isaac", "Caleb", "June", "Ray", "Matt", "Wanda", "Saul", "Lance", "Rita", "Sean", "Ida", "Alvin", "Jenny", "Wilma", "Sarah", "Jerry", "Jamie",
			"Bert", "Lyle", "Garry", "Anita", "Edna", "Benny", "Tammy", "Renee", "Lynne", "Elmer", "Gayle", "Brett", "Lynda", "Mindy", "Debra", "Dean", "Tasha", "Pat", "Emily",
			"Cecil", "Lana", "Daisy", "Darla", "Duane", "Laura", "Eddie", "Erma", "Allen", "Nancy", "Max", "Betsy", "Ben", "Shane", "Jimmy", "Pete", "Ralph", "Dave", "Devin",
			"Tara", "Chad", "Elsie", "Patsy", "Ted", "Mable", "Tina", "Essie", "Emma", "Dewey", "Seth", "Marie", "Gwen", "Betty", "Wade", "Luis", "Casey", "Elias", "Gary", "Angel",
			"Beth", "Fred", "Jaime", "Grant", "Abel", "Randy", "Ryan", "Tim", "Mabel", "Ada", "Ethel", "Mamie", "Clint", "Van", "Jay", "Barry", "Patty", "Ervin", "Clay", "Jill",
			"Grady", "Sam", "Paula", "Angie", "Megan", "Amy", "Jesse", "Shaun", "Irene", "Vera", "Edith", "Mae", "Shawn", "Wm", "Rudy", "Allan", "Terri", "Luz", "Craig", "Delia",
			"Lisa", "Jana", "Lula", "Billy", "Henry", "Perry", "Rene", "Drew", "Stacy", "Bryan", "Dana", "Freda", "Andre", "Paul", "Verna", "Gall", "Rex", "Leigh", "Jenna",
			"Irvin", "Myra", "Judy", "Leah", "Ira", "Lewis", "Nina", "Lynn", "Traci", "Guy", "Janis", "Maria", "Greg", "Gina", "Pearl", "Rufus", "Grace", "Mandy", "Daryl", "Jean",
			"Wayne", "Ruth", "Cary", "Helen", "Hilda", "Edwin", "Ian", "Iris", "Elena", "Erin", "Steve", "Ann", "Lena", "Dawn", "Erica", "Timmy", "Janie", "Lucas", "Peggy", "Levi",
			"Hugh", "David", "Lila", "Lamar", "Cell", "Clyde", "Cindy", "Brent", "Della", "Jesus", "Diana", "Neal", "Terry", "Percy", "Juan", "Amber", "Anna", "Penny", "Earl",
			"Dixie", "Tanya", "Andy", "Elsa", "Inez", "Mary", "Al", "Diane", "Jan", "Eva", "Wendy", "Sue", "Jared", "Ella", "Sandy", "Bruce", "Adam", "Misty", "Brad", "Dale",
			"Cathy", "Alan", "Gregg", "Patti", "Glen", "Sammy", "Jeff", "Marc", "Anne", "Teri", "Cesar", "Brian", "Irma", "Darin", "Bill", "Heidi", "Jim", "Alex", "Lydia", "Linda",
			"Clara", "Julia", "Larry", "Ruby", "Sara", "Eric", "Chris", "Velma", "Ivan", "April", "Tracy", "Carla", "Marty", "Neil", "Juana", "Ana", "Hazel" };
	protected static Logger		log			= Log4jLogger.getLogger(Utils.class);

	public static int getRandByWeight(int[] weight) {
		int randval = Utils.getRandom(1, 100);
		int min = 0;
		int max = 100;
		for (int i = 0; i < weight.length; i++) {
			min = calArray(weight, i);
			max = calArray(weight, i + 1);
			if (randval > min && randval <= max)
				return i;
		}
		return 0;
	}

	public static int calArray(int[] array, int count) {
		int result = 0;
		for (int i = 0; i < count && i < array.length; i++) {
			result = result + array[i];
		}
		return result;
	}

	public static int getRandom(int min, int max) {
		Random random = new Random();
		return (random.nextInt(max + 1 - min) + min);
	}

	public static String getDate() {
		java.util.Date d = new java.util.Date();
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		return s.format(d);
	}

	public static String getTime() {
		java.util.Date d = new java.util.Date();
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return s.format(d);
	}

	public static String getTime(String format) {
		java.util.Date d = new java.util.Date();
		SimpleDateFormat s = new SimpleDateFormat(format);
		return s.format(d);
	}

	public static int showDaysOfMonth(int year, int month) {
		int days[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		if (2 == month && 0 == (year % 4) && (0 != (year % 100) || 0 == (year % 400))) {
			days[1] = 29;
		}
		return (days[month - 1]);
	}

	public static String getCurrentPath() {
		try {
			File directory = new File("");
			return directory.getAbsolutePath();
		} catch (Exception e) {
		}
		return null;
	}
	
	public static byte[] getFileContent(String filePath) throws IOException {  
        File file = new File(filePath);  
        log.info(filePath);
        long fileSize = file.length();  
        if (fileSize > Integer.MAX_VALUE) {  
            System.out.println("file too big...");  
            return null;  
        }  
        FileInputStream fi = new FileInputStream(file);  
        byte[] buffer = new byte[(int) fileSize];  
        int offset = 0;  
        int numRead = 0;  
        while (offset < buffer.length  
        && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {  
            offset += numRead;  
        }  
        // 确保所有数据均被读取  
        if (offset != buffer.length) {  
        throw new IOException("Could not completely read file "  
                    + file.getName());  
        }  
        fi.close();  
        return buffer;  
    }  

	public static void saveToFile(String destUrl, String fileName) throws IOException {
		int BUFFER_SIZE = 4096;
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		HttpURLConnection httpUrl = null;
		URL url = null;
		byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;

		// 建立链接
		url = new URL(destUrl);
		httpUrl = (HttpURLConnection) url.openConnection();
		// 连接指定的资源
		httpUrl.connect();
		// 获取网络输入流
		bis = new BufferedInputStream(httpUrl.getInputStream());
		// 建立文件
		fos = new FileOutputStream(fileName);

		// if (this.DEBUG)
		// System.out.println("正在获取链接[" + destUrl + "]的内容...\n将其保存为文件["
		// +fileName + "]");
		// 保存文件
		while ((size = bis.read(buf)) != -1)
			fos.write(buf, 0, size);

		fos.close();
		bis.close();
		httpUrl.disconnect();
	}

	public static boolean pathExist(String path) {
		File dirname = new File(path);
		if (dirname.isDirectory()) { // 目录不存在
			return true;
		}
		return false;
	}

	public static void mkPath(String path) {
		File dirname = new File(path);
		if (!dirname.isDirectory()) { // 目录不存在
			dirname.mkdir();
		}
	}

	public static void appendFile(String fileName, String content) {
		FileWriter writer = null;
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			writer = new FileWriter(fileName, true);
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getKeyValue(String key, LinkedList<String> queue) {
		String line = "";
		try {
			while (queue.size() > 0) {
				line = queue.poll();
				if (line == null || line.trim().length() == 0) {
					log.error("ERROR:get null from queue");
					break;
				}
				// log.info(line);
				if (line.indexOf(key) > -1) {
					return line;
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	public static int getKey(String key, LinkedList<String> queue) {
		return getKey(new String[] { key }, queue);
	}

	public static int getKey(String[] keys, LinkedList<String> queue) {
		String line = "";
		try {
			while (queue.size() > 0) {
				line = queue.poll();
				if (line == null || line.trim().length() == 0) {
					log.error("ERROR:get null from queue");
					break;
				}
				// log.info(line);
				for (int i = 0; i < keys.length; i++) {
					// log.info(keys[i]);
					if (line.indexOf(keys[i]) > -1) {
						// log.info("### Find Key:" + keys[i]);
						return i;
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return -1;
	}

	public static void waitKey(String key, LinkedList<String> queue) {
		while (getKey(key, queue) == -1) {
			sleep(1000);
		}
	}

	public static String execCMD(String cmd) {// cmd
		// System.out.println(cmd);
		Process p = null;
		String result = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			// p.wait(30000);
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()), 4096);
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			result = "";
			while ((line = reader.readLine()) != null) {
				result = result + line + "\r\n";
			}
			while ((line = errorReader.readLine()) != null) {
				/*
				 * if(line!=null && line.length()>0) log.error("execCMD error:"
				 * + line);
				 */
			}
			p.getOutputStream().close();
			p.destroy();
			reader = null;
			errorReader = null;
		} catch (IOException e) {
			log.error(e);
		} catch (InterruptedException e) {
			log.error(e);
		} finally {
			if (p != null) {
				p.destroy();
			}
		}
		return result;
	}

	public static void sleep(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String format2(double num) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
		return df.format(num);
	}
	
	public static String genAndrodid(){		  
        String str="ABCDEF0123456789"; 
        StringBuffer sb = new StringBuffer();	          
        for(int i = 0 ; i < 16; ++i){	              
            sb.append(str.charAt(getRandom(0,str.length()-1)));  
        }  
        return sb.toString();	     
	}

	public static String genImsi() {		
		return "460" + getRandom(1000,9999) + getRandom(1000,9999)+ getRandom(1000,9999) ;
	}

	public static String genSimserial() {
		return "8986" + getRandom(1000,9999) + getRandom(1000,9999)+ getRandom(1000,9999)+ getRandom(1000,9999) ;
	}

	public static String genSerialno(String model) {
		String str="ABCDEF0123456789"; 
        StringBuffer sb = new StringBuffer();	
        sb.append(model.substring(0,2));
        for(int i = 0 ; i < 8; ++i){	              
            sb.append(str.charAt(getRandom(0,str.length()-1)));  
        }  
        return sb.toString();	    
	}
	
	public static String getPinYinHeadChar(String str) {  
        String convert = "";  
        for (int j = 0; j < str.length(); j++) {  
            char word = str.charAt(j);  
            // 提取汉字的首字母  
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);  
            if (pinyinArray != null) {  
                convert += pinyinArray[0].charAt(0);  
            } else {  
                convert += word;  
            }  
        }  
        return convert;  
    }  
}
