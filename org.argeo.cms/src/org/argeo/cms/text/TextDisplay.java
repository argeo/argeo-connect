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
	// private final static Log log = LogFactory.getLog(TextDisplay.class);

	private CmsEditable cmsEditable;
	// private final String textNodePath;// cache
	// private VersionManager versionManager;

	// private ComplexTextEditor textViewer;

	// private Boolean canEdit = false;
	private ScrolledPage page;

	public TextDisplay(Composite parent, Node textNode) {
		try {
			// this.textNodePath = textNode.getPath();
			// if (textNode.getSession().hasPermission(textNode.getPath(),
			// Session.ACTION_ADD_NODE)) {
			// canEdit = true;
			// versionManager = textNode.getSession().getWorkspace()
			// .getVersionManager();
			// }
			cmsEditable = new JcrVersionCmsEditable(textNode);
			if (cmsEditable.canEdit()) {
				TextEditorHeader teh = new TextEditorHeader(cmsEditable,
						parent, SWT.NONE);
				teh.setLayoutData(CmsUtils.fillWidth());
			}

			page = new ScrolledPage(parent, SWT.NONE);
			page.setLayout(CmsUtils.noSpaceGridLayout());
			new ComplexTextEditor(page, textNode, cmsEditable);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot initialize text display for "
					+ textNode, e);
		}
	}

	public void setLayoutData(Object layoutData) {
		page.setLayoutData(layoutData);
	}

	// private class TextEditHeader extends Composite {
	// private static final long serialVersionUID = 4186756396045701253L;
	//
	// public TextEditHeader(Composite parent, int style) {
	// super(parent, style);
	// setLayout(CmsUtils.noSpaceGridLayout());
	// final Button publish = new Button(this, SWT.FLAT);
	// publish.setText(getPublishButtonLabel());
	// publish.addSelectionListener(new SelectionAdapter() {
	// private static final long serialVersionUID = 2588976132036292904L;
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// if (cmsEditable.isEditing()) {
	// cmsEditable.stopEditing();
	// } else {
	// cmsEditable.startEditing();
	// }
	// publish.setText(getPublishButtonLabel());
	// }
	// });
	// }
	//
	// private String getPublishButtonLabel() {
	// if (cmsEditable.isEditing())
	// return "Publish";
	// else
	// return "Edit";
	// }
	// }

	//
	// CMS EDITABLE IMPLEMENTATION
	//
	// public Boolean isEditing() {
	// try {
	// if (!canEdit())
	// return false;
	// return versionManager.isCheckedOut(textNodePath);
	// } catch (RepositoryException e) {
	// throw new CmsException("Cannot check whether " + textNodePath
	// + " is editing", e);
	// }
	// }
	// @Override
	// public Boolean canEdit() {
	// return canEdit;
	// }
	//
	// @Override
	// public void startEditing() {
	// try {
	// versionManager.checkout(textNodePath);
	// //textViewer.refresh();
	// } catch (RepositoryException e1) {
	// throw new CmsException("Cannot publish " + textNodePath);
	// }
	// textViewer.refresh();
	// }
	//
	// @Override
	// public void stopEditing() {
	// try {
	// versionManager.checkin(textNodePath);
	// } catch (RepositoryException e1) {
	// throw new CmsException("Cannot publish " + textNodePath, e1);
	// }
	// textViewer.refresh();
	// }

}
