package com.hoolai.sdk;

import java.util.HashMap;
import java.util.Map;
//import java.util.Map.Entry;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;
import com.union.config.Config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hoolai.open.fastaccess.channel.AbstractChannelInterfaceImpl;
import com.hoolai.open.fastaccess.channel.ExitCallback;
import com.hoolai.open.fastaccess.channel.FastSdk;
import com.hoolai.open.fastaccess.channel.GetServerListCallback;
import com.hoolai.open.fastaccess.channel.InitCallback;
import com.hoolai.open.fastaccess.channel.LoginCallback;
import com.hoolai.open.fastaccess.channel.PayCallback;
import com.hoolai.open.fastaccess.channel.ReturnValue;
import com.hoolai.open.fastaccess.channel.SelectServerCallback;
import com.hoolai.open.fastaccess.channel.ServerInfos;
import com.hoolai.open.fastaccess.channel.UserLoginResponse;
import com.hoolai.open.fastaccess.channel.bi.SendBICallback;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;


public class HoolaiSdk
{
	public static final String TAG = "Hoolai--";
	
	private boolean isInit = false;
	UnityPlayerActivity mActivity;
	
	Handler mHandler = new Handler();
	
	private static final int Type_Init_Fail = 1;
	private static final int Type_Init_Success = 2;
	private static final int Type_Login_Fail = 3;
	private static final int Type_Login_Success = 4;
	private static final int Type_Logout = 5;
	private static final int Type_Pay_Fail = 6;
	private static final int Type_Pay_Success = 7;
	private static final int Type_Exit_Channel = 8;
	private static final int Type_Exit_Game = 9;
	private static final int Type_GetServers_Success = 10;
	private static final int Type_GetServers_Fail = 11;
	private static final int Type_SelectServer_Success = 12;
	private static final int Type_SelectServer_Fail = 13;
	
	private static void MyUnitySendMessage(String arg0, String arg1, String arg2) {
		Log.d(TAG, "MyUnitySendMessage:arg0=" + arg0 + ",arg1=" + arg1 + ",arg2=" + arg2);
		UnityPlayer.UnitySendMessage(arg0, arg1, arg2);
	}
	
	public void OnCreate(UnityPlayerActivity activity)
	{
		mActivity = activity;
		if (!isInit) 
		{
			Map<String, String> parms = new HashMap<String, String>();
			parms.put(FastSdk.CURRENT_VERSION_CODE, getMainifestMetaData(FastSdk.CURRENT_VERSION_CODE));
			parms.put(FastSdk.CRASH_REPORT_APP_ID, getMainifestMetaData(FastSdk.CRASH_REPORT_APP_ID));// Bugly APP_ID
			parms.put(FastSdk.XG_V2_ACCESS_ID, getMainifestMetaData(FastSdk.XG_V2_ACCESS_ID));// 鎺ㄩ�� ACCESS_ID
			parms.put(FastSdk.XG_V2_ACCESS_KEY, getMainifestMetaData(FastSdk.XG_V2_ACCESS_KEY));// 鎺ㄩ�� ACCESS_KEY
			FastSdk.initWithParms(mActivity, parms);
		}
		
		FastSdk.onCreate(mActivity);
		// 鑾峰彇褰撳墠SDK鐗堟湰鍙锋帴鍙�
		String sdkVersion = FastSdk.getSdkVersion();
		Log.i(AbstractChannelInterfaceImpl.TAG, "褰撳墠SDK鐗堟湰鍙凤細" + sdkVersion);
	}
	
	public void doLogin(final String paramString) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				FastSdk.login(mActivity, paramString);
			}
		});
	}

	public void doPay(final int amount, final String itemName, final String callbackInfo, final String customParams) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				FastSdk.pay(mActivity, itemName, amount, callbackInfo);
			}
		});
	}

	public void doLogout(final String paramString) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				FastSdk.logout(mActivity, paramString);
			}
		});
	}

	public void doExit(final String SDKListening, final String exitCallbackMethod) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "doExit锛歋DKListening=" + SDKListening + ",exitCallbackMethod=" + exitCallbackMethod);
				// 璋冪敤SDK閫�鍑哄姛鑳�
				FastSdk.exit(mActivity, "", new ExitCallback() {
					@Override
					public void onExitSuccess(String s) {
						Log.d(TAG, "onExitSuccess start");
						MyUnitySendMessage(SDKListening, exitCallbackMethod, toResultData(Type_Exit_Channel, null));
						Log.d(TAG, "onExitSuccess end");
					}

					@Override
					public void onCustomExit(final String s) {
						Log.d(TAG, "onCustomExit start");
						MyUnitySendMessage(SDKListening, exitCallbackMethod, toResultData(Type_Exit_Game, null));
						Log.d(TAG, "onCustomExit end");
					}
				});
			}
		});
	}

	public void doGetServers(final String SDKListening, final String callbackMethod, final String version) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "doGetServers锛歋DKListening=" + SDKListening + ",callbackMethod=" + callbackMethod);
				FastSdk.getServerList(mActivity, version, new GetServerListCallback() {

					@Override
					public void onSuccess(ServerInfos serverInfos) {
						Log.d(TAG, "onGetServersSuccess");
						MyUnitySendMessage(SDKListening, callbackMethod, toResultData(Type_GetServers_Success, new Entry("serverInfos", serverInfos)));
					}

					@Override
					public void onFail(String code, String desc) {
						Log.d(TAG, "onGetServersFail");
						MyUnitySendMessage(SDKListening, callbackMethod, toResultData(Type_GetServers_Fail, new Entry("desc", desc)));
					}
				});
			}
		});
	}

	public void doSelectServer(final String SDKListening, final String callbackMethod, final String serverId) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "doSelectServer锛歋DKListening=" + SDKListening + ",callbackMethod=" + callbackMethod);
				FastSdk.selectServer(mActivity, serverId, new SelectServerCallback() {

					@Override
					public void onSuccess() {
						Log.d(TAG, "onSelectServersSuccess");
						MyUnitySendMessage(SDKListening, callbackMethod, toResultData(Type_SelectServer_Success, null));
					}

					@Override
					public void onFail(String code, String desc) {
						Log.d(TAG, "onSelectServersFail");
						MyUnitySendMessage(SDKListening, callbackMethod, toResultData(Type_SelectServer_Fail, new Entry("desc", desc)));
					}
				});
			}
		});
	}

	public void doSendBIData(final String SDKListening, final String callbackMethod, final String metric, final String jsonString) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "doSendBIData锛歋DKListening=" + SDKListening + ",callbackMethod=" + callbackMethod);
				FastSdk.getBiInterface(mActivity).sendBIData(metric, jsonString, new SendBICallback() {

					@Override
					public void onResult(String message) {
						Log.d(TAG, "onSendBiData Result=" + message);
						MyUnitySendMessage(SDKListening, callbackMethod, message);
					}
				});
			}
		});
	}

	public void setExtData(final String json) {
		mHandler.post(new Runnable() {

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				Map<String, String> userExtData = null;
				try {
					userExtData = JSON.parseObject(json, Map.class);
				} catch (Exception e) {
					Log.e(AbstractChannelInterfaceImpl.TAG, "setUserExtData:json=" + json, e);
					showToast("鏁版嵁鎵╁睍JSON鏍煎紡閿欒!");
					return;
				}
				Log.d(TAG, "setExtData");
				FastSdk.setUserExtData(mActivity, userExtData);
			}
		});
	}

	public void doInit(final String SDKListening, final String initCallback, final String loginCallback, final String payCallback) {
		if (isInit) {
			return;
		}
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "doInit锛歋DKListening=" + SDKListening + ",initCallback=" + initCallback + ",loginCallback=" + loginCallback + ",payCallback=" + payCallback);
				// 鍒濆鍖栧洖璋�
				InitCallback initCallbackImpl = new InitCallback() {

					@Override
					public void onInitSuccess(String s) {
						Log.d(TAG, "onInitSuccess");
						isInit = true;
						MyUnitySendMessage(SDKListening, initCallback, toResultData(Type_Init_Success, null));
					}

					@Override
					public void onInitFail(String s) {
						Log.d(TAG, "onInitFail");
						MyUnitySendMessage(SDKListening, initCallback, toResultData(Type_Init_Fail, null));
					}
				};
				
				
				// 鐧诲綍鍥炶皟
				LoginCallback loginCallbackImpl = new LoginCallback() 
				{
					@Override
					public void onLogout(Object paramObject) {
						if (paramObject == null || !(paramObject instanceof String)) {
							paramObject = "onLogout";
						}
						Log.d(TAG, "onLogoutSuccess start");
						MyUnitySendMessage(SDKListening, loginCallback, toResultData(Type_Logout, new Entry("customParams", paramObject)));
						Log.d(TAG, "onLogoutSuccess end");
					}

					@Override
					public void onLoginSuccess(UserLoginResponse userLoginResponse, Object paramObject) {
						Log.d(TAG, "鐧婚檰鎴愬姛:" + JSON.toJSONString(userLoginResponse));
						try {
							JSONObject jo = new JSONObject();
							jo.put("nickName", userLoginResponse.getNickName());
							jo.put("uid", userLoginResponse.getUid() + "");
							jo.put("accessToken", userLoginResponse.getAccessToken());
							jo.put("channelId", FastSdk.getChannelInfo().getId() + "");
							jo.put("channel", userLoginResponse.getChannel());
							jo.put("productId", FastSdk.getChannelInfo().getProductId() + "");
							jo.put("channelUid", userLoginResponse.getChannelUid());
//							jo.put("customParams", "onLoginSuccess");

							Log.d(TAG, "onLoginSuccess");
							MyUnitySendMessage(SDKListening, loginCallback, toResultData(Type_Login_Success, new Entry("data", jo)));
						} catch (Exception e) {
							Log.e(TAG, "楠岃瘉access鍑虹幇寮傚父", e);
						}
					}

					@Override
					public void onLoginFailed(ReturnValue<?> returnValue, Object paramObject) {
						String resultValue = JSON.toJSONString(returnValue);
						Log.d(TAG, "鐧婚檰澶辫触:" + resultValue);
						JSONObject jo = new JSONObject();
						jo.put("customParams", "onLoginFailed");
						jo.put("detail", resultValue);
						Log.d(TAG, "onLoginFailed");
						MyUnitySendMessage(SDKListening, loginCallback, toResultData(Type_Login_Fail, new Entry("data", jo)));
					}
				};
				// 鏀粯鍥炶皟
				PayCallback payCallbackImpl = new PayCallback() {
					@Override
					public void onSuccess(String param) {
						Log.d(TAG, "onPaySuccess");
						MyUnitySendMessage(SDKListening, payCallback, toResultData(Type_Pay_Success, new Entry("customParams", "onSuccess Pay " + param)));
					}

					@Override
					public void onFail(String param) {
						Log.d(TAG, "onPayFail");
						MyUnitySendMessage(SDKListening, payCallback, toResultData(Type_Pay_Fail, new Entry("customParams", "onFail Pay " + param)));
					}
				};

				FastSdk.applicationInit(mActivity, initCallbackImpl, loginCallbackImpl, payCallbackImpl);
			}
		});
	}

	private String toResultData(int resultCode, Entry entry) {
		JSONObject jo = new JSONObject();
		jo.put("resultCode", resultCode);
		if (entry != null) {
			jo.put(entry.getKey(), entry.getValue());
		}
		return jo.toJSONString();
	}

	private class Entry {

		private String key;
		private Object value;

		public Entry(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}

	}

	public void releaseResource() {
		mHandler.post(new Runnable() {
			public void run() {
				FastSdk.onStop(mActivity);
				FastSdk.onDestroy(mActivity);
				FastSdk.getChannelInterface().applicationDestroy(mActivity);
			}
		});
	}

	public void showToast(final String paramString) {
		mHandler.post(new Runnable() {
			public void run() {
				Toast.makeText(mActivity, paramString, Toast.LENGTH_LONG).show();
			}
		});
	}

	public String getMainifestMetaData(String name) {
		return FastSdk.getMetaDataConfig(mActivity, name);
	}

	public String getSdkVersion() {
		return FastSdk.getSdkVersion();
	}

	public void androidKillProcess() {
		Log.d(AbstractChannelInterfaceImpl.TAG, "androidKillProcess");
		Process.killProcess(Process.myPid());
	}

	public void runOnAndroidUIThread(final String gameObject, final String callbackMethod) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "runOnAndroidUIThread锛歋DKListening=" + gameObject + ",callbackMethod=" + callbackMethod);
				MyUnitySendMessage(gameObject, callbackMethod, "");
			}
		});
	}
}
