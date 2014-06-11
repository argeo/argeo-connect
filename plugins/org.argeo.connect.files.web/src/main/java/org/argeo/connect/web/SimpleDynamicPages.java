package org.argeo.connect.web;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class SimpleDynamicPages implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		if (context == null)
			throw new ArgeoException("Context cannot be null");
		parent.setLayout(new GridLayout(2, false));

		// parent
		if (!context.getPath().equals("/")) {
			new CmsLink("..", context.getParent().getPath()).createUi(
					parent, context);
			new Label(parent, SWT.NONE).setText(context.getParent().getPrimaryNodeType()
					.getName());
		}

		// context
		Label contextL = new Label(parent, SWT.NONE);
		contextL.setData(RWT.MARKUP_ENABLED, true);
		contextL.setText("<b>"+context.getName()+"</b>");
		new Label(parent, SWT.NONE).setText(context.getPrimaryNodeType()
				.getName());

		// children
		for (NodeIterator nIt = context.getNodes(); nIt.hasNext();) {
			Node child = nIt.nextNode();
			new CmsLink(child.getName(), child.getPath()).createUi(
					parent, context);

			new Label(parent, SWT.NONE).setText(child.getPrimaryNodeType()
					.getName());
		}

		return null;
	}

}
