package org.argeo.connect.streams;

import javax.jcr.Node;
import javax.jcr.Session;

/** Retrieve items and manages them in the repository. */
public interface RssManager {
	/**
	 * Get or create a new channel. Will need to access the underlying feed,
	 * which must be vailable.
	 */
	public Node getOrCreateChannel(Session session, String url);

	/** Retrieve items */
	public void retrieveItems();

}
