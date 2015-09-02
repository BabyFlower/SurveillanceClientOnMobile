package com.anviz.scom.control;

import com.anviz.scom.ui.UI05_RegisterActivity;

import android.content.Context;
import android.content.Intent;

/**
 * UI页面，接收用户输入，页面控制
 * @author 8444
 *
 */
public class UIControl {
	
	/**打开注册页面*/
	public static void startUI05(Context context){
		context.startActivity(new Intent(context, UI05_RegisterActivity.class));
	}
	
}
