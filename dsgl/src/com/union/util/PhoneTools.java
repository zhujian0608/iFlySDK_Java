package com.union.util;

import com.union.config.Config;
import com.unity3d.player.UnityPlayer;

public class PhoneTools 
{
	public static void CBUnity(String method,String message)
	{
		UnityPlayer.UnitySendMessage(Config.CALLBACK_GAMEOBJECT, method, message);
	}
}
