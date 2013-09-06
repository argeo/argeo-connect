package org.argeo.connect.streams.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for entity lists
 */
public class RssListLabelProvider extends LabelProvider implements RssNames {

	private static final long serialVersionUID = 1L;
	private final boolean smallList;

	// Small list by default
	public RssListLabelProvider() {
		this(true);
	}

	public RssListLabelProvider(boolean smallList) {
		this.smallList = smallList;
	}

	@Override
	public String getText(Object element) {
		Node node = (Node) element;
		StringBuilder builder = new StringBuilder();
		try {
			if (node.isNodeType(RssTypes.RSS_ITEM)) {
				if (smallList)
					builder.append(RssHtmlProvider.getItemShort(node));
				else
					builder.append(RssHtmlProvider.getItemMedium(node));
			} else if (node.isNodeType(RssTypes.RSS_CHANNEL)) {
				if (smallList)
					builder.append(RssHtmlProvider.getChannelShort(node));
				else
					builder.append(RssHtmlProvider.getChannelMedium(node));

			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Error generating html in RssList", re);
		}
		return builder.toString();
	}
}
