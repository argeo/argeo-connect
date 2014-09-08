package org.argeo.connect.cms;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.application.ExceptionHandler;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

/** Configures an Argeo CMS RWT application. */
public class CmsApplication implements ApplicationConfiguration,
		BundleContextAware {
	final static Log log = LogFactory.getLog(CmsApplication.class);

	private Map<String, EntryPointFactory> entryPoints = new HashMap<String, EntryPointFactory>();
	private Map<String, Map<String, String>> entryPointsBranding = new HashMap<String, Map<String, String>>();
	private Map<String, List<String>> styleSheets = new HashMap<String, List<String>>();

	private List<String> resources = new ArrayList<String>();

	// private Bundle clientScriptingBundle;
	private BundleContext bundleContext;

	public void configure(Application application) {
		try {
			application.setOperationMode(OperationMode.SWT_COMPATIBILITY);
			application.setExceptionHandler(new CmsExceptionHandler());

			// loading gif
			application.addResource("icons/loading.gif",
					createResourceLoader("icons/loading.gif"));

			for (String resource : resources) {
				URL res = bundleContext.getBundle().getResource(resource);
				application.addResource(resource, new UrlResourceLoader(res));
				if (log.isDebugEnabled())
					log.debug("Registered resource " + resource);
			}

			// entry points
			for (String entryPoint : entryPoints.keySet()) {
				Map<String, String> properties = new HashMap<String, String>();
				if (entryPointsBranding.containsKey(entryPoint)) {
					properties = entryPointsBranding.get(entryPoint);
					if (properties.containsKey(WebClient.FAVICON)) {
						String faviconRelPath = properties
								.get(WebClient.FAVICON);
						URL res = bundleContext.getBundle().getResource(
								faviconRelPath);
						application.addResource(faviconRelPath,
								new UrlResourceLoader(res));
						if (log.isTraceEnabled())
							log.trace("Registered favicon " + faviconRelPath);

					}

					if (!properties.containsKey(WebClient.BODY_HTML))
						properties.put(WebClient.BODY_HTML,
								DEFAULT_LOADING_BODY);
				}

				application.addEntryPoint("/" + entryPoint,
						entryPoints.get(entryPoint), properties);
				log.info("Registered entry point /" + entryPoint);
			}

			// stylesheets
			for (String themeId : styleSheets.keySet()) {
				List<String> cssLst = styleSheets.get(themeId);
				for (String css : cssLst) {
					URL res = bundleContext.getBundle().getResource(css);
					application.addStyleSheet(themeId, css,
							new UrlResourceLoader(res));
				}

			}

			// registerClientScriptingResources(application);
		} catch (RuntimeException e) {
			// Easier access to initialisation errors
			log.error("Unexpected exception when configuring RWT application.",
					e);
			throw e;
		}
	}

	// see Eclipse.org bug 369957
	// private void registerClientScriptingResources(Application application) {
	// if (clientScriptingBundle != null) {
	// String className =
	// "org.eclipse.rap.clientscripting.internal.resources.ClientScriptingResources";
	// try {
	// Class<?> resourceClass = clientScriptingBundle
	// .loadClass(className);
	// Method registerMethod = resourceClass.getMethod("register",
	// Application.class);
	// registerMethod.invoke(null, application);
	// } catch (Exception exception) {
	// throw new RuntimeException(exception);
	// }
	// }
	// }

	private static ResourceLoader createResourceLoader(final String resourceName) {
		return new ResourceLoader() {
			public InputStream getResourceAsStream(String resourceName)
					throws IOException {
				return getClass().getClassLoader().getResourceAsStream(
						resourceName);
			}
		};
	}

	public void setEntryPoints(
			Map<String, EntryPointFactory> entryPointFactories) {
		this.entryPoints = entryPointFactories;
	}

	public void setEntryPointsBranding(
			Map<String, Map<String, String>> entryPointBranding) {
		this.entryPointsBranding = entryPointBranding;
	}

	public void setStyleSheets(Map<String, List<String>> styleSheets) {
		this.styleSheets = styleSheets;
	}

	// public void setClientScriptingBundle(Bundle clientScriptingBundle) {
	// this.clientScriptingBundle = clientScriptingBundle;
	// }

	public void setBundleContext(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}

	public void setResources(List<String> resources) {
		this.resources = resources;
	}

	class CmsExceptionHandler implements ExceptionHandler {

		@Override
		public void handleException(Throwable throwable) {
			CmsSession.current.get().exception(throwable);
		}

	}

	/*
	 * TEXTS
	 */
	private static String DEFAULT_LOADING_BODY = "<div"
			+ " style=\"position: absolute; left: 50%; top: 50%; margin: -32px -32px; width: 64px; height:64px\">"
			+ "<img src=\"./rwt-resources/icons/loading.gif\" width=\"32\" height=\"32\" style=\"margin: 16px 16px\"/>"
			+ "</div>";
}