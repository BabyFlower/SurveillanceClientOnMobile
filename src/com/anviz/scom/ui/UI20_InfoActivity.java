package com.anviz.scom.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.InfoSql;

/**
 * 信息
 * 
 * @author 8444
 * 
 */
public class UI20_InfoActivity extends Activity {

	private ListView lv;
	private UI20_Adapter adapter;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui20);

		lv = (ListView) findViewById(R.id.ui20_lv);

		InfoSql.insert(
				this,
				"软件升级",
				"有以下新功能XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
				0, System.currentTimeMillis());

		Cursor cursor = InfoSql.query(this, null);
		adapter = new UI20_Adapter(this, cursor);
		lv.setAdapter(adapter);

		findViewById(R.id.ui20_back).setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				finish();
			}
		});
	}
}
