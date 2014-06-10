package org.argeo.connect.web;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class CmsEntryPoint extends AbstractEntryPoint {
	private final Session session;

	public CmsEntryPoint(Session node) {
		super();
		this.session = node;
	}

	@Override
	protected void createContents(Composite parent) {
		try {
			GridLayout layout = new GridLayout(1, true);
			parent.setLayout(layout);
			new Label(parent, SWT.NONE).setText("Logged-in as "
					+ session.getUserID());
			for (NodeIterator nIt = session.getRootNode().getNodes(); nIt
					.hasNext();) {
				Node node = nIt.nextNode();
				new Label(parent, SWT.NONE).setText(node.getName() + " ("
						+ node.getPrimaryNodeType().getName() + ")");
			}

		} catch (Exception e) {
			throw new ArgeoException("Cannot create entrypoint contents", e);
		}
	}

}
