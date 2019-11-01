package org.argeo.connect.streams.web.listeners;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssTypes;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Centralises the management of double click on an item in a list.
 */
public class NodeListDoubleClickListener implements IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
		if (event.getSelection() == null || event.getSelection().isEmpty())
			return;
		Object obj = ((IStructuredSelection) event.getSelection())
				.getFirstElement();
		if (obj instanceof Node) {
			try {
				Node curNode = (Node) obj;
				if (curNode.isNodeType(RssTypes.RSS_CHANNEL_INFO)) {
					// TODO implement browsing to the correct page.
				}
			} catch (RepositoryException re) {
				throw new ArgeoException("Unable to open editor for node", re);
			}
		}
	}
}