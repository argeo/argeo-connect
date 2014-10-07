package org.argeo.connect.people.web;


/**
 * Provide utilities to register images.
 */
public class PeopleWebImages {
	// private final static Log log = LogFactory.getLog(PeopleWebImages.class);
	//
	// private final BundleContext bundleContext;
	//
	// public PeopleWebImages(BundleContext bundleContext) {
	// this.bundleContext = bundleContext;
	// }
	//
	// public String registerImageIfNeeded(String image) {
	// ResourceManager resourceManager = RWT.getResourceManager();
	// if (!resourceManager.isRegistered(image)) {
	// URL res = getImageUrl(image);
	// InputStream inputStream = null;
	// try {
	// IOUtils.closeQuietly(inputStream);
	// inputStream = res.openStream();
	// resourceManager.register(image, inputStream);
	// if (log.isTraceEnabled())
	// log.trace("Registered image " + image);
	// } catch (Exception e) {
	// throw new CmsException("Cannot load image " + image, e);
	// } finally {
	// IOUtils.closeQuietly(inputStream);
	// }
	// }
	// return resourceManager.getLocation(image);
	// }
	//
	// private URL getImageUrl(String image) {
	// URL url;
	// try {
	// // pure URL
	// url = new URL(image);
	// } catch (MalformedURLException e1) {
	// // in OSGi bundle
	// if (bundleContext == null)
	// throw new CmsException("No bundle context available");
	// url = bundleContext.getBundle().getResource(image);
	// }
	// if (url == null)
	// throw new CmsException("No image " + image + " available.");
	// return url;
	// }
}
