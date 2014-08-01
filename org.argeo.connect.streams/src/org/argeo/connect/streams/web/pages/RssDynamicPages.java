package org.argeo.connect.streams.web.pages;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.cms.CmsUiProvider;
import org.argeo.connect.streams.RssTypes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class RssDynamicPages implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		if (context == null)
			throw new ArgeoException("Context cannot be null");

		if (context.isNodeType(RssTypes.RSS_CHANNEL_INFO))
			return (new ChannelPage()).createUi(parent, context);

		parent.setLayout(new GridLayout());
		new Label(parent, SWT.NONE).setText("Unknown node type.");

		// TODO implement page type depending on the node type.
		// Default we display a list of posts.
		return null;
	}

}
