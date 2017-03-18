package com.itheima26.smsmanager.utils;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.util.Log;

public class Utils {

	private static final String TAG = "Utils";

	/**
	 * ����α�����
	 * @param cursor
	 */
	public static void printCursor(Cursor cursor) {
		if(cursor != null && cursor.getCount() > 0) {
			String columnName;
			String columnValue;
			while(cursor.moveToNext()) {
				
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					columnName = cursor.getColumnName(i);
					columnValue = cursor.getString(i);
					Log.i(TAG, "��" + cursor.getPosition() + "��: " + columnName + " = " + columnValue);
				}
			}
			
			cursor.close();
		}
	}
	
	/**
	 * ���ݺ����ȡ��ϵ�˵�����
	 * @param address
	 * @return
	 */
	public static String getContactName(ContentResolver resolver, String address) {
		
		// content://com.android.contacts/phone_lookup/95556
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		
		Cursor cursor = resolver.query(uri, new String[]{"display_name"}, null, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			String contactName = cursor.getString(0);
			cursor.close();
			return contactName;
		}
		return null;
	}
	
	/**
	 * ������ϵ�˵ĺ����ѯ��ϵ�˵�ͷ��
	 * @param resolver
	 * @param address
	 * @return
	 */
	public static Bitmap getContactIcon(ContentResolver resolver, String address) {
		
		// 1.���ݺ���ȡ����ϵ�˵�id
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		
		Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
		
		if(cursor != null && cursor.moveToFirst()) {
			long id = cursor.getLong(0);
			cursor.close();
			
			// 2.����id��ȡ��ϵ�˵�ͷ��
			
			uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
			InputStream is = Contacts.openContactPhotoInputStream(resolver, uri);
			return BitmapFactory.decodeStream(is);
		}
		return null;
	}
	
	/**
	 * ���Ͷ���
	 * @param address
	 * @param content
	 */
	public static void sendMessage(Context context, String address, String content) {
		SmsManager smsManager = SmsManager.getDefault();
		
		// ��70���ַ��ָ����
		ArrayList<String> divideMessage = smsManager.divideMessage(content);
		
		// ��������Ϊ��ʿintent
		Intent intent = new Intent("com.itheima26.smsmanager.receive.ReceiveSmsBroadcastReceive");
		
		PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, 
				intent, PendingIntent.FLAG_ONE_SHOT);
		
		for (String sms : divideMessage) {
			smsManager.sendTextMessage(
					address, 	// �����˵ĺ���
					null, 				// �������ĵĺ���
					sms, 					// ���ŵ�����
					sentIntent, 			// ���ͳɹ��Ļص��㲥
					null);		// ���ճɹ��Ļص��㲥
		}
		
		// ���뵽���ݿ�
		writeMessage(context, address, content);
	}
	
	/**
	 * ��ӵ����ݿ�
	 * @param context
	 * @param address
	 * @param content
	 */
	public static void writeMessage(Context context, String address, String content) {
		ContentValues values = new ContentValues();
		values.put("address", address);
		values.put("type", Sms.SEND_TYPE);
		values.put("body", content);
		
		context.getContentResolver().insert(Sms.SMS_URI, values);
	}
	
	/**
	 * ���ݸ�����uri��ѯ��ϵ�˵�id
	 * @param resolver
	 * @param uri ��ϵ�˵�uri
	 * @return �������-1, ����ǰ��ϵ��û����Ӻ���
	 */
	public static long getContactID(ContentResolver resolver, Uri uri) {
		Cursor cursor = resolver.query(uri, new String[]{"_id", "has_phone_number"}, null, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			int hasPhoneNumber = cursor.getInt(1);
			if(hasPhoneNumber > 0) {
				long id = cursor.getLong(0);
				cursor.close();
				return id;
			}
		}
		return -1;
	}
	
	/**
	 * ������ϵ�˵�id��ȡ��ϵ�˵ĺ���
	 * @param resolver
	 * @param id
	 * @return
	 */
	public static String getContactAddress(ContentResolver resolver, long id) {
		String selection = "contact_id = ?";
		String selectionArgs[] = {String.valueOf(id)};
		Cursor cursor = resolver.query(Phone.CONTENT_URI, new String[]{"data1"}, selection, selectionArgs, null);
		if(cursor != null && cursor.moveToFirst()) {
			String address = cursor.getString(0);
			cursor.close();
			return address;
		}
		return null;
	}
	
	/**
	 * ������������ָ����uri
	 * @param position
	 * @return
	 */
	public static Uri getUriFromIndex(int position) {
		switch (position) {
		case 0:
			return Sms.INBOX_URI;
		case 1:
			return Sms.OUTBOX_URI;
		case 2:
			return Sms.SENT_URI;
		case 3:
			return Sms.DRAFT_URI;
		default:
			break;
		}
		return null;
	}
}