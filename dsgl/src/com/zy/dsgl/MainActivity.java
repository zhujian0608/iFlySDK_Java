package com.zy.dsgl;

import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;


import com.hoolai.open.fastaccess.channel.AbstractChannelInterfaceImpl;
import com.hoolai.open.fastaccess.channel.FastSdk;
import com.hoolai.sdk.HoolaiSdk;
//import com.hoolai.demo.AllActivity;
//import com.hoolai.open.fastaccess.channel.FastSdk;
//import com.hoolai.open.fastaccess.channel.InitCallback;
//import com.hoolai.sdk.HoolaiSdk;
import com.tencent.bugly.crashreport.CrashReport;
import com.union.config.Config;
import com.union.util.PhoneTools;
import com.union.voice.IatVoice;
import com.unity3d.player.UnityPlayerActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends UnityPlayerActivity {

	// wifi相关  
	IntentFilter wifiIntentFilter;  // wifi监听器 
	private Context context;  
	private boolean isInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        
        WifiManager wifi_service = (WifiManager)getSystemService(WIFI_SERVICE); 
        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        
        wifiIntentFilter = new IntentFilter();  
        wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); 

        
        CrashReport.initCrashReport(getApplicationContext(),Config.BuglyAppIDAndroid,false);   //buggly
        initVoice();
        
        initHoolSdk();   //初始化sdk
    }
    
 
    /* --------------------------获取wifi信号----------------------------*/
 // 声明wifi消息处理过程  
    private BroadcastReceiver wifiIntentReceiver = new BroadcastReceiver() 
    {  
	    @Override 
	    public void onReceive(Context context, Intent intent) 
	    {   
	        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	        android.net.NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
	   
	        if(mobile.isAvailable())  //getState()方法是查询是否连接了数据网络  
	        {
	        	PhoneTools.CBUnity("DataThroughInfo",""); 
	        }
	        else  
	        {
	        	 int wifi_state = intent.getIntExtra("wifi_state", 0);  
	        	 int level = ((WifiManager)getSystemService(WIFI_SERVICE)).getConnectionInfo().getRssi(); 
	        	 PhoneTools.CBUnity("WifiInfo", String.valueOf(level));
	        }
	       
	        
	        //Log.i(Global.TAG, "1111:" + level);  
	        //switch (wifi_state) 
	        //{  
	        //case WifiManager.WIFI_STATE_DISABLING:  
	          //Log.i(Global.TAG, "1111:" + WifiManager.WIFI_STATE_DISABLING);  
	          //wifi_image.setImageResource(R.drawable.wifi_sel);  
	          //wifi_image.setImageLevel(level);  
	          //break;  
	        //case WifiManager.WIFI_STATE_DISABLED:  
	          //Log.i(Global.TAG, "2222:" + WifiManager.WIFI_STATE_DISABLED);  
	          //wifi_image.setImageResource(R.drawable.wifi_sel);  
	          //wifi_image.setImageLevel(level);  
	          //break;  
	        //case WifiManager.WIFI_STATE_ENABLING:  
	          //wifi_image.setImageResource(R.drawable.wifi_sel);  
	          //wifi_image.setImageLevel(level);  
	          //Log.i(Global.TAG, "33333:" + WifiManager.WIFI_STATE_ENABLING);  
	          //break;  
	        //case WifiManager.WIFI_STATE_ENABLED:  
	          //Log.i(Global.TAG, "4444:" + WifiManager.WIFI_STATE_ENABLED);  
	          //wifi_image.setImageResource(R.drawable.wifi_sel);  
	          //wifi_image.setImageLevel(level);  
	          //break;  
	        //case WifiManager.WIFI_STATE_UNKNOWN:  
	          //Log.i(Global.TAG, "5555:" + WifiManager.WIFI_STATE_UNKNOWN);  
	          //wifi_image.setImageResource(R.drawable.wifi_sel);  
	          //wifi_image.setImageLevel(level);  
	          //break;  
	        //}  
	      }  
    	}; 
    
    
    
    
    /*----------------------------互爱SDK---------------------------------*/
    private HoolaiSdk mHoolaiSdk;
    public void initHoolSdk()
    {
    	mHoolaiSdk = new HoolaiSdk();
    	mHoolaiSdk.OnCreate(this);
    }
    
    //注册  初始化   登录  充值 的回调
    public void doInit(final String initCallback, final String loginCallback, final String payCallback)
    {
    	mHoolaiSdk.doInit(Config.CALLBACK_GAMEOBJECT, initCallback, loginCallback, payCallback);
    }
    
    //互爱SDK 登录接口
    public void doLogin(final String paramString)
    {
    	mHoolaiSdk.doLogin(paramString);
    }
    
    //互爱SDK 登出接口
    public void doLogout(final String paramString)
    {
    	mHoolaiSdk.doLogout(paramString);
    	
    	//mHoolaiSdk.doLogin(paramString);
    }
    
    //互爱SDK 退出游戏接口
    public void doExit(final String exitCallbackMethod)
    {
    	mHoolaiSdk.doExit(Config.CALLBACK_GAMEOBJECT,exitCallbackMethod);
    }
    
    //充值接口
    public void doPay(final int amount, final String itemName, final String callbackInfo, final String customParams) 
    {
    	mHoolaiSdk.doPay(amount,itemName,callbackInfo,customParams);
    }
    
    ///获取服务器列表
    public void doGetServers(final String SDKListening, final String callbackMethod, final String version) 
    {
    	mHoolaiSdk.doGetServers(Config.CALLBACK_GAMEOBJECT,callbackMethod,version);
    }
    
    //选择服务器
    public void doSelectServer(final String callbackMethod, final String serverId) 
    {
    	mHoolaiSdk.doSelectServer(Config.CALLBACK_GAMEOBJECT,callbackMethod,serverId);
    }
    
    //报送BI
    public void doSendBIData(final String callbackMethod, final String metric, final String jsonString) 
    {
    	mHoolaiSdk.doSendBIData(Config.CALLBACK_GAMEOBJECT,callbackMethod,metric,jsonString);
    }
    
    //用户扩展接口 
    public void setExtData(final String json) {
    	mHoolaiSdk.setExtData(json);
    }
    
    public void showToast(final String paramString){
    	mHoolaiSdk.showToast(paramString);
	}
    
	public void releaseResource() {
		mHoolaiSdk.releaseResource();
	}
	
	public String getSdkVersion() {
		return mHoolaiSdk.getSdkVersion();
	}
    
	public String getMainifestMetaData(String name) {
		return mHoolaiSdk.getMainifestMetaData(name);
	}

	public void androidKillProcess() {
		mHoolaiSdk.androidKillProcess();
	}
	
	public void runOnAndroidUIThread(final String gameObject, final String callbackMethod) {
		mHoolaiSdk.runOnAndroidUIThread(gameObject,callbackMethod);
	}
	
	
	
	
	
    /* --------------------------语音----------------------------*/
    private IatVoice mIatVoice;
    
    public void initVoice()
    {
    	mIatVoice = new IatVoice();
    	mIatVoice.OnCreate(this);
    }
    
    public void getVoicePath()
    {
    	PhoneTools.CBUnity("GetTalkDir", mIatVoice.voiceDir);  //activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "UnityVoice";
    }
    
    public void stopTalk() throws IOException
    {
    	if(mIatVoice != null)
    	{
    		mIatVoice.stopTalk();
    	}
    }
    
    public void startToRecordVoice()
    {
    	if(mIatVoice != null)
    	{
    		mIatVoice.StartToRecordVoice();
    	}
    }
    
    public void playRecordVoice(String path)
    {
    	if(mIatVoice != null)
    	{
    		mIatVoice.playMusic(path);
    	}
    }
    
    public void stopPlayRecordVoice(String path)
    {
    	if(mIatVoice != null)
    	{
    		mIatVoice.stopPlayMusic(path);
    	}
    }
    
    public void playLocalVoice()
    {
    	if(mIatVoice != null)
    	{
    		mIatVoice.playLocalVoice();
    	}
    }
    
    //中断语音情况下重置
    public void releaseSoundResource()
    {
    	if(mIatVoice != null)
    	{
    		mIatVoice.ReleaseSoundResource();
    	}
    }
    
    
	
    
    
    
    /* --------------------------继承父类----------------------------*/
    @Override 
    protected void onResume() {  
      super.onResume();
      // 注册wifi消息处理器  
      registerReceiver(wifiIntentReceiver, wifiIntentFilter);  
     // FastSdk.onResume(this);
    } 
    
    @Override 
    protected void onPause() {  
      super.onPause();  
      unregisterReceiver(wifiIntentReceiver);  
     // FastSdk.onPause(this);
    } 
    
    @Override
	protected void onStart() {
		super.onStart();
		FastSdk.onStart(this);
	}
    
    @Override
	protected void onRestart() {
		super.onRestart();
		FastSdk.onRestart(this);
	}
    
    @Override
	protected void onStop() {
		super.onStop();
		FastSdk.onStop(this);
	}

    @Override
	protected void onDestroy() {
		try {
			super.onDestroy();
			FastSdk.onDestroy(this);
		} catch (Exception e) {
			Log.e("Hookai--", "onDestroy occured exception", e);
		}
	}
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		FastSdk.onConfigurationChanged(this, newConfig);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		FastSdk.onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		FastSdk.onNewIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		FastSdk.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		FastSdk.onBackPressed();
	}

}
