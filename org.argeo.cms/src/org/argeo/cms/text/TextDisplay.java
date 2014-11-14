package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.viewers.JcrVersionCmsEditable;
import org.argeo.cms.widgets.ScrolledPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/** Read-only display of text. */
public class TextDisplay implements CmsNames, TextStyles {
	private CmsEditable cmsEditable;
	private ScrolledPage page;

	public TextDisplay(Composite parent, Node textNode) {
		try {
			cmsEditable = new JcrVersionCmsEditable(textNode);
			if (cmsEditable.canEdit()) {
				TextEditorHeader teh = new TextEditorHeader(cmsEditable,
						parent, SWT.NONE);
				teh.setLayoutData(CmsUtils.fillWidth());
			}

			page = new ScrolledPage(parent, SWT.NONE);
			page.setLayout(CmsUtils.noSpaceGridLayout());
			new StandardTextEditor(page, SWT.NONE, textNode, cmsEditable);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot initialize text display for "
					+ textNode, e);
		}
	}

	public void setLayoutData(Object layoutData) {
		page.setLayoutData(layoutData);
	}
}
