package org.argeo.cms.ui.fs;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Default file browser page for the CMS */
public class MyFilesBrowserProvider implements CmsUiProvider {

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		CmsFsBrowser browser = new CmsFsBrowser(parent, SWT.NO_FOCUS);
		browser.setLayoutData(EclipseUiUtils.fillAll());

		// TODO set input on the default home folder parent for one user's files
		return browser;
	}

}
