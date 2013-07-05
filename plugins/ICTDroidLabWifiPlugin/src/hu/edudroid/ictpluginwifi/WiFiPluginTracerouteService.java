package hu.edudroid.ictpluginwifi;

import hu.edudroid.interfaces.Constants;
import hu.edudroid.interfaces.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WiFiPluginTracerouteService extends Service {
	
	private Context mContext;
	
	private void reportResult(long callId, String versionCode, String methodName, String method, List<String> result) {
		Intent intent = new Intent(Constants.INTENT_ACTION_PLUGIN_CALLMETHOD_ANSWER);
		intent.putExtra(Constants.INTENT_EXTRA_CALL_ID, callId);
		intent.putExtra(Constants.INTENT_EXTRA_KEY_PLUGIN_ID, methodName);
		intent.putExtra(Constants.INTENT_EXTRA_KEY_VERSION, versionCode);
		intent.putExtra(Constants.INTENT_EXTRA_METHOD_NAME, method);
		intent.putStringArrayListExtra(Constants.INTENT_EXTRA_VALUE_RESULT, new ArrayList<String>(result));
		mContext.sendBroadcast(intent);
	}
	
	public class LogStreamReader implements Runnable {

        private BufferedReader reader;
        private long callId;

        public LogStreamReader(long callId, InputStream is) {
            this.reader = new BufferedReader(new InputStreamReader(is));
            this.callId = callId;
        }

        public void run() {
            try {
                String line = reader.readLine();
                List<String> res = new ArrayList<String>();
                while (line != null) {
                    line = reader.readLine();
                    if(line!=null){
                    	res.add(line);
                    }
                }
                
                reportResult(callId, "v1.0", "WiFi Plugin", "traceroute", res);
                
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	@Override
    public void onStart(Intent intent, int startId) {
		
		mContext=this.getApplicationContext();
		
		final String ip=intent.getExtras().getString("ip");
		final long callId=Long.parseLong(intent.getExtras().getString("callId"));
		
		List<String> commandLine=new ArrayList<String>();
		commandLine.add("su");
		commandLine.add("-c");
		commandLine.add("traceroute");
        commandLine.add(ip);      
        
        Process mProcess=null;
        try {
        	mProcess = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
        	
        	LogStreamReader lsr = new LogStreamReader(callId,mProcess.getInputStream());
            Thread thread = new Thread(lsr, "LogStreamReader");
            thread.start();
        } catch(Exception e){
        	e.printStackTrace();
        }
        	

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}