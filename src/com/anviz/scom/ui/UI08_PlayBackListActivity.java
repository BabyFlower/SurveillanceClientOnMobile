package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.PlayBackSql;

/**
 * 录像回放列表
 * 
 * @author 8444
 * 
 */
public class UI08_PlayBackListActivity extends Activity {

	private ListView lv;
	private UI08_Adapter adapter;
	private Cursor cursor;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui08);

//		PlayBackSql.insert(this, R.drawable.play_default, "测试1", "2014-01-18",
//				"120Min", 1);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试1", "2014-01-19",
//				"120Min", 1);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试1", "2014-01-20",
//				"120Min", 1);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试1", "2014-01-26",
//				"120Min", 1);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试3", "2014-01-22",
//				"120Min", 1);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试2", "2014-01-23",
//				"120Min", 1);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试2", "2014-01-12",
//				"120Min", 0);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试3", "2014-01-12",
//				"120Min", 1);
//
//		PlayBackSql.insert(this, R.drawable.play_default, "测试1", "2014-01-15",
//				"120Min", 0);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试1", "2014-01-16",
//				"120Min", 0);
//		PlayBackSql.insert(this, R.drawable.play_default, "测试1", "2014-01-17",
//				"120Min", 0);

		lv = (ListView) findViewById(R.id.ui08_lv);

		cursor = PlayBackSql.queryByGroup(this);
		adapter = new UI08_Adapter(this, cursor);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Cursor cursor = (Cursor) lv.getItemAtPosition(position);
				final String name = cursor.getString(2);
				Intent it = new Intent(UI08_PlayBackListActivity.this,
						UI12_PlayBackInfoListActivity.class);
				it.putExtra("name", name);
				UI08_PlayBackListActivity.this.startActivity(it);
			}
		});
	}

}
