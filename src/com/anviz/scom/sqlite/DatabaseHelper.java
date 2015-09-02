package com.anviz.scom.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库管理类
 * @author 8444
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	/** 数据库名 */
	private static final String DATABASE_NAME = "anvizmobile.db";
	/** 数据库版本号 */
	private static final int DATABASE_Version = 1;
	/** 表名 */


	/**
	 * 创建数据库
	 * 
	 * @param context
	 */

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_Version);
	}

	/**
	 * 创建表
	 */
	public void onCreate(SQLiteDatabase db) {
		
//		创建实时播放视频列表数据库表结构
		LiveSql.createTable(db);
//		创建录像表结构
		PlayBackSql.createTable(db);
//		创建事件表结构
		EventSql.createTable(db);
//		创建信息表结构
		InfoSql.createTable(db);
		
		
	}

	/**
	 * 更新表时回调
	 */
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}

}