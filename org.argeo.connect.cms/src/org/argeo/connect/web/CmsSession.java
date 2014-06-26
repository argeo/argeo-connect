package org.argeo.connect.web;

/** Provides interaction with the CMS system */
public interface CmsSession {
	public final static String KEY = "org.argeo.connect.web.cmsSession";

	final ThreadLocal<CmsSession> current = new ThreadLocal<CmsSession>();

	public void navigateTo(String state);

	public void authChange();

	public void exception(Throwable e);
}
