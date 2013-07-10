package hu.edudroid.ict.ui;

import hu.edudroid.ict.R;
import hu.edudroid.interfaces.ModuleDescriptor;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ModuleListAdapter implements ListAdapter, OnClickListener {
	
	private List<ModuleDescriptor> modules;
	private List<Boolean> loadedState;
	private List<DataSetObserver> observers = new ArrayList<DataSetObserver>();
	private LayoutInflater inflater;
	private ModuleOverviewActivity activity;

	public ModuleListAdapter(List<ModuleDescriptor> modules, List<Boolean> loadedState, ModuleOverviewActivity activity, LayoutInflater inflater) {
		this.loadedState = loadedState;
		this.modules = modules;
		this.inflater = inflater;
		this.activity = activity;
	}
	
	public void setModules(List<ModuleDescriptor> modules, List<Boolean> loadedState){
		this.modules = modules;
		this.loadedState = loadedState;
		notifyObservers();
	}
	
	private void notifyObservers() {
		for (DataSetObserver observer : observers) {
			observer.onChanged();
		}
	}

	@Override
	public int getCount() {
		return modules.size();
	}

	@Override
	public Object getItem(int arg0) {
		return modules.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getItemViewType(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ModuleDescriptor module = modules.get(arg0);
		if (arg1 == null) {
			arg1 = inflater.inflate(R.layout.view_module_list_item, null);
		}
		((TextView)arg1.findViewById(R.id.moduleNameLable)).setText(module.getModuleName());
		if (loadedState.get(arg0)) {
			arg1.findViewById(R.id.loadModuleButton).setVisibility(View.INVISIBLE);
			arg1.findViewById(R.id.loadedLabel).setVisibility(View.VISIBLE);
		} else {
			arg1.findViewById(R.id.loadedLabel).setVisibility(View.GONE);
			arg1.findViewById(R.id.loadModuleButton).setVisibility(View.VISIBLE);
			arg1.findViewById(R.id.loadModuleButton).setOnClickListener(this);
			arg1.findViewById(R.id.loadModuleButton).setTag(module);
		}
		arg1.setTag(module);
		return arg1;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return modules.isEmpty();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		observers.add(arg0);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		observers.remove(arg0);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}

	@Override
	public void onClick(View arg0) {
		activity.loadModule((ModuleDescriptor)arg0.getTag());
	}
}