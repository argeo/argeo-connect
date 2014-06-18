package org.argeo.connect.streams.web.pages;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.web.CmsUiProvider;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class RssDynamicPages implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		if (context == null)
			throw new ArgeoException("Context cannot be null");

		// TODO implement page type depending on the node type.
		// Default we display a list of posts.
		parent.setLayout(new GridLayout(2, false));
		return (new SearchPostPage()).createUi(parent, context);
	}

}
