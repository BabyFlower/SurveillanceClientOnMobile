package com.anviz.scom.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.EventSql;
import com.anviz.scom.sqlite.LiveSql;
import com.anviz.scom.util.GeneratorContactCountIcon;
import com.anviz.scom.view.ScrollerItemView;

/**
 * 实时播放设备列表适配器
 * 
 * @author 8444
 * 
 */
public class UI07_DevAdapter extends ResourceCursorAdapter {

	@SuppressWarnings("deprecation")
	public UI07_DevAdapter(Context context, Cursor cursor) {
		super(context, R.layout.ui07_adapter, cursor);

	}

	public void bindView(View view, final Context context, final Cursor cursor) {
		((ScrollerItemView) view).shrink();
		ImageView iv = (ImageView) view.findViewById(R.id.ui07_adapter_iv);
		TextView nameTv = (TextView) view.findViewById(R.id.ui07_adapter_name);
		final TextView deleteTv = (TextView) view
				.findViewById(R.id.ui07_adapter_delete);

		final String name = cursor.getString(2);
		final int id = cursor.getInt(0);

		deleteTv.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View view) {
				LiveSql.delete(context, id);
				// 下两句是数据源刷新代码
				cursor.requery();
				UI07_DevAdapter.this.notifyDataSetChanged();
			}
		});

		nameTv.setText(name);

		// 获取数据
		byte[] b = cursor.getBlob(1);

		// 将获取的数据转换成drawable
		Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, null);
		iv.setImageBitmap(bitmap);

		final ImageView playBackIv = (ImageView) view
				.findViewById(R.id.ui07_adapter_playback);
		playBackIv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
			}
		});

		final ImageView eventIv = (ImageView) view
				.findViewById(R.id.ui07_adapter_event);
		int queue = EventSql.queryUnreadCount(context, name);
		if(queue > 0){
			eventIv.setImageBitmap(new GeneratorContactCountIcon().addSum(context
					.getResources().getDrawable(R.drawable.event), queue));
		}else{
			eventIv.setImageResource(R.drawable.event_bg);
		}
		
		eventIv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
			}
		});
	}

}
