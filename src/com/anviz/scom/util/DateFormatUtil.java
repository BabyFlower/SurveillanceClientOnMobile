package com.anviz.scom.util;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 时间格式化工具类
 * @author 8444
 *
 */
public class DateFormatUtil {

	public static String format = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 字符串格式日期 转换成 Date long
	 * @param time
	 * @return
	 */
	public static long stringToLong(String time){
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			return formatter.parse(time).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
	
	/**
	 * Date long 转换成 字符串格式日期
	 * @param time
	 * @return
	 */
	public static String longToString(long time){
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}
}
