package org.argeo.people;

/** People specific exception. Simply wraps a <code>RuntimeException</code> */
public class PeopleException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PeopleException(String message) {
		super(message);
	}

	public PeopleException(String message, Throwable e) {
		super(message, e);
	}
}
