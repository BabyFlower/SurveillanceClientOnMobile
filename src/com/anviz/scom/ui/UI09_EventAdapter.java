package com.anviz.scom.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.EventSql;
import com.anviz.scom.util.DateFormatUtil;
import com.anviz.scom.view.ScrollerItemView;

/**
 * 事件列表
 * 
 * @author 8444
 * 
 */
public class UI09_EventAdapter extends ResourceCursorAdapter {

	@SuppressWarnings("deprecation")
	public UI09_EventAdapter(Context context, Cursor cursor) {
		super(context, R.layout.ui09_adapter, cursor);
	}

	@SuppressWarnings("deprecation")
	public void bindView(View view, final Context context, final Cursor cursor) {
		
		((ScrollerItemView)view).shrink();
		final ImageView iv = (ImageView) view.findViewById(R.id.ui09_adapter_iv);
		TextView timeTv = (TextView) view.findViewById(R.id.ui09_adapter_time);
		TextView nameTv = (TextView) view.findViewById(R.id.ui09_adapter_name);
		TextView alarmTv = (TextView) view
				.findViewById(R.id.ui09_adapter_alarm);
		TextView keepTv = (TextView) view.findViewById(R.id.ui09_adapter_keep);
		TextView saveAsTv = (TextView) view
				.findViewById(R.id.ui09_adapter_save_as);

		final int id = cursor.getInt(0);
		
		
		int isRead = cursor.getInt(5);
		
		timeTv.setText(DateFormatUtil.longToString(cursor.getLong(1)));
		nameTv.setText(cursor.getString(2));
		alarmTv.setText(cursor.getString(4));
		
		
		if (isRead == 0) {
			timeTv.setTextColor(Color.RED);
			nameTv.setTextColor(Color.RED);
			alarmTv.setTextColor(Color.RED);
			timeTv.getPaint().setFakeBoldText(true);
			nameTv.getPaint().setFakeBoldText(true);
			alarmTv.getPaint().setFakeBoldText(true);
		}else{
			timeTv.setTextColor(Color.BLACK);
			nameTv.setTextColor(Color.BLACK);
			alarmTv.setTextColor(Color.BLACK);
			timeTv.getPaint().setFakeBoldText(false);
			nameTv.getPaint().setFakeBoldText(false);
			alarmTv.getPaint().setFakeBoldText(false);
		}

		// 获取数据
		final byte[] b = cursor.getBlob(3);
		
		final Handler mHandler = new Handler(){
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Drawable drawable = (Drawable) msg.obj;
				iv.setImageDrawable(drawable);
			}
		};
		
		new Thread(){
			public void run() {
				// 将获取的数据转换成drawable
				Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, null);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
				Drawable drawable = bitmapDrawable;

				Message msg = mHandler.obtainMessage(0, drawable);
				mHandler.sendMessage(msg);
			}
		}.start();

		
		iv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent it = new Intent(context,
						UI11_FullScreenImageActivity.class);
				it.putExtra("byte", b);
				context.startActivity(it);
			}
		});
		
		
		final int isretain = cursor.getInt(6);
		
		if(isretain == 1){
//			如果已保留，显示不保留功能
			keepTv.setText(context.getResources().getString(R.string.no_keep));
		}else{
//			如果未保留，显示保留功能
			keepTv.setText(context.getResources().getString(R.string.keep));
		}
		
		keepTv.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				
				if(isretain == 1){
//					如果已保留，点击后则设置成不保留，且按钮显示为保留功能
					EventSql.setRetain(context, id, false);
					cursor.requery();
					UI09_EventAdapter.this.notifyDataSetChanged();
				}else{
//					如果未保留，点击后则设置成保留，且按钮显示为不保留功能
					EventSql.setRetain(context, id, true);
					cursor.requery();
					UI09_EventAdapter.this.notifyDataSetChanged();
				}
			}
		});

		saveAsTv.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

			}
		});
	}

}
