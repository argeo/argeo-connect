package org.argeo.connect.streams.web;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */

public class StreamsWebActivator implements BundleActivator {

	private static StreamsWebActivator plugin;

	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	static StreamsWebActivator getDefault() {
		return plugin;
	}
}