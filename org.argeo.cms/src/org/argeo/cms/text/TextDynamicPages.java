package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.cms.CmsLink;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUiProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Display the text of the context, and provide an editor if the user can edit. */
public class TextDynamicPages implements CmsUiProvider, CmsNames {
	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		if (context.isNodeType(CmsTypes.CMS_TEXT)) {
			new TextDisplay(parent, context);
		} else if (context.isNodeType(NodeType.NT_FOLDER)
				|| context.getPath().equals("/")) {
			Node indexNode = JcrUtils.getOrAdd(context, CMS_INDEX,
					CmsTypes.CMS_TEXT);
			TextDisplay textDisplay = new TextDisplay(parent, indexNode);
			GridData textGd = new GridData(SWT.FILL, SWT.FILL, true, false);
			textGd.heightHint = 400;
			textDisplay.setLayoutData(textGd);

			for (NodeIterator ni = context.getNodes(); ni.hasNext();) {
				Node textNode = ni.nextNode();
				if (textNode.isNodeType(NodeType.NT_FOLDER))
					new CmsLink(textNode.getName() + "/", textNode.getPath())
							.createUi(parent, textNode);
			}
			for (NodeIterator ni = context.getNodes(); ni.hasNext();) {
				Node textNode = ni.nextNode();
				if (textNode.isNodeType(CmsTypes.CMS_TEXT)
						&& !textNode.getName().equals(CMS_INDEX))
					new CmsLink(textNode.getName(), textNode.getPath())
							.createUi(parent, textNode);
			}
		}

		return null;
	}
}
