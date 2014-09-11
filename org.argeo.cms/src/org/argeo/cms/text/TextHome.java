package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsLink;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUiProvider;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.UserJcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class TextHome implements CmsUiProvider, CmsNames {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		Node userHome = UserJcrUtils.getUserHome(context.getSession());

		new Label(parent, SWT.NONE).setText("Drafts");

		Node drafts = JcrUtils.getOrAdd(userHome, CMS_DRAFTS);
		for (NodeIterator ni = drafts.getNodes(); ni.hasNext();) {
			Node textNode = ni.nextNode();
			new CmsLink(textNode.getName(), textNode.getPath()).createUi(
					parent, textNode);
		}
		return null;
	}

}
