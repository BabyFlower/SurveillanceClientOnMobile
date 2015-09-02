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
 * 实时播放视频列表数据库管理
 * 
 * @author 8444
 * 
 */
public class LiveSql {

	private final static String TABLE_NAME = "live";

	/**
	 * 创建LIVE表结构
	 * 
	 * @param db
	 */
	public static void createTable(SQLiteDatabase db) {
		String sql = "Create table " + TABLE_NAME + "(" + BaseColumns._ID
				+ " integer primary key autoincrement, "
				+ " picture blob not null," // 图片byte[]
				+ " name text" // 设备名称
				+ ");";
		db.execSQL(sql);
	}

	/**
	 * 往picture表中添加数据
	 * 
	 * @param context
	 * @param name
	 *            视频名称
	 * @param picture
	 *            图片资源drawable形式
	 */
	public static void insert(Context context, String name, int picture) {

		Drawable drawable = context.getResources().getDrawable(picture);

		if (drawable == null) {
			return;
		}
		BitmapDrawable bd = (BitmapDrawable) drawable;
		Bitmap bitmap = bd.getBitmap();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, os);
		insert(context, name, os.toByteArray());
	}

	/**
	 * 往picture表中添加数据
	 * 
	 * @param context
	 * @param name
	 *            视频名称
	 * @param picture
	 *            图片数组形式
	 */
	public static void insert(Context context, String name, byte[] picture) {

		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
		ContentValues cv = new ContentValues();

		cv.put("picture", picture);
		cv.put("name", name);

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
	 * 查询
	 * 
	 * @param context
	 * @param table
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @return
	 */
	public static Cursor query(Context context, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();

		return db.query(TABLE_NAME, columns, selection, selectionArgs, groupBy,
				having, orderBy);
	}
}
