package com.anviz.scom.sqlite;

import java.io.ByteArrayOutputStream;

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
 * 录像表管理
 * 
 * @author 8444
 * 
 */
public class PlayBackSql {

	public final static String TABLE_NAME = "playback";

	/**
	 * 创建PLAYBACK表结构
	 * 
	 * @param db
	 */
	public static void createTable(SQLiteDatabase db) {
		String sql = "Create table " + TABLE_NAME + "(" + BaseColumns._ID
				+ " integer primary key autoincrement, "
				+ " picture blob not null," // 图片byte[]
				+ " name text," // 设备名称
				+ " time text," // 时间
				+ " length text," // 时长
				+ " isLocal integer" // 是本地还是远程录像， 1：本地，0远程
				+ ");";

		db.execSQL(sql);
	}

	/**
	 * 
	 * @param context
	 * @param picture
	 * @param name
	 * @param time
	 * @param length
	 * @param isLocal
	 */
	public static void insert(Context context, int picture, String name,
			String time, String length, int isLocal) {
		Drawable drawable = context.getResources().getDrawable(picture);

		if (drawable == null) {
			return;
		}
		BitmapDrawable bd = (BitmapDrawable) drawable;
		Bitmap bitmap = bd.getBitmap();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, os);

		insert(context, os.toByteArray(), name, time, length, isLocal);
	}

	/**
	 * 
	 * @param context
	 * @param picture
	 * @param name
	 * @param time
	 * @param length
	 * @param isLocal
	 */
	public static void insert(Context context, byte[] picture, String name,
			String time, String length, int isLocal) {

		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put("picture", picture);
		cv.put("name", name);
		cv.put("time", time);
		cv.put("length", length);
		cv.put("isLocal", isLocal);

		db.insert(TABLE_NAME, null, cv);
		db.close();
	}

	/**
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
	 * 查询以name 分组
	 * 
	 * @param context
	 * @return
	 */
	public static Cursor queryByGroup(Context context) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		return db.query(TABLE_NAME, null, null, null, "name", null, null);

	}

	/**
	 * 查询以 name isLocal为条件
	 * 
	 * @return
	 */
	public static Cursor queryByNameAndIsLocal(Context context, String name,
			int isLocal) {

		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		return db.query(TABLE_NAME, null, "name=? and isLocal=?", new String[] {
				name, "" + isLocal }, null, null, "time");
	}

}
