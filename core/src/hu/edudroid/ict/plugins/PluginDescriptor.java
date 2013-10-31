package hu.edudroid.ict.plugins;

import hu.edudroid.interfaces.Plugin;

/**
 * Class describing a plugin that has not been downloaded yet.
 * @author lajthabalazs
 *
 */
public class PluginDescriptor implements Comparable<PluginDescriptor> {
	private final String name;
	private final String packageName;
	private final String description;
	private Plugin plugin;
	
	public PluginDescriptor(String name, String packageName, String description) {
		this.name = name;
		this.packageName = packageName;
		this.description = description;
	}

	public PluginDescriptor(Plugin plugin) {
		this.name = plugin.getName();
		this.packageName = plugin.getClass().getPackage().getName();
		this.description = plugin.getDescription();
		this.plugin = plugin;
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isDownloaded() {
		return (plugin != null);
	}
	
	public Plugin getPlugin() {
		return plugin;
	}

	public void setPlugin(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public int hashCode() {
		return packageName.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PluginDescriptor) {
			return packageName.equals(((PluginDescriptor)o).getPackageName());
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(PluginDescriptor another) {
		return name.compareTo(another.getName());
	}
}