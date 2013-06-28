package hu.edudroid.ictpluginwifi;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PluginLogic {

	private static final String				INTENT_REPORT_RESULT	= "hu.edudroid.ict.plugin_polling_answer";
	private static PluginLogic				mInstance				= null;

	public final String						mTitle					= "WiFi Plugin";
	public final String						mAuthor					= "Patrik Weisz";
	public final String						mDescription			= "This plugin was created for testing purposes.";
	public final String						mVersionCode			= "1.0";

	private final ArrayList<PluginMethod>	mMethods;
	private final Context					mContext;

	private PluginLogic(Context context) {
		mContext = context;

		mMethods = new ArrayList<PluginMethod>();
		mMethods.add(new PluginMethod(	"showIPAddress",
										"Shows the device's IP Address",
										this));
		mMethods.add(new PluginMethod(	"showMACAddress",
										"Shows the device's MAC Address",
										this));
		mMethods.add(new PluginMethod(	"showNetMaskAddress",
										"Shows the device's NetMask Address",
										this));
		mMethods.add(new PluginMethod(	"showNetworkSpeed",
										"Shows the device's Network Speed",
										this));
	}

	public static PluginLogic getInstance(Context context){
		// double check locking
		if (mInstance == null){
			synchronized (PluginLogic.class){
				if (mInstance == null)
					mInstance = new PluginLogic(context);
			}
		}
		return mInstance;
	}

	public ArrayList<PluginMethod> getMethods(){
		return mMethods;
	}
	
	public ArrayList<String> getMethodsName(){
		ArrayList<String> methods=new ArrayList<String>();
		for(int i=0;i<mMethods.size();i++){
			methods.add(mMethods.get(i).mName);
		}
		return methods;
	}

	public final void callMethod(final String methodName, final Object[] params){
		// final Method[] methods = getClass().getMethods();
		for (int i = 0; i < mMethods.size(); i++){
			if (mMethods.get(i).mName.equals(methodName)){
				try {
					mMethods.get(i).invoke(params);
					return;
				}
				catch (IllegalArgumentException e){
					reportError(methodName, "IllegalArgumentException", e.getMessage());
				}
				catch (InvocationTargetException e){
					reportError(methodName, "InvocationTargetException", e.getMessage());
				}
				catch (IllegalAccessException e){
					reportError(methodName, "IllegalAccessException", e.getMessage());
				}
			}
					
		}
	}

	private void reportResult(final String resultCode, final String sender, final String result, final String metadata){
		Intent intent = new Intent(INTENT_REPORT_RESULT);
		intent.putExtra("action", resultCode);
		intent.putExtra("plugin", mTitle);
		intent.putExtra("version", mVersionCode);
		intent.putExtra("meta", metadata);
		intent.putExtra("sender", sender);
		intent.putExtra("result", result);
		mContext.sendBroadcast(intent);
	}
	
	protected final void reportResult(final String sender, final String result, final String metadata){
		reportResult("reportResult", sender, result, metadata);
	}
	
	protected final void reportResult(final String sender, final String result){
		reportResult("reportResult", sender, result, "");
	}
	
	protected final void reportError(final String sender, final String result, final String metadata){
		reportResult("reportError", sender, result, metadata);
	}
	
	protected final void reportError(final String sender, final String result){
		reportResult("reportError", sender, result, "");
	}

	public void showIPAddress(String msg1, String msg2, String msg3){
		Toast.makeText(mContext, "showIPAddress: "+msg1 + msg2 + msg3, Toast.LENGTH_LONG).show();
		reportResult("showIPAddress", msg1 + " " + msg2 + " " + msg3);
	}
	
	public void showMACAddress(String msg1, String msg2, String msg3){
		Toast.makeText(mContext, "showMACAddress: "+msg1 + msg2 + msg3, Toast.LENGTH_LONG).show();
		reportResult("showMACAddress", msg1 + " " + msg2 + " " + msg3);
	}
	
	public void showNetMaskAddress(String msg1, String msg2, String msg3){
		Toast.makeText(mContext, "showNetMaskAddress: "+msg1 + msg2 + msg3, Toast.LENGTH_LONG).show();
		reportResult("showNetMaskAddress", msg1 + " " + msg2 + " " + msg3);
	}
	
	public void showNetworkSpeed(String msg1, String msg2, String msg3){
		Toast.makeText(mContext, "showNetworkSpeed: "+msg1 + msg2 + msg3, Toast.LENGTH_LONG).show();
		reportResult("showNetworkSpeed", msg1 + " " + msg2 + " " + msg3);
	}
}