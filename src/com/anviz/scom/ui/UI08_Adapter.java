package com.anviz.scom.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

/**
 * 录像列表适配器
 * 
 * @author 8444
 * 
 */
@SuppressWarnings("deprecation")
public class UI08_Adapter extends ResourceCursorAdapter {

	public UI08_Adapter(Context context, Cursor cursor) {
		super(context, R.layout.ui08_adapter, cursor);
	}

	public void bindView(View view, final Context context, final Cursor cursor) {

		final ImageView iv = (ImageView) view
				.findViewById(R.id.ui08_adapter_iv);
		TextView nameTv = (TextView) view.findViewById(R.id.ui08_adapter_name);

		// 获取数据
		final byte[] b = cursor.getBlob(1);
		final String name = cursor.getString(2);

		final Handler mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				Drawable drawable = (Drawable) msg.obj;
				iv.setImageDrawable(drawable);
			}
		};

		new Thread() {
			public void run() {
				// 将获取的数据转换成drawable
				Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length,
						null);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
				Drawable drawable = bitmapDrawable;

				Message msg = mHandler.obtainMessage(0, drawable);
				mHandler.sendMessage(msg);
			}
		}.start();

		nameTv.setText(name);

		view.findViewById(R.id.ui08_adapter_remote).setOnClickListener(
				new OnClickListener() {

					public void onClick(View view) {

					}
				});

		view.findViewById(R.id.ui08_adapter_local).setOnClickListener(
				new OnClickListener() {

					public void onClick(View view) {

					}
				});

	}

}
