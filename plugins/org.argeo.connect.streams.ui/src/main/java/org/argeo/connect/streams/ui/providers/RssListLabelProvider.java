package org.argeo.connect.streams.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.ui.commands.DeleteEntity;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for entity lists
 */
public class RssListLabelProvider extends LabelProvider implements RssNames {

	private static final long serialVersionUID = 1L;
	private final boolean smallList;

	private String propName;

	// Small list by default
	public RssListLabelProvider() {
		this(true);
	}

	public RssListLabelProvider(boolean smallList) {
		this.smallList = smallList;
	}

	/**
	 * @param propName
	 *            optionally the key to recognise the current column
	 * @param parentVersionableNode
	 *            the ancestor node that is versionable to manage remove/edit
	 *            action in case we are dealing such actions.
	 */
	public RssListLabelProvider(boolean smallList, String propName) {
		this.smallList = smallList;
		this.propName = propName;

	}

	@Override
	public String getText(Object element) {
		Node node = (Node) element;

		if (DeleteEntity.ID.equals(propName)) {
			String currStr = getRemoveText(node);
			return currStr;
		}

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

			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Error generating html in RssList", re);
		}
		return builder.toString();
	}
	
	public String getRemoveText(Node currNode) {
		return PeopleHtmlUtils.getRemoveSnippetForLists(currNode,
				true);
	}
	
}
