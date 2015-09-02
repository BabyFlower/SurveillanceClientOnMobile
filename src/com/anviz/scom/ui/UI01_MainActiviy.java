package com.anviz.scom.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.libsdl.app.SDLActivity;

import com.anviz.scom.R;
import com.anviz.scom.struct.Devipc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * 系统入口
 * 
 * @author 8444
 */
public class UI01_MainActiviy extends Activity {
	private ListView lv;
	private Button startBtn;
	private List<Devipc> data = new ArrayList<Devipc>();
	private UI01_DevAdapter adapter;
	private EditText uidEt;
	
	/** 要查看的列表索引 第次四个 */
	private int index = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui01);
		uidEt = (EditText) findViewById(R.id.ui01_et);
		lv = (ListView) findViewById(R.id.ui01_lv);
		startBtn = (Button) findViewById(R.id.ui01_start);

		startBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				SDLActivity.initTutk(uidEt.getText().toString());
				String json = SDLActivity.getDevList(index);
				setData(json);
				adapter.notifyDataSetChanged();
			}
		});

		adapter = new UI01_DevAdapter(this, data);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				SDLActivity.startDev(data.get(arg2).getM_ID());
				startActivity(new Intent(UI01_MainActiviy.this,
						UI03_AnvizPlayerActivity.class));
			}
		});
	}

	public void setData(String json) {
		JSONTokener jt = new JSONTokener(json);

		try {
			JSONObject mJSONObject = (JSONObject) jt.nextValue();

			int index = mJSONObject.getInt("iIndex");
			JSONArray mJSONArray = mJSONObject.getJSONArray("stTpDevList");

			for (int i = 0; i < mJSONArray.length(); i++) {
				JSONObject devJson = mJSONArray.getJSONObject(i);
				Devipc mDevipc = new Devipc();

				mDevipc.setIndex(index);
				mDevipc.setM_ID(devJson.getInt("m_ID"));
				mDevipc.setSzDevInfo(devJson.getString("szDevInfo"));
				mDevipc.setSzRtspFile0(devJson.getString("szRtspFile0"));
				mDevipc.setSzRtspFile1(devJson.getString("szRtspFile1"));
				mDevipc.setUiIp(devJson.getInt("uiIp"));
				mDevipc.setUsPort(devJson.getInt("usPort"));
				mDevipc.setUsRtspPort(devJson.getInt("usRtspPort"));
				data.add(mDevipc);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean onCreateOptionsMenu(Menu paramMenu) {
		paramMenu.add(0, 1, 1, "Enter url");
		paramMenu.add(0, 2, 2, "二维码");
		return super.onCreateOptionsMenu(paramMenu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent it = new Intent(this, UI03_AnvizPlayerActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("PATH", "rtsp://192.168.20.50:554/1");
			it.putExtras(mBundle);
			startActivity(it);
			break;
		case 2:
			Intent it2 = new Intent(this, UI02_CaptureActivity.class);
			startActivity(it2);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
