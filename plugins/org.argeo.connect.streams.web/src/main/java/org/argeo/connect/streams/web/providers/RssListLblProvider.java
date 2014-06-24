package org.argeo.connect.streams.web.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssTypes;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for entity lists
 */
public class RssListLblProvider extends LabelProvider { 

	private static final long serialVersionUID = 1L;
	private final boolean smallList;

	// Small list by default
	public RssListLblProvider() {
		this(true);
	}

	public RssListLblProvider(boolean smallList) {
		this.smallList = smallList;
	}

	public String getText(Object element) {
		Node node = (Node) element;

		StringBuilder builder = new StringBuilder();
		try {
			if (node.isNodeType(RssTypes.RSS_ITEM)) {
				if (smallList)
					builder.append(RssHtmlProvider.getItemShort(node));
				else
					builder.append(RssHtmlProvider.getItemMedium(node));
			} else if (node.isNodeType(RssTypes.RSS_CHANNEL_INFO)) {
				if (smallList)
					builder.append(RssHtmlProvider.getChannelShort(node));
				else
					builder.append(RssHtmlProvider.getChannelMedium(node));

			} else if (node.isNodeType(NodeType.NT_UNSTRUCTURED))
				builder.append("Unknown node type: ").append(node.toString());
		} catch (RepositoryException re) {
			throw new ArgeoException("Error generating html in RssList", re);
		}
		return builder.toString();
	}
}