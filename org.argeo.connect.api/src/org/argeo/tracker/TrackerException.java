package org.argeo.tracker;

/** Tracker specific exception. Wraps an usual RuntimeException */
public class TrackerException extends RuntimeException {
	private static final long serialVersionUID = 3514802367100606183L;

	public TrackerException(String message) {
		super(message);
	}

	public TrackerException(String message, Throwable e) {
		super(message, e);
	}
}
