package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.JcrContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/** Read-only display of text. */
public class TextDisplay implements CmsEditable, CmsNames, TextStyles {
	private final String textNodePath;// cache
	private VersionManager versionManager;

	private TextViewer textViewer;

	private Boolean canEdit = false;

	public TextDisplay(Composite parent, Node textNode) {
		try {
			this.textNodePath = textNode.getPath();
			if (textNode.getSession().hasPermission(textNode.getPath(),
					Session.ACTION_ADD_NODE)) {
				canEdit = true;
				versionManager = textNode.getSession().getWorkspace()
						.getVersionManager();
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot initialize text display for "
					+ textNode, e);
		}

		if (canEdit()) {
			TextEditHeader teh = new TextEditHeader(parent, SWT.NONE);
			teh.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		textViewer = new TextViewer(parent, SWT.NONE);
		textViewer.getPage().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		textViewer.setContentProvider(new JcrContentProvider());
		textViewer.setCmsEditable(this);
		textViewer.setInput(textNode);
		textViewer.refresh();
	}

	//
	// CMS EDITABLE IMPLEMENTATION
	//
	public Boolean isEditing() {
		try {
			if (!canEdit())
				return false;
			return versionManager.isCheckedOut(textNodePath);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot check whether " + textNodePath
					+ " is editing", e);
		}
	}

	public void setLayoutData(Object layoutData) {
		textViewer.getPage().setLayoutData(layoutData);
	}

	private class TextEditHeader extends Composite {
		private static final long serialVersionUID = 4186756396045701253L;

		public TextEditHeader(Composite parent, int style) {
			super(parent, style);
			setLayout(CmsUtils.noSpaceGridLayout());
			Button publish = new Button(this, SWT.NONE);
			publish.setText("Publish");
		}

	}

	@Override
	public Boolean canEdit() {
		return canEdit;
	}

	@Override
	public void startEditing() {
		try {
			versionManager.checkout(textNodePath);
			textViewer.refresh();
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot publish " + textNodePath);
		}
		textViewer.refresh();
	}

	@Override
	public void stopEditing() {
		try {
			versionManager.checkin(textNodePath);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot publish " + textNodePath);
		}
		textViewer.refresh();
	}

}
