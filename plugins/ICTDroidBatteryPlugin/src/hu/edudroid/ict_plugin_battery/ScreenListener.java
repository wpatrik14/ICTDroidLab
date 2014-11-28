package hu.edudroid.ict_plugin_battery;

import hu.edudroid.ictplugin.PluginCommunicationInterface;
import hu.edudroid.interfaces.BatteryConstants;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenListener extends BroadcastReceiver {

	private static final String TAG = ScreenListener.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		PluginCommunicationInterface communicationInterface = new PluginCommunicationInterface(new BatteryPlugin());
		// Subscribe to screen event when any broadcast is received
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
			Log.d(TAG, "Screen off");
			Map<String, Object> values = new HashMap<String, Object>();
			values.put(BatteryConstants.SCREEN_STATE, BatteryConstants.SCREEN_STATE_OFF);
			communicationInterface.fireEvent(BatteryConstants.SCREEN_STATE_CHANGED, values, context);
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
			Log.d(TAG, "Screen on");
			Map<String, Object> values = new HashMap<String, Object>();
			values.put(BatteryConstants.SCREEN_STATE, BatteryConstants.SCREEN_STATE_ON);
			communicationInterface.fireEvent(BatteryConstants.SCREEN_STATE_CHANGED, values, context);
		}else {
			Log.e(TAG, "Unknown event received: " + intent.getAction());
		}
	}	
}
