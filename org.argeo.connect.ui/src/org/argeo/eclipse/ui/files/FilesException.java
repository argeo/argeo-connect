package org.argeo.eclipse.ui.files;

/** Files specific exception */
public class FilesException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public FilesException(String message) {
		super(message);
	}

	public FilesException(String message, Throwable e) {
		super(message, e);
	}
}
