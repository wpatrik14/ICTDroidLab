package hu.edudroid.ict;

import hu.edudroid.ict.plugins.AndroidPluginCollection;
import hu.edudroid.ict.plugins.PluginListener;
import hu.edudroid.ict.plugins.PLuginIntentReceiver;
import hu.edudroid.interfaces.Constants;
import hu.edudroid.interfaces.Logger;
import hu.edudroid.interfaces.Module;
import hu.edudroid.interfaces.ModuleDescriptor;
import hu.edudroid.interfaces.Plugin;
import hu.edudroid.interfaces.PluginCollection;
import hu.edudroid.interfaces.Preferences;
import hu.edudroid.interfaces.TimeServiceInterface;
import hu.edudroid.module.AndroidLogger;
import hu.edudroid.module.ModuleTimeService;
import hu.edudroid.module.SharedPrefs;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class CoreService extends Service implements PluginListener {
	
	public static final String TEMP_DIR = "temp";
	public static final String DESCRIPTOR_FOLDER = "descriptors";
	public static final String JAR_FOLDER = "jars";	
	
	public static File getDescriptorFolder(Context context) {
		return new File(context.getFilesDir(), CoreService.DESCRIPTOR_FOLDER);
	}

	
	public static File getJarFolder(Context context) {
		return new File(context.getFilesDir(), CoreService.JAR_FOLDER);
	}

	private static final String TAG = "CoreService";

	private PLuginIntentReceiver mBroadcast;
	private HashMap<String, Module> modules = new HashMap<String, Module>(); // Modules by class name
	private HashMap<String, ModuleDescriptor> descriptors = new HashMap<String, ModuleDescriptor>(); // Descriptors by class name
	
	private CoreBinder binder = new CoreBinder();
	
	private AndroidPluginCollection pluginCollection;

	private HashSet<PluginListener> listeners = new HashSet<PluginListener>();
	private boolean started = false;
	
	public class CoreBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (!started) {
			started = true;
			Log.e(TAG, "Starting service!");
			mBroadcast = new PLuginIntentReceiver();
			registerReceiver(mBroadcast, new IntentFilter(
					Constants.INTENT_ACTION_DESCRIBE));
			registerReceiver(mBroadcast, new IntentFilter(
					Constants.INTENT_ACTION_PLUGIN_CALLMETHOD_ANSWER));
			registerReceiver(mBroadcast, new IntentFilter(
					Constants.INTENT_ACTION_PLUGIN_EVENT));
	
			pluginCollection = new AndroidPluginCollection();
			mBroadcast.registerPluginDetailsListener(this);
	
			Intent mIntent = new Intent(Constants.INTENT_ACTION_PLUGIN_POLL);
			sendBroadcast(mIntent);
	
			// Process descriptor files
			File descriptorFolder = getDescriptorFolder(this);
			String[] descriptors = descriptorFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith("desc");
				}
			});
			for (String descriptor : descriptors) {
				ModuleDescriptor moduleDescriptor = ModuleLoader.parseModuleDescriptor(new File(descriptorFolder,descriptor));
				if (moduleDescriptor != null) {
					addModule(moduleDescriptor);
				}
			}
		} else {
			Log.e(TAG, "Service already running.");
		}
	}
	
	public void registerPluginDetailsListener(PluginListener listener) {
		listeners.add(listener);
	}

	public void unregisterPluginDetailsListener(PluginListener listener) {
		listeners.remove(listener);
	}
	
	public List<ModuleDescriptor> getLoadedModules() {
		List<ModuleDescriptor> ret = new ArrayList<ModuleDescriptor>();
		for (String moduleClass : modules.keySet()) {
			ModuleDescriptor descriptor = descriptors.get(moduleClass);
			ret.add(descriptor);
		}
		return ret;
	}
	
	public boolean addModule(ModuleDescriptor moduleDescriptor) {
		if (modules.containsKey(moduleDescriptor.getClassName())) {
			Log.w(TAG, "Module " + moduleDescriptor.getClassName() + " already loaded.");
			return false;
		}
		try {
			File jarFolder = getJarFolder(this);
			Module module = loadModule(new File(jarFolder, moduleDescriptor.getJarFile()).getAbsolutePath(), moduleDescriptor.getClassName());
			modules.put(moduleDescriptor.getClassName(), module);
			this.descriptors.put(moduleDescriptor.getClassName(), moduleDescriptor);
			module.init();
			return true;
		} catch (NullPointerException e) {
			Log.e(TAG, "Couldn't load module " + e);
			e.printStackTrace();
			return false;
		} catch (SecurityException e) {
			Log.e(TAG, "Couldn't load module " + e);
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Couldn't load module " + e);
			e.printStackTrace();
			return false;
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "Couldn't load module " + e);
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			Log.e(TAG, "Couldn't load module " + e);
			e.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Module loadModule(String dexedJavaFile, String className) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		Log.i(TAG, "Loading module " + className + " from file " + dexedJavaFile);
		File dexOptimizedFolder = new File(getFilesDir(), TEMP_DIR);
		dexOptimizedFolder.mkdirs();
		DexClassLoader dexLoader = new DexClassLoader(dexedJavaFile, 
														dexOptimizedFolder.getAbsolutePath(), 
														null, 
														getClassLoader());
		try {
			Class<?> dexLoadedClass = dexLoader.loadClass(className);
			Module module = null; 
			Log.e(TAG,"Retrieving constructor");
			Constructor<Module> constructor = (Constructor<Module>) dexLoadedClass.getConstructor(Preferences.class, Logger.class, PluginCollection.class, TimeServiceInterface.class);
			if (constructor == null) {
				throw new NoSuchMethodException("Couldn't find proper consturctor.");
			}
			TimeServiceInterface timeservice = ModuleTimeService.getInstance();
			Log.e(TAG,"Calling constructor");
			module = constructor.newInstance(new SharedPrefs(this, className),
					new AndroidLogger(className),
					pluginCollection,
					timeservice);
			Log.e(TAG,"Module init ready " + module);
			return module;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void onDestroy() {
		Log.e(TAG, "Service destroyed");
		super.onDestroy();
	}

	@Override
	public boolean newPlugin(Plugin plugin) {
		pluginCollection.newPlugin(plugin);
		for (PluginListener listener : listeners) {
			listener.newPlugin(plugin);
		}
		return true;
	}


	public List<Plugin> getPlugins() {
		return pluginCollection.getAllPlugins();
	}
}