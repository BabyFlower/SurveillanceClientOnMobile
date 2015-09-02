package com.anviz.scom.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * 信息
 * 
 * @author 8444
 * 
 */
public class InfoSql {

	private final static String TABLE_NAME = "info";

	/** 信息唯一标识 */
	private final static String ID = BaseColumns._ID;
	/** 信息头 */
	private final static String TITLE = "title";
	/** 信息内容 */
	private final static String CONTENT = "content";
	/** 是否为已读 1为已读 0为未读 */
	private final static String ISREAD = "isread";
	/**日期*/
	private final static String TIME = "time";

	/**
	 * 创建INFO表结构
	 * 
	 * @param db
	 */
	public static void createTable(SQLiteDatabase db) {
		String sql = "create table " + TABLE_NAME + "(" + //
				ID + " integer primary key autoincrement," + //
				TITLE + " text," + //
				CONTENT + " text," + //
				ISREAD + " integer," + //
				TIME + " long" + //
				");";//
		db.execSQL(sql);
	}

	/**
	 * 
	 * @param context
	 * @param title
	 * @param content
	 * @param isRead
	 * @param time
	 */
	public static void insert(Context context, String title, String content, int isRead, long time) {

		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put(TITLE, title);
		cv.put(CONTENT, content);
		cv.put(ISREAD, isRead);
		cv.put(TIME, time);

		db.insert(TABLE_NAME, null, cv);

		db.close();
	}

	/**
	 * 
	 * @param context
	 * @param orderBy
	 * @return
	 */
	public static Cursor query(Context context, String orderBy) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		return db.query(TABLE_NAME, null, null, null, null, null, orderBy);
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
		cv.put(ISREAD, 1);
		db.update(TABLE_NAME, cv, "_id=?", new String[] { id + "" });
	}
}
