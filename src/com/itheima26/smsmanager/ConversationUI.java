package com.itheima26.smsmanager;

import java.util.HashSet;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.Sms;
import com.itheima26.smsmanager.utils.Utils;

/**
 * @author andong
 * �Ự
 */
public class ConversationUI extends Activity implements OnClickListener, 
	OnItemClickListener, OnItemLongClickListener {
	
	protected static final String TAG = "ConversationUI";
	private static final int SEARCH_ID = 0;
	private static final int EDIT_ID = 1;
	private static final int CANCEL_EDIT_ID = 2;
	
	private final int LIST_STATE = -1;
	private final int EDIT_STATE = -2;
	private int currentState = LIST_STATE;		// ��ǰĬ�ϵ�״̬Ϊ�б�״̬
	
	private HashSet<Integer> mMultiDeleteSet;
	
	
	private String[] projection = {
			"sms.thread_id AS _id",
			"sms.body AS body",
			"groups.msg_count AS count",
			"sms.date AS date",
			"sms.address AS address"
	};
	private final int THREAD_ID_COLUMN_INDEX = 0;
	private final int BODY_COLUMN_INDEX = 1;
	private final int COUNT_COLUMN_INDEX = 2;
	private final int DATE_COLUMN_INDEX = 3;
	private final int ADDRESS_COLUMN_INDEX = 4;
	private ConversationAdapter mAdapter;
	
	private Button btnNewMessage;
	private Button btnSelectAll;
	private Button btnCancelSelect;
	private Button btnDeleteMessage;
	private ListView mListView;
	private ProgressDialog mProgressDialog;
	private boolean isStop = false;		// �Ƿ�ֹͣ

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversation);
		
		initView();
		prepareData();
		
	}
	
	@Override
	public void onBackPressed() {
		if(currentState == EDIT_STATE) {
			currentState = LIST_STATE;
			mMultiDeleteSet.clear();
			refreshState();
			return;
		}
		super.onBackPressed();
	}

	/**
	 * �˷����Ǵ���options�˵�����, ֻ�ᱻ����һ��
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, SEARCH_ID, 0, "����");
		menu.add(0, EDIT_ID, 0, "�༭");
		menu.add(0, CANCEL_EDIT_ID, 0, "ȡ���༭");
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * ���˵���Ҫ��ʾ����Ļ��ʱ, �ص��˷���
	 * ������ʾ��һ���˵�
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(currentState == EDIT_STATE) {
			// ��ʾȡ���༭, ������������
			menu.findItem(SEARCH_ID).setVisible(false);
			menu.findItem(EDIT_ID).setVisible(false);
			menu.findItem(CANCEL_EDIT_ID).setVisible(true);
		} else {
			menu.findItem(SEARCH_ID).setVisible(true);
			menu.findItem(EDIT_ID).setVisible(true);
			menu.findItem(CANCEL_EDIT_ID).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * ��options�˵���ѡ��ʱ�ص�
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SEARCH_ID:		// �����˵���ѡ��
			onSearchRequested();		// ���������Ի���
			break;
		case EDIT_ID:		// �༭�˵�
			currentState = EDIT_STATE;
			refreshState();
			break;
		case CANCEL_EDIT_ID:	// ȡ���༭
			currentState = LIST_STATE;
			mMultiDeleteSet.clear();
			refreshState();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * ˢ��״̬
	 */
	private void refreshState() {
		if(currentState == EDIT_STATE) {
			// �½���Ϣ����, ������ť��ʾ, ÿһ��item��Ҫ��ʾһ��checkBox 
			btnNewMessage.setVisibility(View.GONE);
			btnSelectAll.setVisibility(View.VISIBLE);
			btnCancelSelect.setVisibility(View.VISIBLE);
			btnDeleteMessage.setVisibility(View.VISIBLE);
			
			if(mMultiDeleteSet.size() == 0) {
				// û��ѡ���κ�checkbox
				btnCancelSelect.setEnabled(false);
				btnDeleteMessage.setEnabled(false);
			} else {
				btnCancelSelect.setEnabled(true);
				btnDeleteMessage.setEnabled(true);
			}
			
			// ȫѡ
			btnSelectAll.setEnabled(mMultiDeleteSet.size() != mListView.getCount());
		} else {
			// �½���Ϣ��ʾ, ����������
			btnNewMessage.setVisibility(View.VISIBLE);
			btnSelectAll.setVisibility(View.GONE);
			btnCancelSelect.setVisibility(View.GONE);
			btnDeleteMessage.setVisibility(View.GONE);
		}
	}

	private void initView() {
		mMultiDeleteSet = new HashSet<Integer>();
		
		mListView = (ListView) findViewById(R.id.lv_conversation);
		btnNewMessage = (Button) findViewById(R.id.btn_conversation_new_message);
		btnSelectAll = (Button) findViewById(R.id.btn_conversation_select_all);
		btnCancelSelect = (Button) findViewById(R.id.btn_conversation_cancel_select);
		btnDeleteMessage = (Button) findViewById(R.id.btn_conversation_delete_message);
		
		btnNewMessage.setOnClickListener(this);
		btnSelectAll.setOnClickListener(this);
		btnCancelSelect.setOnClickListener(this);
		btnDeleteMessage.setOnClickListener(this);
		
		mAdapter = new ConversationAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
	}

	/**
	 * �첽��ѯ����
	 */
	private void prepareData() {
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		String selection = null;
		
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		if(!TextUtils.isEmpty(title)) {
			setTitle(title);
			String threadIDs = intent.getStringExtra("threadIDs");
			selection = "thread_id in " + threadIDs;
		}
		
		asyncQuery.startQuery(0, mAdapter, Sms.CONVERSATION_URI, projection, selection, null, "date desc");
	}
	
	class ConversationAdapter extends CursorAdapter {
		
		private ConversationHolderView mHolder;

		public ConversationAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		/**
		 * ����һ��view
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.conversation_item, null);
			mHolder = new ConversationHolderView();
			mHolder.checkBox = (CheckBox) view.findViewById(R.id.cb_conversation_item);
			mHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_conversation_item_icon);
			mHolder.tvName = (TextView) view.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvDate = (TextView) view.findViewById(R.id.tv_conversation_item_date);
			mHolder.tvBody = (TextView) view.findViewById(R.id.tv_conversation_item_body);
			view.setTag(mHolder);
			return view;
		}

		/**
		 * ������
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (ConversationHolderView) view.getTag();
			int id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			int count = cursor.getInt(COUNT_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			// �жϵ�ǰ��״̬�Ƿ��Ǳ༭
			if(currentState == EDIT_STATE) {
				// ��ʾcheckbox
				mHolder.checkBox.setVisibility(View.VISIBLE);
				
				// ��ǰ�ĻỰid�Ƿ������deleteSet������
				mHolder.checkBox.setChecked(mMultiDeleteSet.contains(id));
			} else {
				// ����checkbox
				mHolder.checkBox.setVisibility(View.GONE);
			}
			
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				// ��ʾ����
				mHolder.tvName.setText(address + "(" + count + ")");
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				// ��ʾ����
				mHolder.tvName.setText(contactName + "(" + count + ")");
				
				Bitmap contactIcon = Utils.getContactIcon(getContentResolver(), address);
				if(contactIcon != null) {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
				} else {
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				}
			}
			
			String strDate = null;
			if(DateUtils.isToday(date)) {
				// ��ʾʱ��
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				// ��ʾ����
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			mHolder.tvDate.setText(strDate);
			
			mHolder.tvBody.setText(body);
		}
	}
	
	public class ConversationHolderView {
		public CheckBox checkBox;
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_conversation_new_message: // �½���Ϣ
			startActivity(new Intent(this, NewMessageUI.class));
			break;
		case R.id.btn_conversation_select_all: // ȫѡ
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(-1);		// ��λ����ʼ��λ��
			
			while(cursor.moveToNext()) {
				mMultiDeleteSet.add(cursor.getInt(THREAD_ID_COLUMN_INDEX));
			}
			mAdapter.notifyDataSetChanged();	// ˢ������
			refreshState();
			break;
		case R.id.btn_conversation_cancel_select: // ȡ��ѡ��
			mMultiDeleteSet.clear();
			mAdapter.notifyDataSetChanged();	// ˢ������
			refreshState();
			break;
		case R.id.btn_conversation_delete_message: // ɾ����Ϣ
			showConfirmDeleteDialog();
			break;
		default:
			break;
		}
	}

	/**
	 * ȷ��ɾ���Ի���
	 */
	private void showConfirmDeleteDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);		// ����ͼ��
		builder.setTitle("ɾ��");
		builder.setMessage("ȷ��ɾ��ѡ�еĻỰ��?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "ȷ��ɾ��");
				
				// �������ȶԻ���
				showDeleteProgressDialog();
				isStop = false;
				// �������߳�, ����ɾ������, ÿɾ��һ������, ���½�����
				new Thread(new DeleteRunnable()).start();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
	
	/**
	 * ����ɾ�����ȶԻ���
	 */
	@SuppressWarnings("deprecation")
	private void showDeleteProgressDialog() {
		mProgressDialog = new ProgressDialog(this);
		// �������ֵ
		mProgressDialog.setMax(mMultiDeleteSet.size());
		// ���ý���������ʾΪ����
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setButton("ȡ��", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "��ֹɾ��");
				isStop = true;
			}
		});
		mProgressDialog.show();
		mProgressDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				currentState = LIST_STATE;
				refreshState();
			}
		});
	}
	
	/**
	 * @author andong
	 * ɾ���Ự������
	 */
	class DeleteRunnable implements Runnable {

		@Override
		public void run() {
			// ɾ���Ự
			
			Iterator<Integer> iterator = mMultiDeleteSet.iterator();
			
			int thread_id;
			String where;
			String[] selectionArgs;
			while(iterator.hasNext()) {
				
				if(isStop) {
					break;
				}
				
				thread_id = iterator.next();
				where = "thread_id = ?";
				selectionArgs = new String[]{String.valueOf(thread_id)};
				getContentResolver().delete(Sms.SMS_URI, where, selectionArgs);
				
				SystemClock.sleep(2000);
				
				// ���½�����
				mProgressDialog.incrementProgressBy(1);
			}
			
			mMultiDeleteSet.clear();
			mProgressDialog.dismiss();
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// �ѵ�ǰ�������item�ĻỰid��ӵ�������, ˢ��checkbox
		Cursor cursor = mAdapter.getCursor();
		// �ƶ�����ǰ���������
		cursor.moveToPosition(position);
		
		// �Ự��id
		int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
		String address = cursor.getString(ADDRESS_COLUMN_INDEX);
		
		if(currentState == EDIT_STATE) {
			
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_conversation_item);
			
			if(checkBox.isChecked()) {
				// �Ƴ�id
				mMultiDeleteSet.remove(thread_id);
			} else {
				mMultiDeleteSet.add(thread_id);
			}
			checkBox.setChecked(!checkBox.isChecked());
			
			// ÿһ�ε��ˢ��һ�°�ť��״̬
			refreshState();
		} else {
			Intent intent = new Intent(this, ConversationDetailUI.class);
			intent.putExtra("thread_id", thread_id);
			intent.putExtra("address", address);
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		
		// �жϵĵ�ǰ�Ự�Ƿ���ӹ�Ⱥ��
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String thread_id = cursor.getString(THREAD_ID_COLUMN_INDEX);
		
		String groupName = getGroupName(thread_id);
		if(!TextUtils.isEmpty(groupName)) {
			Toast.makeText(this, "�ûỰ�Ѿ������\"" + groupName + "\"��", 0).show();
		} else {
			// ����ѡ��Ⱥ��Ի���
			showSelectGroupDialog(thread_id);
		}
		return true;
	}
	
	/**
	 * ����ѡ��Ⱥ��ĶԻ���
	 * @param thread_id
	 */
	private void showSelectGroupDialog(final String thread_id) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("ѡ��Ҫ�����Ⱥ��");
		// ������е�Ⱥ��
		Cursor cursor = getContentResolver().query(Sms.GROUPS_QUERY_ALL_URI, null, null, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			final String[] groupNameArray = new String[cursor.getCount()];
			final String[] groupIDArray = new String[cursor.getCount()];
			
			while(cursor.moveToNext()) {
				groupIDArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("_id"));
				groupNameArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("group_name"));
			}
			
			builder.setItems(groupNameArray, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
//					Log.i(TAG, "��ǰѡ�е�Ⱥ����: " + groupNameArray[which]);
					
					// �ѵ�ǰ�Ự��ӵ�ѡ�е�Ⱥ����
					addGroup(groupIDArray[which], thread_id);
				}
			});
			builder.show();
		}
	}
	
	/**
	 * ��ӵ�Ⱥ��
	 * @param group_id
	 * @param thread_id
	 */
	private void addGroup(String group_id, String thread_id) {
		// ��������ϵ�������һ������
		ContentValues values = new ContentValues();
		values.put("group_id", group_id);
		values.put("thread_id", thread_id);
		Uri uri = getContentResolver().insert(Sms.THREAD_GROUP_INSERT_URI, values);
		
		if(ContentUris.parseId(uri) != -1) {
			Toast.makeText(this, "��ӳɹ�", 0).show();
		} else {
			Toast.makeText(this, "���ʧ��", 0).show();
		}
	}

	/**
	 * ���ݻỰ��id��ȡȺ�������
	 * @param thread_id
	 * @return
	 */
	private String getGroupName(String thread_id) {
		// ���ݻỰ��id��ȡȺ���id 
		String selection = "thread_id = " + thread_id;
		Cursor cursor = getContentResolver().query(Sms.THREAD_GROUP_QUERY_ALL_URI, new String[]{"group_id"}, 
				selection, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			String groupId = cursor.getString(0);
			cursor.close();
			//���Ⱥ��id��Ϊnull
			if(!TextUtils.isEmpty(groupId)) {
				// ȡȺ����аѶ�Ӧ����ȡ��
				selection = "_id = " + groupId;
				cursor = getContentResolver().query(Sms.GROUPS_QUERY_ALL_URI, new String[]{"group_name"}, 
						selection, null, null);
				if(cursor != null && cursor.moveToFirst()) {
					String groupName = cursor.getString(0);
					return groupName;
				}
			}
		}
		return null;
	}
}
