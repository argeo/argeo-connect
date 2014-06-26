package org.argeo.connect.people;

/**
 * Connect People's specific exception. For the time being, it just wraps a
 * usual RuntimeException
 */
public class PeopleException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PeopleException(String message) {
		super(message);
	}

	public PeopleException(String message, Throwable e) {
		super(message, e);
	}

}
