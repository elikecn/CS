package com.grabparking.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.grabparking.application.GPApplication;
import com.grabparking.application.MyLocationListener;
import com.grabparking.application.NotifyLister;

import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

@SuppressLint({ "NewApi", "NewApi" })
public class MainActivity extends Activity {
	MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private TextView view=null;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	public BDLocation  dblocation=null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.home_title);
		openGPSSettings();// 提示用户打开gps
		view=(TextView)findViewById(R.id.Titletext);
		view.setText("抢车位");
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		SDKInitializer.initialize(getApplicationContext());
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();  
		//普通地图  
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);  
		//卫星地图  
		//mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
		//开启交通图   
		//mBaiduMap.setTrafficEnabled(true);
		//开启热力图  
		//mBaiduMap.setBaiduHeatMapEnabled(true);
		//设置缩放级别
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build()));//设置缩放级别
		// 开启定位图层  
		//mBaiduMap.setMyLocationEnabled(true);  
		// 当不需要定位图层时关闭定位图层  
		//mBaiduMap.setMyLocationEnabled(false);
		
		
		dblocation=GPApplication.gpManager.getLocation(getApplicationContext(), mLocationClient, myListener);
//		 mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
//		 LocationClientOption option = new LocationClientOption();
//		 option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
//		 option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
//		 option.setScanSpan(5000);//设置发起定位请求的间隔时间为5000ms
//		 option.setIsNeedAddress(true);//返回的定位结果包含地址信息
//		 option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
//		 mLocationClient.setLocOption(option);
//		 mLocationClient.registerLocationListener( myListener );    //注册监听函数
//		 mLocationClient.start();
//		 if (mLocationClient != null && mLocationClient.isStarted())
//			    mLocationClient.requestLocation();
//			else 
//				Log.d("LocSDK5", "locClient is null or not started");
//		 
//		 
		//位置提醒相关代码
		 BDNotifyListener  mNotifyer = new NotifyLister();
		 mNotifyer.SetNotifyLocation(dblocation.getLongitude(),dblocation.getLatitude(),3000,"gps");//4个参数代表要位置提醒的点的坐标，具体含义依次为：纬度，经度，距离范围，坐标系类型(gcj02,gps,bd09,bd09ll)
		 mLocationClient.registerNotify(mNotifyer);
		 //注册位置提醒监听事件后，可以通过SetNotifyLocation 来修改位置提醒设置，修改后立刻生效。
		 //取消位置提醒
		 mLocationClient.removeNotifyEvent(mNotifyer);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void openGPSSettings() {
		LocationManager alm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
			return;
		}

		Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * 标记定位地点
	 */
	public void marker(){
		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// 按下的如果是BACK，同时没有重复
			askForOut();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void askForOut() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("确定退出").setMessage("确定退出？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).setCancelable(false).show();
	}

}