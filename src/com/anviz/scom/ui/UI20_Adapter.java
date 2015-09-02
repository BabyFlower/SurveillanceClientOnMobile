package com.anviz.scom.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.InfoSql;
import com.anviz.scom.util.DateFormatUtil;


/**
 * 信息列表适配器
 * @author 8444
 *
 */
@SuppressWarnings("deprecation")
public class UI20_Adapter extends ResourceCursorAdapter{

	public UI20_Adapter(Context context, Cursor cursor) {
		super(context, R.layout.ui20_adapter, cursor);
	}

	public void bindView(View view, final Context context, final Cursor cursor) {
		
		final int id = cursor.getInt(0);
		String title = cursor.getString(1);
		String content = cursor.getString(2);
		int isRead = cursor.getInt(3);
		long time = cursor.getLong(4);
		
		TextView timeTv = (TextView) view.findViewById(R.id.ui20_adapter_time);
		TextView contentTv = (TextView) view.findViewById(R.id.ui20_adapter_content);
		TextView titleTv = (TextView) view.findViewById(R.id.ui20_adapter_title);
		
		timeTv.setText(DateFormatUtil.longToString(time));
		contentTv.setText("     " + content);
		titleTv.setText(title);
		
		if(isRead == 1){
			timeTv.setTextColor(R.color.black);
			contentTv.setTextColor(R.color.black);
			titleTv.setTextColor(R.color.black);
			timeTv.getPaint().setFakeBoldText(false);
			contentTv.getPaint().setFakeBoldText(false);
			titleTv.getPaint().setFakeBoldText(false);
		}else{
			timeTv.getPaint().setFakeBoldText(true);
			contentTv.getPaint().setFakeBoldText(true);
			titleTv.getPaint().setFakeBoldText(true);
			timeTv.setTextColor(R.color.black);
			contentTv.setTextColor(R.color.black);
			titleTv.setTextColor(R.color.black);
		}
		
		view.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				InfoSql.setIsRead(context, id);
				
				cursor.requery();
				UI20_Adapter.this.notifyDataSetChanged();
			}
		});
	}

}
