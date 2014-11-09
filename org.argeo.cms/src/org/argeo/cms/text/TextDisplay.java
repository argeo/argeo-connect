package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/** Read-only display of text. */
public class TextDisplay implements CmsEditable, CmsNames, TextStyles {
	private final static Log log = LogFactory.getLog(TextDisplay.class);

	private final String textNodePath;// cache
	private VersionManager versionManager;

	private ComplexTextEditor textViewer;

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
			if (canEdit()) {
				TextEditHeader teh = new TextEditHeader(parent, SWT.NONE);
				teh.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			}
			textViewer = new ComplexTextEditor(parent, textNode, this);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot initialize text display for "
					+ textNode, e);
		}
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
		textViewer.getControl().setLayoutData(layoutData);
	}

	private class TextEditHeader extends Composite {
		private static final long serialVersionUID = 4186756396045701253L;

		public TextEditHeader(Composite parent, int style) {
			super(parent, style);
			setLayout(CmsUtils.noSpaceGridLayout());
			final Button publish = new Button(this, SWT.FLAT);
			publish.setText(getPublishButtonLabel());
			publish.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 2588976132036292904L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (isEditing()) {
						stopEditing();
					} else {
						startEditing();
					}
					publish.setText(getPublishButtonLabel());
				}
			});
		}

		private String getPublishButtonLabel() {
			if (isEditing())
				return "Publish";
			else
				return "Edit";
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
			throw new CmsException("Cannot publish " + textNodePath, e1);
		}
		textViewer.refresh();
	}

	private class DCListener implements IDoubleClickListener {

		@Override
		public void doubleClick(DoubleClickEvent event) {
			log.debug(event);
		}

	}

	private class OListener implements IOpenListener {

		@Override
		public void open(OpenEvent event) {
			log.debug(event);
		}

	}
}
