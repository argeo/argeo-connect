package org.argeo.connect.payment.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PaymentUiPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.argeo.connect.payment.ui"; //$NON-NLS-1$
	private static PaymentUiPlugin plugin;
	private static BundleContext bundleContext;

	public PaymentUiPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		bundleContext = null;
		super.stop(context);
	}

	public static PaymentUiPlugin getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}