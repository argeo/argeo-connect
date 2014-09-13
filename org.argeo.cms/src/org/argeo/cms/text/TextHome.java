package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsLink;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUiProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class TextHome implements CmsUiProvider, CmsNames {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		Node indexNode = JcrUtils.getOrAdd(context, CMS_INDEX,
				CmsTypes.CMS_TEXT);
		TextDisplay textDisplay = new TextDisplay(parent, indexNode);

		new Label(parent, SWT.NONE).setText("Pages");

		for (NodeIterator ni = context.getNodes(); ni.hasNext();) {
			Node textNode = ni.nextNode();
			if (textNode.isNodeType(CmsTypes.CMS_TEXT))
				new CmsLink(textNode.getName(), textNode.getPath()).createUi(
						parent, textNode);
		}
		return null;
	}

}
