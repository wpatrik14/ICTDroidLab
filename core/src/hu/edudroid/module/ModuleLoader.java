package hu.edudroid.module;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import dalvik.system.DexClassLoader;

public class ModuleLoader {

	private static ModuleLoader	mInstance;
	private final Context		mContext;

	private ModuleLoader(Context context) {
		mContext = context;
	}

	public static ModuleLoader getInstance(Context context){
		synchronized (ModuleLoader.class){
			if (mInstance == null)
				mInstance = new ModuleLoader(context);
			return mInstance;
		}
	}

	@SuppressWarnings("rawtypes")
	private ModuleRunnable loadModule(File file){
		
		File dexedJavaFile = AssetReader.copyAssetToInternalStorage("ModuleExample.jar", this.mContext);
		
		DexClassLoader dexLoader = new DexClassLoader(dexedJavaFile.getAbsolutePath(), 
														file.getAbsolutePath(), 
														null, 
														getClass().getClassLoader());
		
		Log.e("ModuleLoader","DexedPath: "+dexedJavaFile.getAbsolutePath());
		Log.e("ModuleLoader","FilesDir: "+this.mContext.getFilesDir().getAbsolutePath());
		
		try {
			Class<?> dexLoadedClass = dexLoader.loadClass("hu.edudroid.ict.sample_project.ModulExample");
			ModuleRunnable urlContent = (ModuleRunnable)dexLoadedClass.newInstance();
			return urlContent;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void runModule(String urlString, String fileUrl){
		try {
			/*
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.connect();
			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(fileUrl);
	
			byte data[] = new byte[1024];
			int count;
			while ((count = input.read(data)) != -1){
				output.write(data, 0, count);
			}
	
			output.flush();
			output.close();
			input.close();
			*/
			File outFile = new File(this.mContext.getFilesDir().getAbsolutePath());
			ModuleRunnable module = loadModule(outFile);
			module.run();
			
		} catch (NullPointerException e) {
			
		}
	}
}