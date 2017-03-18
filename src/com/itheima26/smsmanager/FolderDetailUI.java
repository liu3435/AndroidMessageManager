package com.itheima26.smsmanager;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima26.smsmanager.utils.CommonAsyncQuery;
import com.itheima26.smsmanager.utils.CommonAsyncQuery.OnQueryNotifyCompleteListener;
import com.itheima26.smsmanager.utils.Utils;

public class FolderDetailUI extends Activity implements 
	OnQueryNotifyCompleteListener, OnItemClickListener,OnClickListener {

	private int index;
	private FolderDetailAdapter mAdapter;
	private final String[] projection = {
			"_id",
			"address",
			"date",
			"body"
	};
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int BODY_COLUMN_INDEX = 3;
	
	private HashMap<Integer, String> dateMap;		// ���ڼ���
	private HashMap<Integer, Integer> smsRealPositionMap;	// ������ʵ�����ļ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_detail);
		
		Intent intent = getIntent();
		
		index = intent.getIntExtra("index", -1);
		
		initTitle();
		initView();
		prepareData();
	}

	private void initView() {
		dateMap = new HashMap<Integer, String>();
		smsRealPositionMap = new HashMap<Integer, Integer>();
		
		ListView mListView = (ListView) findViewById(R.id.lv_folder_detail_sms);
		Button btn_new_sms =(Button) findViewById(R.id.btn_folder_detail_new_message);
		btn_new_sms.setOnClickListener(this);
		mAdapter = new FolderDetailAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	private void prepareData() {
		Uri uri = Utils.getUriFromIndex(index);
		CommonAsyncQuery asyncQuery = new CommonAsyncQuery(getContentResolver());
		asyncQuery.setOnQueryNotifyCompleteListener(this);
		asyncQuery.startQuery(0, mAdapter, uri, projection, null, null, "date desc");
	}

	private void initTitle() {
		switch (index) {
		case 0:
			setTitle("�ռ���");
			break;
		case 1:
			setTitle("������");
			break;
		case 2:
			setTitle("�ѷ���");
			break;
		case 3:
			setTitle("�ݸ���");
			break;
		default:
			break;
		}
	}
	
	class FolderDetailAdapter extends CursorAdapter {
		
		private FolderDetailHolderView mHolder;

		public FolderDetailAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			return dateMap.size() + smsRealPositionMap.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// ��position���������ڼ�����ȡ������ʱ, ���ص���TextView
			if(dateMap.containsKey(position)) {		// ��ǰ��Ҫ��ʾ����
				TextView tvDate = new TextView(FolderDetailUI.this);
				tvDate.setBackgroundResource(android.R.color.darker_gray);
				tvDate.setTextSize(20);
				tvDate.setTextColor(Color.WHITE);
				tvDate.setGravity(Gravity.CENTER);
				tvDate.setText(dateMap.get(position));
				return tvDate;
			}
			
			// ���ص��Ƕ��ŵ�item
			Cursor mCursor = mAdapter.getCursor();
			mCursor.moveToPosition(smsRealPositionMap.get(position));
			
	        View v;
	        if (convertView == null || convertView instanceof TextView) {
	            v = newView(FolderDetailUI.this, mCursor, parent);
	        } else {
	            v = convertView;
	        }
	        bindView(v, FolderDetailUI.this, mCursor);
	        return v;
	    }

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = View.inflate(context, R.layout.conversation_item, null);
			mHolder = new FolderDetailHolderView();
			mHolder.ivIcon = (ImageView) view.findViewById(R.id.iv_conversation_item_icon);
			mHolder.tvName = (TextView) view.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvDate = (TextView) view.findViewById(R.id.tv_conversation_item_date);
			mHolder.tvBody = (TextView) view.findViewById(R.id.tv_conversation_item_body);
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			mHolder = (FolderDetailHolderView) view.getTag();
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				mHolder.tvName.setText(address);
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				mHolder.tvName.setText(contactName);
				
				Bitmap contactIcon = Utils.getContactIcon(getContentResolver(), address);
				if(contactIcon == null) {
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				} else {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
				}
			}
			
			String strDate = null;
			if(DateUtils.isToday(date)) {
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			mHolder.tvDate.setText(strDate);
			
			mHolder.tvBody.setText(body);
		}
		
	}
	
	public class FolderDetailHolderView {
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}

	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		if(cursor != null && cursor.getCount() > 0) {
			
			java.text.DateFormat dateFormat = DateFormat.getDateFormat(this);
			int listViewIndex = 0;	// listview������
			
			String strDate;
			
			while(cursor.moveToNext()) {
				// ��ʽ���������
				strDate = dateFormat.format(cursor.getLong(DATE_COLUMN_INDEX));
				
				// �жϵ�ǰ���ŵ�����, �Ƿ�������ڼ�����, ���������, ��һ��
				if(!dateMap.containsValue(strDate)) {
					dateMap.put(listViewIndex, strDate);
					listViewIndex++;
				}
				
				// �ѵ�ǰ���ŵ���ʵ���������smsRealPositionMap��
				smsRealPositionMap.put(listViewIndex, cursor.getPosition());
				listViewIndex++;
			}
			// ���α긴λ��-1
			cursor.moveToPosition(-1);
		}
		
	}

	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(!dateMap.containsKey(position)) {
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(smsRealPositionMap.get(position));
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			Intent intent = new Intent(this, SmsDetailUI.class);
			intent.putExtra("index", index);
			intent.putExtra("address", address);
			intent.putExtra("date", date);
			intent.putExtra("body", body);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_folder_detail_new_message: // �½���Ϣ
			startActivity(new Intent(this, NewMessageUI.class));
			break;
		default:
			break;
		}
	}
}
