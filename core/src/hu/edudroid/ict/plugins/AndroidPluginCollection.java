package hu.edudroid.ict.plugins;

import hu.edudroid.interfaces.Plugin;
import hu.edudroid.interfaces.PluginCollection;

import java.util.ArrayList;
import android.util.Log;

public class AndroidPluginCollection implements PluginCollection{

	private static AndroidPluginCollection	mInstance	= null;
	private ArrayList<PluginBase>		mPlugins	= null;

	private AndroidPluginCollection() {
		mPlugins = new ArrayList<PluginBase>();
	}

	public static AndroidPluginCollection getInstance(){
		if (mInstance == null){
			synchronized (AndroidPluginCollection.class){
				if (mInstance == null)
					mInstance = new AndroidPluginCollection();
			}
		}

		return mInstance;
	}
	
	public PluginBase getPluginByHashcode(final int hash){
		Log.e("PLUGIN", "# of plugins = " + mPlugins.size());
		for (int i = 0; i < mPlugins.size(); i++){
			Log.e("PLUGIN", "(" + i + ") hash: " + mPlugins.get(i).hashCode());
			if (mPlugins.get(i).hashCode() == hash)
				return mPlugins.get(i);
		}
		return null;
	}
	
	@Override
	public Plugin getPluginByName(final String name) {
		for (int i=0; i< mPlugins.size(); i++) {
			if (mPlugins.get(i).getName().equals(name))
				return mPlugins.get(i);
		}
		return null;
	}

	public ArrayList<PluginBase> getPlugins() {
		return new ArrayList<PluginBase>(mPlugins);
	}
}
