package org.argeo.connect.streams.web;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */

public class StreamsActivator implements BundleActivator {

	private static StreamsActivator plugin;

	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	static StreamsActivator getDefault() {
		return plugin;
	}
}