package org.argeo.connect.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.springframework.security.context.SecurityContextHolder;

public class CmsLogin implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		String username = SecurityContextHolder.getContext()
				.getAuthentication().getName();
		if (username.equals("anonymous"))
			username = null;

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		Label l = new Label(comp, SWT.NONE);
		l.setData(RWT.CUSTOM_VARIANT, CmsStyles.CMS_LOGIN);
		l.setData(RWT.MARKUP_ENABLED, true);
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		if (username != null)
			l.setText("Logged in as <b>" + username + "</b>");
		else
			l.setText("Log in");
		return l;
	}

}
