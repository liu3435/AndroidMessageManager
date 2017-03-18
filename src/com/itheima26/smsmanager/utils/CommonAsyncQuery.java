package com.itheima26.smsmanager.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;
import android.widget.CursorAdapter;

public class CommonAsyncQuery extends AsyncQueryHandler {

	private static final String TAG = "CommonAsyncQuery";
	private OnQueryNotifyCompleteListener mOnQueryNotifyCompleteListener;

	public CommonAsyncQuery(ContentResolver cr) {
		super(cr);
	}

	/**
	 * ������startQuery��ʼ�첽��ѯ����ʱ, ��ѯ��Ϻ��ѯ�������α�����cursor�ᴫ�ݵ��˷���
	 * ִ�������߳���(��������)
	 * @param token startQuery��������token
	 * @param cookie startQuery��������cookie(CursorAdapter)
	 * @param cursor ��ѯ���������½����
	 */
	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//		Log.i(TAG, "onQueryComplete is calling : token = " + token + ", cookie = " + cookie);

		// ��ˢ��֮ǰ, ���û���һЩ׼������
		if(mOnQueryNotifyCompleteListener != null) {
			mOnQueryNotifyCompleteListener.onPreNotify(token, cookie, cursor);
		}
		
		// ˢ������
		if(cookie != null) {
			notifyAdapter((CursorAdapter) cookie, cursor);
		}
		
		// ֪ͨ�û�ˢ�����, �û����Բ���һЩ����
		if(mOnQueryNotifyCompleteListener != null) {
			mOnQueryNotifyCompleteListener.onPostNotify(token, cookie, cursor);
		}
	}
	
	/**
	 * ��������
	 * @param adapter
	 * @param cursor
	 */
	private void notifyAdapter(CursorAdapter adapter, Cursor cursor) {
		// ��adapterˢ������, ����BaseAdapter�е�notifyDataSetchange
		adapter.changeCursor(cursor);
	}
	
	public void setOnQueryNotifyCompleteListener(OnQueryNotifyCompleteListener l) {
		this.mOnQueryNotifyCompleteListener = l;
	}
	
	/**
	 * @author andong
	 * ����ѯ������ɲ�������������ɵļ����¼�
	 */
	public interface OnQueryNotifyCompleteListener {
		
		/**
		 * ��adapter����֮ǰ�ص��˷���(�û���һЩ��������֮ǰ��׼������)
		 * @param token
		 * @param cookie
		 * @param cursor
		 */
		void onPreNotify(int token, Object cookie, Cursor cursor);
		
		/**
		 * ��ˢ��������֮��ص��˷���
		 * @param token
		 * @param cookie
		 * @param cursor
		 */
		void onPostNotify(int token, Object cookie, Cursor cursor);
	}
}
