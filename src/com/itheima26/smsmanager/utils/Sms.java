package com.itheima26.smsmanager.utils;

import android.net.Uri;

public class Sms {

	/**
	 * ��ѯ�Ự��uri
	 */
	public static final Uri CONVERSATION_URI = Uri.parse("content://sms/conversations");
	
	/**
	 * ����sms��ĵ�uri
	 */
	public static final Uri SMS_URI = Uri.parse("content://sms/");
	
	/**
	 * �ռ����uri
	 */
	public static final Uri INBOX_URI = Uri.parse("content://sms/inbox");
	
	/**
	 * �������uri
	 */
	public static final Uri OUTBOX_URI = Uri.parse("content://sms/outbox");
	
	/**
	 * �ѷ��͵�uri
	 */
	public static final Uri SENT_URI = Uri.parse("content://sms/sent");
	
	/**
	 * �ݸ����uri
	 */
	public static final Uri DRAFT_URI = Uri.parse("content://sms/draft");
	
	/**
	 * ��ӵ�Ⱥ���uri
	 */
	public static final Uri GROUPS_INSERT_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/insert");
	
	/**
	 * ��ѯ����Ⱥ���uri
	 */
	public static final Uri GROUPS_QUERY_ALL_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups");
	
	/**
	 * ��ѯ���й�����ϵ���е����ݵ�uri
	 */
	public static final Uri THREAD_GROUP_QUERY_ALL_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group");
	
	/**
	 * ��ӵ�������ϵ���uri
	 */
	public static final Uri THREAD_GROUP_INSERT_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/thread_group/insert");
	
	/**
	 * ����Ⱥ����uri
	 */
	public static final Uri GROUPS_UPDATE_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/update");
	
	/**
	 * ɾ��Ⱥ���uri(ɾ��Ⱥ���ɾ���������Ĺ�����ϵ)
	 */
	public static final Uri GROUPS_SINGLE_DELETE_URI = Uri.parse("content://com.itheima26.smsmanager.provider.GroupContentProvider/groups/delete/#");
	
	
	public static final int RECEVIE_TYPE = 1;	// ��������: ���յ�
	public static final int SEND_TYPE = 2;		// ��������: ���͵�
	
}
