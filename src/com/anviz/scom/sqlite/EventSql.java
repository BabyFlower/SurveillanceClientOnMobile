package com.anviz.scom.sqlite;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.BaseColumns;

/**
 * 事件表管理
 * 
 * @author 8444
 * 
 */
public class EventSql {

	public final static String ID = BaseColumns._ID;
	public final static String NAME = "name";
	public final static String TYPE = "type";
	public final static String TIME = "time";
	public final static String ALARM = "alarm";
	public final static String ISREAD = "isread";

	private final static String TABLE_NAME = "event";

	/**
	 * 创建EVENT表结构
	 * 
	 * @param db
	 */
	public static void createTable(SQLiteDatabase db) {
		String sql = "Create table " + TABLE_NAME + "(" + ID
				+ " integer primary key autoincrement," + " time long," // 时间
				+ " name text," // 名称
				+ " picture bolb not null," // 图片byte[]
				+ " alarm text," // 告警信息
				+ " isread integer," // 是否已读 1为已读，0为未读
				+ " isretain integer," // 是否保留 1为保留，0为不保留
				+ " type text" // 事件类型 IO 移动侦测
				+ ");";
		db.execSQL(sql);
	}

	/**
	 * 
	 * @param context
	 * @param time
	 * @param name
	 * @param alarm
	 * @param picture
	 * @param isread
	 */
	public static void insert(Context context, String time, String name,
			String alarm, int picture, int isread, String type) {

		Drawable drawable = context.getResources().getDrawable(picture);

		if (drawable == null) {
			return;
		}
		BitmapDrawable bd = (BitmapDrawable) drawable;
		Bitmap bitmap = bd.getBitmap();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, os);

		insert(context, time, name, alarm, os.toByteArray(), isread, type);
	}

	/**
	 * 
	 * @param context
	 * @param time
	 * @param name
	 * @param alarm
	 * @param picture
	 * @param isread
	 */
	public static void insert(Context context, String time, String name,
			String alarm, byte[] picture, int isread, String type) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put("picture", picture);
		cv.put("name", name);
		cv.put("time", time);
		cv.put("alarm", alarm);
		cv.put("isread", isread);
		cv.put("isretain", 0);
		cv.put("type", type);

		db.insert(TABLE_NAME, null, cv);

		db.close();
	}

	/**
	 * 删除
	 * 
	 * @param context
	 * @param id
	 */
	public static void delete(Context context, int id) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		db.delete(TABLE_NAME, "_id=?", new String[] { id + "" });

		db.close();
	}

	/**
	 * 普通排序查询
	 * 
	 * @param context
	 * @param orderBy
	 * @return
	 */
	public static Cursor query(Context context, String orderBy) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();

		return db.query(TABLE_NAME, null, null, null, null, null, orderBy);
	}

	public static Cursor queryByGroup(Context context, String groupBy,
			String orderBy) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		return db.query(TABLE_NAME, null, null, null, groupBy, null, orderBy);
	}

	/**
	 * 事件过滤查询
	 * 
	 * @param context
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 * @param devselList
	 *            设备选择列表
	 * @param typeList
	 *            过滤类型列表 (time>? and time<?) and (type=? or type=?) and (name=?
	 *            or name=?)
	 * @param orderBy
	 * @return
	 */
	public static Cursor queryEventFiltering(Context context, long startTime,
			long endTime, ArrayList<String> devselList,
			ArrayList<String> typeList, String orderBy) {

		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();

		int length = 2 + (devselList == null ? 0 : devselList.size())
				+ (typeList == null ? 0 : typeList.size());
		StringBuffer selection = new StringBuffer();
		String[] selectionArgs = new String[length];

		int index = 0;
		selection.append("(time>? and time<?)");
		selectionArgs[index] = startTime + "";
		if (endTime == 0) {
			endTime = System.currentTimeMillis();
		}
		index++;
		selectionArgs[index] = endTime + "";

		for (int i = 0; i < devselList.size(); i++) {
			index++;
			if (i == 0) {
				selection.append(" and (");
			}

			selection.append("name=?");
			selectionArgs[index] = devselList.get(i);

			if ((i + 1) != devselList.size()) {
				selection.append(" or ");
			} else {
				selection.append(")");
			}
		}

		for (int i = 0; i < typeList.size(); i++) {

			index++;
			if (i == 0) {
				selection.append(" and (");
			}

			selection.append("type=?");
			selectionArgs[index] = typeList.get(i);

			if ((i + 1) != typeList.size()) {
				selection.append(" or ");
			} else {
				selection.append(")");
			}
		}

		return db.query(TABLE_NAME, null, selection.toString(), selectionArgs,
				null, null, orderBy);
	}

	/**
	 * 设置为已读
	 * 
	 * @param context
	 * @param id
	 */
	public static void setIsRead(Context context, int id) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("isread", 1);
		db.update(TABLE_NAME, cv, "_id=?", new String[] { id + "" });
	}
	
	/**
	 * 查询未读事件数量
	 * @param context
	 * @param name
	 * @return
	 */
	public static int queryUnreadCount(Context context, String name){
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		
		StringBuffer selection = new StringBuffer();
		selection.append(ISREAD + "=?");
		
		int length = 1;
		if(null != name){
			selection.append(" and " + NAME + "=?");
			length ++;
		}
		String[] selectionArgs = new String[length];
		
		selectionArgs[0] = "0";
		if(length == 2){
			selectionArgs[1] = name;
		}
		return db.query(TABLE_NAME, null, selection.toString(), selectionArgs,
				null, null, null).getCount();
	}
	
	/**
	 * 设置是否为保留
	 * 
	 * @param context
	 * @param id
	 * @param isRetain
	 *            true 为保留，false为不保留
	 */
	public static void setRetain(Context context, int id, boolean isRetain) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		ContentValues cv = new ContentValues();

		if (isRetain) {
			cv.put("isretain", 1);
		} else {
			cv.put("isretain", 0);
		}
		db.update(TABLE_NAME, cv, "_id=?", new String[] { id + "" });
	}

}
