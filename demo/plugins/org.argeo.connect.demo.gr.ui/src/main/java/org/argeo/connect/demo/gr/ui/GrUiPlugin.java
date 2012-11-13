package org.argeo.connect.demo.gr.ui;

import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GrUiPlugin extends AbstractUIPlugin {
	private final static Log log = LogFactory.getLog(GrUiPlugin.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "org.argeo.connect.demo.gr.ui";

	// The shared instance
	private static GrUiPlugin plugin;

	private BundleContext bundleContext;

	private ResourceBundle messages;

	/** Default constructor */
	public GrUiPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.bundleContext = context;
		plugin = this;
		messages = ResourceBundle
				.getBundle("org.argeo.connect.demo.gr.ui.messages");
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
	public static GrUiPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public FormColors getFormColors(Display display) {
		return new FormColors(display);
	}

	/** Returns the internationalized label for the given key */
	public static String getMessage(String key) {
		try {
			return getDefault().messages.getString(key.replace(':', '_'));
		} catch (NullPointerException npe) {
			log.warn(key.replace(':', '_') + " not found.");
			return key;
		}
	}

	/**
	 * Gives access to the internationalization message bundle. Returns null in
	 * case the ClientUiPlugin is not started (for JUnit tests, by instance)
	 */
	public static ResourceBundle getMessagesBundle() {
		if (getDefault() != null)
			// To avoid NPE
			return getDefault().messages;
		else
			return null;
	}

	public static void showExceptionDialog(String message, Exception e) {
		MessageDialog.openError(Display.getDefault().getActiveShell(), "Error",
				message + ": " + e.getMessage());
		log.error(message, e);
	}
}
