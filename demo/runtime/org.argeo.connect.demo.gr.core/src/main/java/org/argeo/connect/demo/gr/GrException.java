package org.argeo.connect.demo.gr;

/** Connect demo specific exception */
public class GrException extends RuntimeException {
	private static final long serialVersionUID = 3514802367100606183L;

	public GrException(String message) {
		super(message);
	}

	public GrException(String message, Throwable cause) {
		super(message, cause);
	}

}
