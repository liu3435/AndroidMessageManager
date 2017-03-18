package com.itheima26.smsmanager;

import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.Sms;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author andong
 * Ⱥ��
 */
public class GroupUI extends ListActivity implements OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = "GroupUI";
	private GroupAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		init();
		prepareData();
	}
	
	private void prepareData() {
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		asyncQuery.startQuery(0, mAdapter, Sms.GROUPS_QUERY_ALL_URI, null, null, null, null);
	}

	private void init() {
		ListView mListView = getListView();
		
		mAdapter = new GroupAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.create_group, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_create_group) {
//			Log.i(TAG, "����Ⱥ��");
			
			showCreateGroupDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * �����½�Ⱥ��Ի���
	 */
	private void showCreateGroupDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("�½�Ⱥ��");
		final AlertDialog dialog = builder.create();
		
		View view = View.inflate(this, R.layout.create_group, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_create_group_name);
		view.findViewById(R.id.btn_create_group).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String groupName = etName.getText().toString();
				if(!TextUtils.isEmpty(groupName)) {
					createGroup(groupName);
					dialog.dismiss();
				}
			}

		});
		
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
		
		// ��öԻ����������
		LayoutParams lp = dialog.getWindow().getAttributes();

		// ������Ļ�Ŀ��
		
		lp.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		
		dialog.getWindow().setAttributes(lp);
		
	}
	

	/**
	 * ����Ⱥ��
	 * @param groupName
	 */
	private void createGroup(String groupName) {
		ContentValues values = new ContentValues();
		values.put("group_name", groupName);
		Uri uri = getContentResolver().insert(Sms.GROUPS_INSERT_URI, values);
		if(ContentUris.parseId(uri) >= 0) {
			Toast.makeText(this, "Ⱥ�鴴���ɹ�", 0).show();
		}
	}
	
	class GroupAdapter extends CursorAdapter {

		public GroupAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.group_item, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tvName = (TextView) view.findViewById(R.id.tv_group_item_name);
			
			tvName.setText(cursor.getString(cursor.getColumnIndex("group_name")));
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// �ѵ�ǰ�����Ⱥ����ڹ�����ϵ�������еĻỰidȡ����
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String group_id = cursor.getString(cursor.getColumnIndex("_id"));
		String group_name = cursor.getString(cursor.getColumnIndex("group_name"));
		
		String selection = "group_id = " + group_id;
		Cursor c = getContentResolver().query(Sms.THREAD_GROUP_QUERY_ALL_URI, new String[]{"thread_id"}, 
				selection, null, null);
		if(c != null && c.getCount() > 0) {
			// (1, 2, 3)
			StringBuilder sb = new StringBuilder("(");
			
			while(c.moveToNext()) {
				sb.append(c.getString(0) + ", ");
			}
			c.close();
			// (1, 2, 3)
			String threadIDs = sb.substring(0, sb.lastIndexOf(", ")) + ")";
			
			// �ѻỰid���ݸ��Ựҳ��
			Intent intent = new Intent(this, ConversationUI.class);
			intent.putExtra("title", group_name);
			intent.putExtra("threadIDs", threadIDs);
			startActivity(intent);
		}
		
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String group_id = cursor.getString(cursor.getColumnIndex("_id"));
		
		showOperatorDialog(group_id);
		return true;
	}
	
	/**
	 * ���������Ի���
	 */
	private void showOperatorDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setItems(new String[]{"�޸�", "ɾ��"}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0) {
					Log.i(TAG, "�޸�");
					// ����һ���޸ĶԻ���
					showUpdateGroupDialog(group_id);
				} else {
					Log.i(TAG, "ɾ��");
					showDeleteGroupDialog(group_id);
				}
			}

		});
		builder.show();
	}

	/**
	 * ȷ��ɾ��ָ��Ⱥ��id��Ⱥ��
	 * @param group_id
	 */
	private void showDeleteGroupDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("ɾ��");
		builder.setMessage("ɾ��Ⱥ�齫��ɾ��Ⱥ���������������ж��ŵĹ���, ȷ�ϼ���?");
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteGroup(group_id);
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.show();
	}
	
	/**
	 * ɾ��Ⱥ��
	 * @param group_id
	 */
	private void deleteGroup(String group_id) {
		
		Uri uri = ContentUris.withAppendedId(Sms.GROUPS_SINGLE_DELETE_URI, Long.valueOf(group_id));
		
		int count = getContentResolver().delete(uri, null, null);
		if(count > 0) {
			Toast.makeText(this, "ɾ���ɹ�", 0).show();
		} else {
			Toast.makeText(this, "ɾ��ʧ��", 0).show();
		}
	}
	
	/**
	 * �����޸�Ⱥ��ĶԻ���
	 */
	private void showUpdateGroupDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("�޸�Ⱥ��");
		final AlertDialog updateGroupDialog = builder.create();
		
		View view = View.inflate(this, R.layout.create_group, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_create_group_name);
		Button btnCreateGroup = (Button) view.findViewById(R.id.btn_create_group);
		btnCreateGroup.setText("ȷ���޸�");
		btnCreateGroup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// ����Ⱥ��
				String group_name = etName.getText().toString();
				if(!TextUtils.isEmpty(group_name)) {
					updateGroup(group_id, group_name);
					updateGroupDialog.dismiss();
				}
			}
		});
		updateGroupDialog.setView(view, 0, 0, 0, 0);
		updateGroupDialog.show();
		
		LayoutParams lp = updateGroupDialog.getWindow().getAttributes();
		lp.width = (int) (getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		updateGroupDialog.getWindow().setAttributes(lp);
	}
	
	/**
	 * ����Ⱥ��
	 * @param group_id
	 * @param group_name
	 */
	private void updateGroup(String group_id, String group_name) {
		ContentValues values = new ContentValues();
		values.put("group_name", group_name);
		String where = "_id = " + group_id;
		int count = getContentResolver().update(Sms.GROUPS_UPDATE_URI, values, where, null);
		if(count > 0) {
			Toast.makeText(this, "�޸ĳɹ�", 0).show();
		} else {
			Toast.makeText(this, "�޸�ʧ��", 0).show();
		}
	}
}
