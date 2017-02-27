package org.argeo.connect.payment;

/** Base specific exception for payments*/
public class PaymentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/** Creates an exception with a message. */
	public PaymentException(String message) {
		super(message);
	}

	/** Creates an exception with a message and a root cause. */
	public PaymentException(String message, Throwable e) {
		super(message, e);
	}

	/**
	 * Chain the messages of all causes (one per line, <b>starts with a line
	 * return</b>) without all the stack
	 */
	public static String chainCausesMessages(Throwable t) {
		StringBuffer buf = new StringBuffer();
		chainCauseMessage(buf, t);
		return buf.toString();
	}

	/** Recursive chaining of messages */
	private static void chainCauseMessage(StringBuffer buf, Throwable t) {
		buf.append('\n').append(' ').append(t.getClass().getCanonicalName())
				.append(": ").append(t.getMessage());
		if (t.getCause() != null)
			chainCauseMessage(buf, t.getCause());
	}
}
