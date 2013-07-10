package hu.edudroid.ict.ui;

import hu.edudroid.ict.CoreService;
import hu.edudroid.ict.CoreService.CoreBinder;
import hu.edudroid.ict.R;
import hu.edudroid.interfaces.ModuleDescriptor;
import hu.edudroid.module.AssetReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class ModuleOverviewActivity extends Activity implements OnItemClickListener, ServiceConnection {
	private static final String TAG = "ModuleOverviewActivity";
	private static final String DESCRIPTOR_ASSET_FOLDER = "descriptors";
	private static final String JAR_ASSET_FOLDER = "jars";
	private ListView moduleList;
	private ModuleListAdapter moduleListAdapter;
	private CoreService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modules_overview);
		moduleList = (ListView)findViewById(R.id.moduleList);
		moduleList.setOnItemClickListener(this);
		moduleListAdapter = new ModuleListAdapter(new ArrayList<ModuleDescriptor>(), new ArrayList<Boolean>(), this, getLayoutInflater());
		moduleList.setAdapter(moduleListAdapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		bindService(new Intent(this, CoreService.class), this, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onPause() {
		service = null;
		unbindService(this);
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		Log.e(TAG, "Service connected");
		this.service = ((CoreBinder)arg1).getService();
		refreshModuleList();
	}
	
	private void refreshModuleList() {
		List<ModuleDescriptor> loadedModules = service.getLoadedModules();
		List<ModuleDescriptor> modulesInAssets = new ArrayList<ModuleDescriptor>();
		AssetManager assetManager = getAssets();
		// Check if there is a module available that has not been loaded already.
		try {
			String[] descriptors = assetManager.list(DESCRIPTOR_ASSET_FOLDER);
			String[] jars = assetManager.list(JAR_ASSET_FOLDER);
			for (String jar : jars) {
				String assetPath = new File(JAR_ASSET_FOLDER, jar).getAbsolutePath();
				AssetReader.copyAsset(assetPath, CoreService.getJarFolder(this), this);
			}
			for (String descriptor : descriptors) {
				File descriptorFile = AssetReader.copyAsset(new File(DESCRIPTOR_ASSET_FOLDER, descriptor).getAbsolutePath(), new File(getFilesDir(), CoreService.DESCRIPTOR_FOLDER), this);
				ModuleDescriptor moduleDescriptor = service.parseModuleDescriptor(descriptorFile.getAbsolutePath());
				if (moduleDescriptor != null) {
					modulesInAssets.add(moduleDescriptor);
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Unable to load assets.");
			e.printStackTrace();
		}
		TreeSet<ModuleDescriptor> orderer = new TreeSet<ModuleDescriptor>(loadedModules);
		orderer.addAll(modulesInAssets);
		final List<ModuleDescriptor> orderedModules = new ArrayList<ModuleDescriptor>(orderer);
		final List<Boolean> loadedStates = new ArrayList<Boolean>();
		for (ModuleDescriptor descriptor : orderedModules) {
			loadedStates.add(loadedModules.contains(descriptor));
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				moduleListAdapter.setModules(orderedModules, loadedStates);
			}
		});
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		this.service = null;
	}

	public void loadModule(ModuleDescriptor moduleDescriptor) {
		if (service != null) {
			boolean success = service.addModule(moduleDescriptor);
			if (success) {
				refreshModuleList();
				Toast.makeText(this, "Module " + moduleDescriptor.getModuleName() + " loaded successfully!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Module " + moduleDescriptor.getModuleName() + " couldn't be loaded.", Toast.LENGTH_LONG).show();
			}
		}
		
	}
}