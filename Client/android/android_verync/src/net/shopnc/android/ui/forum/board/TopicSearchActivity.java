/**
 *  ClassName: TopicSearchActivity.java
 *  created on 2012-3-7
 *  Copyrights 2011-2012 qjyong All rights reserved.
 *  site: http://blog.csdn.net/qjyong
 *  email: qjyong@gmail.com
 */
package net.shopnc.android.ui.forum.board;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import net.shopnc.android.R;
import net.shopnc.android.adapter.TopicListViewAdapter;
import net.shopnc.android.common.MyApp;
import net.shopnc.android.common.SystemHelper;
import net.shopnc.android.handler.RemoteDataHandler;
import net.shopnc.android.model.ResponseData;
import net.shopnc.android.model.Topic;
import net.shopnc.android.ui.forum.topic.SendTopicActivity;
import net.shopnc.android.ui.forum.topic.TopicDetailActivity;
import net.shopnc.android.ui.more.LoginActivity;
import net.shopnc.android.widget.PullView;
import net.shopnc.android.widget.PullView.UpdateHandle;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 选定版块下的帖子搜索
 * @author qjyong
 */
public class TopicSearchActivity extends ListActivity implements UpdateHandle {
	public static final String TAG = "TopicSearchActivity";
	
	private TopicListActivity parent;
	private MyApp myApp;
	private ImageButton btn_right;
	private TextView txt_title;
	
	private PullView pv;
	private TopicListViewAdapter adapter;
	private ArrayList<Topic> datas;
	
	private ImageButton btn_pager_prev;
	private ImageButton btn_pager_next;
	private TextView txt_pager_info;
	private ImageButton btn_board_favorite;
	
	private String boardName;
	private long fid;
	private String url;
	
	private ImageButton btn_search;
	private EditText txt_keyword;
	private String keyword;
	
	private int pagesize;
	private int pageno = 1;
	private long count = 0;
	private long totalpage= 0;

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.board_topic_search);
		parent = (TopicListActivity)this.getParent();
		
		Intent intent = this.getIntent();
	    this.boardName = intent.getStringExtra("boardName");
	    this.fid = intent.getExtras().getLong("fid");
	    this.url = intent.getStringExtra("url");
	    
	    myApp = (MyApp)this.getApplication();
		pagesize = myApp.getPageSize();
		
		initTopButton();
		initPagerBar();
		
		initSearchBar();
		
		initPullView();
	}
	
	@Override
	public void onUpdate() {
		loadPage(pageno = 1);
	}
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(TopicSearchActivity.this, TopicDetailActivity.class);
		
		Topic topic = (Topic)TopicSearchActivity.this.adapter.getItem(position);
		Log.d(TAG, topic.toString());
		
		intent.putExtra(Topic.TOPIC_TAG, topic);
		
		TopicSearchActivity.this.startActivity(intent);
	}
	
	private void initSearchBar(){
		btn_search = (ImageButton)this.findViewById(R.id.btn_search);
		txt_keyword = (EditText) this.findViewById(R.id.txt_keyword);
		
		btn_search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String temp = txt_keyword.getText().toString();
				if(null == temp || "".equals(temp)){
					keyword = "";
					Toast.makeText(TopicSearchActivity.this, "请输入要搜索的关键字！", 0).show();
				}else{
					try {
						keyword = URLEncoder.encode(temp, HTTP.UTF_8);
						
						pv.startUpdate();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
				}
			}
		});
	}
	
	private void initPullView(){
		pv = (PullView)this.findViewById(R.id.pv);
        this.pv.setUpdateHandle(this);
        
        datas = new ArrayList<Topic>();
		adapter = new TopicListViewAdapter(TopicSearchActivity.this);
		setListAdapter(adapter);
	}
	
	
	private void initPagerBar(){
		txt_pager_info = (TextView)this.findViewById(R.id.txt_pager_info);
		
		btn_pager_prev = (ImageButton)this.findViewById(R.id.btn_pager_prev);
		btn_pager_next = (ImageButton)this.findViewById(R.id.btn_pager_next);
		btn_board_favorite = (ImageButton)this.findViewById(R.id.btn_board_favorite);
		btn_board_favorite.setVisibility(View.INVISIBLE);
		
		MyOnClickListener listener = new MyOnClickListener();
		btn_pager_prev.setOnClickListener(listener);
		txt_pager_info.setOnClickListener(listener);
		btn_pager_next.setOnClickListener(listener);
		btn_board_favorite.setOnClickListener(listener);
	}
	
	private void loadPage(final int pageno){
		if(null == keyword || "".equals(keyword)){
			Toast.makeText(TopicSearchActivity.this, "请输入要搜索的关键字！", 0).show();
			pv.endUpdate(); //更新完成后的回调方法,用于隐藏刷新面板
			return;
		}else{
			if(-1 == SystemHelper.getNetworkType(this)){
				pv.endUpdate(); 
				Toast.makeText(TopicSearchActivity.this, "网络连接失败，请检查设备!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			String  real_url = url+fid + "&keyword=" + keyword;
			
			Log.d(TAG, "url="+real_url);
			RemoteDataHandler.asyncGet(real_url, pagesize, pageno, new RemoteDataHandler.Callback() {
				@Override
				public void dataLoaded(ResponseData data) {
					
					pv.endUpdate(); //更新完成后的回调方法,用于隐藏刷新面板
					
					if(data.getCode() == HttpStatus.SC_OK){
						Log.d(TAG, "RemoteDataHanlder---dataLoaded");
						String json = data.getJson();
						
						Log.d(TAG, json);
						
						//设置分页信息...
						count = data.getCount();
						if(count > 0){
							totalpage = ((count + pagesize - 1) / pagesize);
							txt_pager_info.setText(pageno + "/" + totalpage);
						}
						
						if(pageno == 1){
							datas.clear();
						}
						datas.addAll(Topic.newInstanceList(json));
						Log.d(TAG, datas.toString());
						
						adapter.setDatas(datas);
						adapter.notifyDataSetChanged();
					}
				}
			});
		}
	}
	
	private void initTopButton(){
		
		txt_title = (TextView)parent.findViewById(R.id.txt_title);
		txt_title.setText(this.boardName);
		
		btn_right = (ImageButton)parent.findViewById(R.id.btn_right);
		
		if(null != myApp.getUid() && !"".equals(myApp.getUid()) 
				&& null != myApp.getSid() && !"".equals(myApp.getSid())){
			btn_right.setBackgroundResource(R.drawable.btn_sendtopic);
		}else{
			btn_right.setBackgroundResource(R.drawable.btn_login);
		}
		
		btn_right.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null != myApp.getUid() && !"".equals(myApp.getUid()) 
						&& null != myApp.getSid() && !"".equals(myApp.getSid())){//跳转到发帖
					Intent intent = new Intent(TopicSearchActivity.this, SendTopicActivity.class);
					intent.putExtra("fid", TopicSearchActivity.this.fid);
					intent.putExtra("boardName", TopicSearchActivity.this.boardName);
					TopicSearchActivity.this.startActivityForResult(intent, 100);
				}else{//未登录先跳转到登录
					//Toast.makeText(TopicSearchActivity.this, "发帖请先登录！", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(TopicSearchActivity.this, LoginActivity.class);
					TopicSearchActivity.this.startActivityForResult(intent, 200);
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode){  
		case 100:  //发帖成功
        	Log.d(TAG, "post succ, load datas");
        	pageno = 1;
        	pv.startUpdate();
        	break;
        case 200: //登录成功 
        	btn_right.setBackgroundResource(R.drawable.btn_sendtopic);
        	break;
		}
	}
	
	class MyOnClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			Log.d(TAG, ""+v.getId());
			switch(v.getId()){
			case R.id.btn_pager_prev:
				if(totalpage > 1 && pageno > 1){
					pageno -= 1;
					loadPage(--pageno);
				}else{
					Toast.makeText(TopicSearchActivity.this, "已经是第1页了!", 0).show();
				}
				break;
			case R.id.txt_pager_info:
				//TODO 输入页号
				break;
			case R.id.btn_pager_next:
				if(totalpage > 1 && pageno < totalpage){
					pageno += 1;
					loadPage(++pageno);
				}else{
					Toast.makeText(TopicSearchActivity.this, "已经是底页了!", 0).show();
				}
				break;
			case R.id.btn_board_favorite:
				break;
			}
		}
	}
}
