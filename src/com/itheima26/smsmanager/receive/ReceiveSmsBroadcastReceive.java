package com.itheima26.smsmanager.receive;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ReceiveSmsBroadcastReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		int resultCode = getResultCode();
		if(resultCode == Activity.RESULT_OK) {
			// ���ŷ��ͳɹ�
			Toast.makeText(context, "���ͳɹ�", 0).show();
		} else {
			Toast.makeText(context, "����ʧ��", 0).show();
		}
	}

}
