package org.argeo.connect.web;

public interface CmsSession {
	public final static String KEY = "org.argeo.connect.web.cmsSession";

	public void navigateTo(String state);
}
