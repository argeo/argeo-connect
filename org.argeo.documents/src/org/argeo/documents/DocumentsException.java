package org.argeo.documents;

public class DocumentsException extends RuntimeException {
	private static final long serialVersionUID = -5385199495065133392L;

	public DocumentsException(String message) {
		super(message);
	}

	public DocumentsException(String message, Throwable e) {
		super(message, e);
	}
}
