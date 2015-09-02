package com.anviz.scom.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.anviz.scom.R;
import com.anviz.scom.sqlite.LiveSql;

/**
 * 设备管理列表适配器
 * @author 8444
 *
 */
public class UI15_Adapter extends ResourceCursorAdapter{
	
	
	@SuppressWarnings("deprecation")
	public UI15_Adapter(Context context, Cursor cursor) {
		super(context, R.layout.ui15_adapter, cursor);
		
	}

	@SuppressWarnings("deprecation")
	public void bindView(View view, final Context context, final Cursor cursor) {
		ImageView iv = (ImageView) view.findViewById(R.id.ui15_adapter_iv);
		TextView nameTv = (TextView) view.findViewById(R.id.ui15_adapter_name);
		final TextView deleteTv = (TextView) view.findViewById(R.id.ui15_adapter_delete);
		
		
		final String name = cursor.getString(2);
		final int id = cursor.getInt(0);

		
		
		
//		Item单击事件
		view.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				
				Intent it = new Intent(context, UI19_DeviceInfoActivity.class);
				Bundle mBundle = new Bundle();
				mBundle.putString("name", name);
				it.putExtras(mBundle);
				context.startActivity(it);
			}
		});
		
//		Item长按事件
		view.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View view) {
				if(deleteTv.getVisibility() == View.VISIBLE){
//					添加伸展缩进动画
					Animation mAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_PARENT, 1.0f, Animation.INFINITE, 1.0f);
					mAnimation.setDuration(250);
					mAnimation.setAnimationListener(new AnimationListener() {
						public void onAnimationStart(Animation animation) {
						}
						
						public void onAnimationRepeat(Animation animation) {
						}
						public void onAnimationEnd(Animation animation) {
							deleteTv.setVisibility(View.GONE);
						}
					});
					deleteTv.startAnimation(mAnimation);
				}else{
//					添加伸展缩进动画
					Animation mAnimation = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_PARENT, 1.0f, Animation.INFINITE, 1.0f);
					mAnimation.setDuration(250);
					mAnimation.setAnimationListener(new AnimationListener() {
						public void onAnimationStart(Animation animation) {
						}
						
						public void onAnimationRepeat(Animation animation) {
						}
						public void onAnimationEnd(Animation animation) {
							deleteTv.setVisibility(View.VISIBLE);
						}
					});
					deleteTv.startAnimation(mAnimation);
					deleteTv.setVisibility(View.VISIBLE);
				}
				return true;
			}
		});
		
//		初始化 删除 按钮隐藏
		deleteTv.setVisibility(View.GONE);
		deleteTv.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				LiveSql.delete(context, id);
//				下两句是数据源刷新代码
				cursor.requery();
				UI15_Adapter.this.notifyDataSetChanged();
			}
		});
		
		nameTv.setText(name);
		
//		获取数据
		byte[] b = cursor.getBlob(1);
		
//		将获取的数据转换成drawable
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, null);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        Drawable drawable = bitmapDrawable;
		iv.setImageDrawable(drawable);
	}

}
