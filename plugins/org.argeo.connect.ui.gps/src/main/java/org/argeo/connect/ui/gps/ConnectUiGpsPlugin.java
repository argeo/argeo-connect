package org.argeo.connect.ui.gps;

import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ConnectUiGpsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String ID = "org.argeo.connect.ui.gps";

	// The shared instance
	private static ConnectUiGpsPlugin plugin;

	// Internationalized labels for UI gps
	private ResourceBundle messages_gps;

	/**
	 * The constructor
	 */
	public ConnectUiGpsPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		messages_gps = ResourceBundle.getBundle("messages_gps");
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static ConnectUiGpsPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(ID, path);
	}

	public static String getGPSMessage(String key) {
		return getDefault().messages_gps.getString(key.replace(':', '_'));
	}

}
