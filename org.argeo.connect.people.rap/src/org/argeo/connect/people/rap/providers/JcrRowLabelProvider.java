package org.argeo.connect.people.rap.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

import org.argeo.ArgeoException;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;

/**
 * Wraps the getText() method of the SimpleJcrNodeLabelProvider. Retrieves
 * relevant node using the selector name or the Row.getNode() method if no name
 * has been provided considering then that the row contains only one node
 * (typically for xpath queries)
 */
public class JcrRowLabelProvider extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 3265805393751537765L;

	private String selectorName;

	public JcrRowLabelProvider(String selectorName, String propertyName) {
		super(propertyName);
		if (CommonsJcrUtils.checkNotEmptyString(selectorName))
			this.selectorName = selectorName;
	}

	public JcrRowLabelProvider(String propertyName) {
		super(propertyName);
	}

	@Override
	public String getText(Object element) {
		try {
			Row currRow = (Row) element;
			Node currNode = null;
			if (selectorName != null)
				currNode = currRow.getNode(selectorName);
			else
				currNode = currRow.getNode();
			return super.getText(currNode);
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get Node " + selectorName
					+ " from row " + element, re);
		}
	}
}