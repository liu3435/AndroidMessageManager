package com.itheima26.smsmanager.provider;

import com.itheima26.smsmanager.db.GroupOpenHelper;
import com.itheima26.smsmanager.utils.Sms;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Switch;

public class GroupContentProvider extends ContentProvider {
	
	private static final String AUTHORITY = "com.itheima26.smsmanager.provider.GroupContentProvider";
	private static final int GROUPS_INSERT = 0;	// ��ӵ�Ⱥ���ƥ����
	private static final int GROUPS_QUERY_ALL = 1;
	private static final int THREAD_GROUP_QUERY_ALL = 2;
	private static final int THREAD_GROUP_INSERT = 3;
	private static final int GROUPS_UPDATE = 4;
	private static final int GROUPS_SINGLE_DELETE = 5;
	private static UriMatcher uriMatcher;
	
	private GroupOpenHelper mOpenHelper;
	private final String GROUPS_TABLE = "groups";	// Ⱥ�����
	private final String THREAD_GROUP_TABLE = "thread_group";	// ������ϵ����
	
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		// content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/insert
		uriMatcher.addURI(AUTHORITY, "groups/insert", GROUPS_INSERT);
		

		// content://com.itheima26.smsmanager.provider.GroupContentProvider/groups
		uriMatcher.addURI(AUTHORITY, "groups", GROUPS_QUERY_ALL);
		

		// content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group
		uriMatcher.addURI(AUTHORITY, "thread_group", THREAD_GROUP_QUERY_ALL);
		

		// content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group/insert
		uriMatcher.addURI(AUTHORITY, "thread_group/insert", THREAD_GROUP_INSERT);
		
		// content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/update
		uriMatcher.addURI(AUTHORITY, "groups/update", GROUPS_UPDATE);
		
		// content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/delete/#
		uriMatcher.addURI(AUTHORITY, "groups/delete/#", GROUPS_SINGLE_DELETE);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = GroupOpenHelper.getInstance(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_QUERY_ALL:		// ��ѯ���е�Ⱥ������
			if(db.isOpen()) {
				Cursor cursor = db.query(GROUPS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
				// ���α���������һ��֪ͨ��uri
				cursor.setNotificationUri(getContext().getContentResolver(), Sms.GROUPS_QUERY_ALL_URI);
				return cursor;
			}
		case THREAD_GROUP_QUERY_ALL:	// ��ѯ���й�����ϵ�������
			if(db.isOpen()) {
				Cursor cursor = db.query(THREAD_GROUP_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
				return cursor;
			}
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri : " + uri);
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case GROUPS_INSERT:	// ��ӵ�Ⱥ��
			if(db.isOpen()) {
				long id = db.insert(GROUPS_TABLE, null, values);
				// ֪ͨSms.GROUPS_QUERY_ALL_URI, ���ݸı���, ����ִ�����²�ѯ����, ��������
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI, null);
				return ContentUris.withAppendedId(uri, id);
			}
			break;
		case THREAD_GROUP_INSERT:		// ��ӵ�������ϵ����
			if(db.isOpen()) {
				long id = db.insert(THREAD_GROUP_TABLE, null, values);
				return ContentUris.withAppendedId(uri, id);
			}
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri : " + uri);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (uriMatcher.match(uri)) {
		case GROUPS_SINGLE_DELETE: // ��ǰɾ������Ⱥ��
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			if(db.isOpen()) {
				// ����Ⱥ���idɾ��Ⱥ��
				long group_id = ContentUris.parseId(uri);
				String where = "_id = " + group_id;
				int count = db.delete(GROUPS_TABLE, where, null);		// ����ɾ��Ⱥ��
				
				// ֪ͨSms.GROUPS_QUERY_ALL_URI, ���ݸı���, ����ִ�����²�ѯ����, ��������
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI, null);
				
				// ����Ⱥ���idȥ������ϵ����ɾ����ӦȺ��id���е���Ŀ
				where = "group_id = " + group_id;
				db.delete(THREAD_GROUP_TABLE, where, null);		// ɾ����ǰȺ������й�����ϵ
				return count;
			}
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri : " + uri);
		}
		
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (uriMatcher.match(uri)) {
		case GROUPS_UPDATE:	// ����Ⱥ���Ĳ���
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			if(db.isOpen()) {
				int count = db.update(GROUPS_TABLE, values, selection, selectionArgs);
				// ֪ͨSms.GROUPS_QUERY_ALL_URI, ���ݸı���, ����ִ�����²�ѯ����, ��������
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI, null);
				return count;
			}
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri : " + uri);
		}
		return 0;
	}

}
