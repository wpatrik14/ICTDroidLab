package hu.edudroid.ict.sample_module;

import hu.edudroid.interfaces.BatteryConstants;
import hu.edudroid.interfaces.Logger;
import hu.edudroid.interfaces.Module;
import hu.edudroid.interfaces.Plugin;
import hu.edudroid.interfaces.PluginCollection;
import hu.edudroid.interfaces.Preferences;
import hu.edudroid.interfaces.TimeServiceInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ModuleExample extends Module {
	
	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
	
	public ModuleExample(Preferences prefs, Logger logger, PluginCollection pluginCollection, TimeServiceInterface timeservice) {
		super(prefs, logger, pluginCollection, timeservice);
	}
	
	private static final String TAG = ModuleExample.class.getName();

	@Override
	public void init(){
		mLogger.e(TAG, "Module init...");
		mTimeService.runPeriodic(1000, 5000, 5, this);	
		registerPluginListeners();
	}
	
	private void registerPluginListeners() {
		Plugin plugin = mPluginCollection.getPluginByName(BatteryConstants.PLUGIN_NAME);
		if (plugin != null) {
			mLogger.e(TAG, "Plugin available, registering for events.");
			plugin.registerEventListener(BatteryConstants.SCREEN_STATE_CHANGED, this);
			plugin.registerEventListener(BatteryConstants.BATTERY_LEVEL_CHANGED, this);
			plugin.registerEventListener(BatteryConstants.CHARGING_STATE_CHANGED, this);
		} else {
			mLogger.e(TAG, "Plugin not yet available.");			
		}
	}

	@Override
	public void onResult(long id, String plugin, String pluginVersion,
			String methodName, Map<String, Object> result) {
		if (result != null) {
			mLogger.i(TAG, "Result received " + result);
		} else {
			mLogger.i(TAG, "Null received " + result);
		}
	}


	@Override
	public void onError(long id, String plugin, String pluginVersion, String methodName,
			String errorMessage) {
		mLogger.i(TAG, "Error in plugin execution " + errorMessage);
	}

	@Override
	public void onEvent(String plugin, String version, String eventName, Map<String, Object> extras) {
		mLogger.i(TAG, "Event received " + plugin + " " + eventName + " " + extras);
	}

	@Override
	public void onTimerEvent() {
		Plugin plugin = mPluginCollection.getPluginByName(BatteryConstants.PLUGIN_NAME);
		mLogger.i(TAG, "Module example 5s run at " + dateFormatter.format(new Date()));
	}
}