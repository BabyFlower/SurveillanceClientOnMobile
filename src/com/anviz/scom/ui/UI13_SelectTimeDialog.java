package com.anviz.scom.ui;

import com.anviz.scom.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

/**
 * 选择时间对话框
 * 
 * @author 8444
 * 
 */
public class UI13_SelectTimeDialog extends Dialog {

	private Handler handler;

	public UI13_SelectTimeDialog(Context context, Handler handler) {
		super(context);
		this.handler = handler;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ui13_select_time_dialog);

		setTitle("请选择日期及时间");

		final DatePicker dp = (DatePicker) findViewById(R.id.ui13_dialog_dp);
		final TimePicker tp = (TimePicker) findViewById(R.id.ui13_dialog_tp);
		Button btn = (Button) findViewById(R.id.ui13_dialog_btn);

		btn.setOnClickListener(new android.view.View.OnClickListener() {

			public void onClick(View view) {
				StringBuffer sb = new StringBuffer();
				sb.append(String.format("%d-%02d-%02d %02d:%02d:%02d", dp.getYear(),
						dp.getMonth() + 1, dp.getDayOfMonth(),
						tp.getCurrentHour(), tp.getCurrentMinute(), 0));
				
				Message msg = handler.obtainMessage(1, sb.toString());
				handler.sendMessage(msg);
				cancel();
			}
		});
	}
}
