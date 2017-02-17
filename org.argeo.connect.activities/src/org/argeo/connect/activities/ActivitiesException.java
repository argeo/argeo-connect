package org.argeo.connect.activities;

public class ActivitiesException extends RuntimeException {
	private static final long serialVersionUID = -5385199495065133392L;

	public ActivitiesException(String message) {
		super(message);
	}

	public ActivitiesException(String message, Throwable e) {
		super(message, e);
	}
}
