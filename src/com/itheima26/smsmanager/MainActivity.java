package com.itheima26.smsmanager;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

/**
 * @author andong
 * ��������
 */
public class MainActivity extends TabActivity implements OnClickListener {

    private TabHost mTabHost;
	private View mSlideView;	// ҳǩ�Ļ�������
	private int basicWidth = 0;	// һ�ȷֵĿ��
	private int startX = 0;		// ��ס��һ���ƶ����֮���x���ƫ����

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        
        initTabHost();
    }

	/**
	 * ��ʼ��tabhost
	 */
	private void initTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mSlideView = findViewById(R.id.slide_view);
		final View llConversation = findViewById(R.id.ll_conversation);
		
		llConversation.setOnClickListener(this);
		findViewById(R.id.ll_folder).setOnClickListener(this);
		findViewById(R.id.ll_group).setOnClickListener(this);
		
		// ��ʼ�����������Ŀ�͸�
		
		// �����ͼ���Ĺ۲��߶���, ���һ����ȫ������(layout)���ʱ�ļ����¼�
		llConversation.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			/**
			 * ȫ�ֲ������ʱ�ص�.
			 */
			@Override
			public void onGlobalLayout() {
				// �Ƴ�ȫ�ֲ��ֵļ����¼�
				llConversation.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				
				// �õ��Ự���ֵĲ���, ���ø�������
				LayoutParams lp = (LayoutParams) mSlideView.getLayoutParams();
				lp.width = llConversation.getWidth();
				lp.height = llConversation.getHeight();
				lp.leftMargin = llConversation.getLeft();
				mSlideView.setLayoutParams(lp);
				
				basicWidth = findViewById(R.id.rl_conversation).getWidth();
			}
		});
		
		
		addTabSpec("conversation", "�Ự", R.drawable.tab_conversation, new Intent(this, ConversationUI.class));
		addTabSpec("folder", "�ļ���", R.drawable.tab_folder, new Intent(this, FolderUI.class));
		addTabSpec("group", "Ⱥ��", R.drawable.tab_group, new Intent(this, GroupUI.class));
	}

	/**
	 * ���һ��ҳǩ
	 * @param tag ���
	 * @param label ����
	 * @param icon ͼ��
	 * @param intent ָ���activity
	 */
	private void addTabSpec(String tag, String label, int icon, Intent intent) {
		TabSpec newTabSpec = mTabHost.newTabSpec(tag);
		// ����ҳǩ�ı����ͼ��
		newTabSpec.setIndicator(label, getResources().getDrawable(icon));
		// ����ҳǩָ�����ʾ������activity
		newTabSpec.setContent(intent);
		
		// ���ҳǩ
		mTabHost.addTab(newTabSpec);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ll_conversation: // �л����Ựҳǩ
			if(!"conversation".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("conversation");
				startTranslateAnimation(startX, 0);
				startX = 0;
			}
			break;
		case R.id.ll_folder: // �л����ļ���ҳǩ
			if(!"folder".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("folder");
				startTranslateAnimation(startX, basicWidth);
				startX = basicWidth;
			}
			break;
		case R.id.ll_group: // �л���Ⱥ��ҳǩ
			if(!"group".equals(mTabHost.getCurrentTabTag())) {
				mTabHost.setCurrentTabByTag("group");
				startTranslateAnimation(startX, basicWidth * 2);
				startX = basicWidth * 2;
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * ��������ִ��Ψһ����
	 * @param fromXDelta ��ʼλ��x���ƫ����
	 * @param toXDelta	����λ��x���ƫ����
	 */
	private void startTranslateAnimation(int fromXDelta, int toXDelta) {
		TranslateAnimation ta = new TranslateAnimation(
				fromXDelta, toXDelta, 0, 0);
		ta.setDuration(500);
		ta.setFillAfter(true);	// ͣ���ڶ���������λ����
		mSlideView.startAnimation(ta);
	}
}
