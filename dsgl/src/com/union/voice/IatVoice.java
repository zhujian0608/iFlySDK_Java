package com.union.voice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.union.config.Config;
import com.union.util.JsonParser;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AmrInputStream;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class IatVoice implements OnCompletionListener
{
	private Toast mToast = null;
	   // ������д����
	private SpeechRecognizer mIat;
	private String pcmpath = "";
	private MediaPlayer mMediaPlayer;
	
	public String voiceDir = "";
	private String amrpath = "";
	private String sendToUnityPath = "";
	
	UnityPlayerActivity mActivity;
	AudioManager audioManager;
	
	private long talkTime;
	private long startToTalk = 0;
	private int talkVolume = 0;
	
	@SuppressLint("ShowToast")
	public void OnCreate(UnityPlayerActivity activity)
	{
		this.mActivity = activity;
		try
		{
			 SpeechUtility.createUtility(mActivity,"appid=" + Config.VOICE_ID);		// ��ʼ��ʶ�����--�ɵ�543e0f0c
		}catch(Exception e)
		{
			Log.e(Config.TAG,"Init Speech ������");
		}
		
		mIat = SpeechRecognizer.createRecognizer(mActivity, mInitListener);
	 
		voiceDir = activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "UnityVoice";
		
		pcmpath = voiceDir + File.separator + "pcmAudio.pcm";
		
		setParam();
		
		initMediaplayer();
		mToast = Toast.makeText(mActivity, "", Toast.LENGTH_SHORT);
		
	}
	
	  private void initMediaplayer() {
			if (mMediaPlayer != null) {
				mMediaPlayer.reset();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			mMediaPlayer = new MediaPlayer();
			audioManager = (AudioManager)mActivity.getSystemService(Context.AUDIO_SERVICE);
	  }
	  
	  public void StartToRecordVoice()
	  {
		  int ret = 0;
		  ret = mIat.startListening(recognizerListener);
		  if(ret != ErrorCode.SUCCESS){
				showTip("��дʧ��,�����룺" + ret);
		 }else {
				//showTip("�뿪ʼ˵��");
		}		
	  }
	  
	  public void stopTalk() 
	  {
		  if(mIat.isListening())
		  {
			  mIat.stopListening();
		  }
	  }
	  
	//voiceDir + sendToUnityPath;  //activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "UnityVoice"  + "/audio" + ".amr";;
	public void SaveToAmr(String path) throws IOException
	{
		pcm2amr(path);
	}
		
	  /**
		 * ��ʼ����������
		 */
		private InitListener mInitListener = new InitListener() {
			@Override
			public void onInit(int code) {
				Log.d(Config.TAG, "SpeechRecognizer init() code = " + code);
				if (code != ErrorCode.SUCCESS) {
					//showTip("��ʼ��ʧ��,�����룺"+code);
	      	}
			}
		};
		
		  private RecognizerListener recognizerListener=new RecognizerListener()
		  {
			 
			  @Override
				public void onBeginOfSpeech()
			  	{	
				  	startToTalk = System.currentTimeMillis();
				  	//showTip("��ʼ˵��");
			  	}
		
				@Override
				public void onError(SpeechError error) 
				{
					//Log.e(Config.TAG,"SpeechError :" + error.getMessage());
					//showTip("˵��������");
				}
				
				@Override
				public void onEndOfSpeech() 
				{
					//showTip("����˵��");	
					//stopTalk();
				}
			
				@Override
				public void onResult(RecognizerResult results, boolean isLast) 
				{	
					if(results == null) return;
					talkTime = System.currentTimeMillis() - startToTalk;
					String text = JsonParser.parseIatResult(results.getResultString());
					UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT,"TalkWordMessage", text);  //
					if(isLast) 
					{
						//showTip("onResult 11111111111");	
						sendToUnityPath = "/audio" + ".amr";
						amrpath = voiceDir + sendToUnityPath;  //activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "UnityVoice";
						try
						{
							//showTip("onResult 22222222222222");	
							SaveToAmr(amrpath);
							UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT, "TalkTime", String.valueOf(talkTime));
							UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT, "TalkWordEnd", amrpath);
							//showTip("onResult 33333333333333");	
						}catch(Exception e)
						{
							Log.e(Config.TAG,"Failed to convert amr file :" + e.getMessage());
							showTip("Failed to convert amr file");	
						}
					}
				}
			
				@Override
				public void onVolumeChanged(int volume, byte[] data)
				{
					//showTip("������С��" + volume);
					if(talkVolume != volume) 
					{
						talkVolume = volume;
						UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT, "VoiceVolume", String.valueOf(volume));	
					}
				}
			
				@Override
				public void onEvent(int eventType, int arg1, int arg2, Bundle obj) 
				{
					
				}
	  };
  
	
		/**
		 * ��������
		 * @param param
		 * @return 
		 */
	  	@SuppressLint("SdCardPath")
		public void setParam(){
			//mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// ��������
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// ������������
			mIat.setParameter(SpeechConstant.ACCENT,"mandarin");
			 mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
			 mIat.setParameter(SpeechConstant.DOMAIN, "iat"); 
			
			mIat.setParameter(SpeechConstant.SAMPLE_RATE,"8000");
			mIat.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT,"30000");
			// ��������ǰ�˵�,n���ڲ���ʼ˵��ֹͣ¼��
			mIat.setParameter(SpeechConstant.VAD_BOS, "30000");
			// ����������˵�,n�벻˵����ֹͣ¼��
			mIat.setParameter(SpeechConstant.VAD_EOS, "30000");
			// ���ñ�����
			mIat.setParameter(SpeechConstant.ASR_PTT, "1");
			// ������Ƶ����·��
			mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, pcmpath);
			
			//mIat.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");//����������⣬��KEY_REQUEST_FOCUS��Ϊ false,�����Լ�������Ƶ���������
		}
		
	
		public void onDestroy()
		{
			mIat.cancel();
			mIat.destroy();
		}
		
		  //activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "UnityVoice"  + "/audio" + ".amr";;
		 //��pcmתamr
		 public void pcm2amr(String path) throws IOException
	     {
			  InputStream inStream; //new FileInputStream(pcmpath);   //��ȡpcm��Ƶ��Դ
			  inStream = new FileInputStream(pcmpath);
			  AmrInputStream aStream = new AmrInputStream(inStream);
			  
			  File file = new File(path);  //����amr��Ƶ��Դ
			  file.createNewFile();
			  OutputStream out = new FileOutputStream(file);
				 
			  byte[] x = new byte[1024];
		         int len;
		         out.write(0x23);
		         out.write(0x21);
		         out.write(0x41);
		         out.write(0x4D);
		         out.write(0x52);
		         out.write(0x0A);   
		         while ((len = aStream.read(x)) > 0)
		         {
		             out.write(x, 0, len);
		         }
		
		         out.close();
		         aStream.close();
	     }
		 
		 public void playMusic(String path)
		 {
			 path = voiceDir + path;   //activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "UnityVoice" + "/id.amr"
			 try{
				 mMediaPlayer.reset();
				 mMediaPlayer.setDataSource(path);
				 mMediaPlayer.setOnCompletionListener(this);
				 
				 mMediaPlayer.prepare();
				 mMediaPlayer.start();
				 mMediaPlayer.setLooping(false);
			 }catch(Exception e)
			 {
				 UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT,"OnPlaySoundError", e.getMessage());
				 e.printStackTrace();
			 }
		 }
		 
		 
		 public void stopPlayMusic(String path)
		 {
			 path = voiceDir + path;   //activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "UnityVoice" + "/id.amr"
			 try{
				 mMediaPlayer.reset();
				 mMediaPlayer.setDataSource(path);
				 mMediaPlayer.setOnCompletionListener(this);
				 
				 //mMediaPlayer.prepare();
				 mMediaPlayer.stop();
				 //mMediaPlayer.setLooping(false);
			 }catch(Exception e)
			 {
				 UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT,"OnPlaySoundError", e.getMessage());
				 e.printStackTrace();
			 }
		 }
		 
		 
		 
		 public void playLocalVoice()
		 {
			 playMusic("/audio" + ".amr");
		 }
		 
		 @Override
		 public void onCompletion(MediaPlayer media)
		 {
			 if(mMediaPlayer != null)
			 {
				 UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT,"OnPlaySoundComplete", "");
			 }
		 }
		 
		 public void ReleaseSoundResource()
		 {
			 if(mMediaPlayer != null)
			 {
				 mMediaPlayer.stop();
			 }
		 }
		 
		 public void getSystemVoice()
		 {
			 AudioManager mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
			 int current = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
			 UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT,"VoiceVolume","" + current);
		 }
		 
		 @SuppressWarnings("unused")
		 public void setVoice(int voice)
		 {
			 AudioManager mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
		 }

		 public void showTip(final String tips) {
	         mToast.setText(tips);
	         mToast.show();
	 }
	  
		
	
}
