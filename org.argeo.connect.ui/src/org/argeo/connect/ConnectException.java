package org.argeo.connect;

/** Connect specific exception. Wraps a usual RuntimeException */
public class ConnectException extends RuntimeException {
	private static final long serialVersionUID = 5988423754597669946L;

	public ConnectException(String message) {
		super(message);
	}

	public ConnectException(String message, Throwable e) {
		super(message, e);
	}
}
