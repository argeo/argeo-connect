package org.argeo.connect.web;

/** Provides interaction with the CMS system */
public interface CmsSession {
	public final static String KEY = "org.argeo.connect.web.cmsSession";

	public void navigateTo(String state);

	public void authChange();
}
