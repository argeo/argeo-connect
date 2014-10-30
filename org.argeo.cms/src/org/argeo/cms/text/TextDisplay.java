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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/** Read-only display of text. */
public class TextDisplay implements CmsEditable, CmsNames, TextStyles {
	private final static Log log = LogFactory.getLog(TextDisplay.class);

	private final String textNodePath;// cache
	private VersionManager versionManager;

	private TextViewer3 textViewer;

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
		// textViewer = new TextViewer(parent, SWT.NONE);
		// textViewer.setCmsEditable(this);

		// textViewer = new TextViewer2(new ScrolledPage(parent, SWT.NONE));
		// // textViewer.setLabelProvider(new TextLabelProvider());
		// TextViewerColumn column = new TextViewerColumn(textViewer,
		// textViewer.getControl());
		// column.setEditingSupport(new TextEditingSupport(textViewer,
		// new IdentityTextInterpreter()));
		// column.setLabelProvider(new TextLabelProvider());
		//
		// textViewer.addDoubleClickListener(new DCListener());
		// textViewer.addOpenListener(new OListener());
		//
		// textViewer.setContentProvider(new JcrContentProvider());
		// textViewer.getControl().setLayoutData(
		// new GridData(SWT.FILL, SWT.FILL, true, true));
		// textViewer.setInput(textNode);
		// textViewer.refresh();

		// try {
		// ScrolledPage page = new ScrolledPage(parent, SWT.NONE);
		// Section mainSection = new Section(page, SWT.NONE, textNode);
		// mainSection.refresh(true, true);
		// } catch (RepositoryException e) {
		// throw new CmsException("Cannot load main section", e);
		// }
		textViewer = new TextViewer3(parent, textNode, this);
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
