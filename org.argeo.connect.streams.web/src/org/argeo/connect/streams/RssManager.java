package org.argeo.connect.streams;

import javax.jcr.Node;
import javax.jcr.Session;

/** Retrieves on-line items and store them in the repository. */
public interface RssManager {
	/**
	 * Gets or creates a new channel. Will need to access the underlying feed,
	 * which must be available.
	 */
	public Node getOrCreateChannel(Session session, String url);

	/** Retrieves items */
	public void retrieveItems();

}
