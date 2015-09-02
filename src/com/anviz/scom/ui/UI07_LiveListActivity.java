package com.anviz.scom.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.LiveSql;

/**
 * 实时播放视频列表
 * 
 * @author 8444
 * 
 */
public class UI07_LiveListActivity extends Activity {

	private UI07_DevAdapter mAdapter;
	private ListView lv;

	int i = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui07);

		lv = (ListView) findViewById(R.id.ui07_lv);

		// 按照ID 升序查找 _id desc 为降序
		final Cursor cursor = LiveSql.query(this, null, null, null, null, null,
				"_id");

		LayoutInflater mLayoutInflater = LayoutInflater.from(this);

		View view = mLayoutInflater.inflate(R.layout.ui07_footer_view, null);
		TextView btn = (TextView) view.findViewById(R.id.ui07_footer_view_tv);

		btn.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View view) {
				// 获取视频最后一帧图片，与视频名称，保存到数据库当中
				LiveSql.insert(UI07_LiveListActivity.this, "测试" + i,
						R.drawable.play_default);
				i++;
				cursor.requery();
				mAdapter.notifyDataSetChanged();
			}
		});
		// 加入尾部View,必须在加入适配器之前
		lv.addFooterView(view);

		mAdapter = new UI07_DevAdapter(this, cursor);

		lv.setAdapter(mAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(position == cursor.getCount()){
					return;
				}
				// try {
				// SDLActivity.initTutk("EF49ADMPDWERAH6PWZXJ");
				// System.out.println(TutkCommControl.videoStart("0", "0"));
				
				Intent it = new Intent(UI07_LiveListActivity.this,
						UI03_AnvizPlayerActivity.class);
				it.putExtra("type", 0);
				startActivity(it);
				// } catch (JSONException e) {
				// e.printStackTrace();
				// }
			}
		});
	}

}
