/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.demo.gr.ui;

import java.net.URL;
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

	public URL imageUrl(String path) {
		return bundleContext.getBundle().getResource(path);
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
	// public static String getMessage(String key) {
	// try {
	// return getDefault().messages.getString(key.replace(':', '_'));
	// } catch (NullPointerException npe) {
	// log.warn(key.replace(':', '_') + " not found.");
	// return key;
	// }
	// }

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
