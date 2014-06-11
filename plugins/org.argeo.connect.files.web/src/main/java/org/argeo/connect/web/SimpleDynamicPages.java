package org.argeo.connect.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class SimpleDynamicPages implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		if (context == null)
			throw new ArgeoException("Context cannot be null");
		new Label(parent, SWT.NONE).setText(context.getPath());
		return null;
	}

}
