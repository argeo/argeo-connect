package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.widgets.ScrolledPage;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TextViewer3 extends ContentViewer {
	private static final long serialVersionUID = -2401274679492339668L;

	private ScrolledPage page;
	private Section mainSection;

	private Composite edited;

	public TextViewer3(Composite parent, Node textNode) {
		try {
			page = new ScrolledPage(parent, SWT.NONE);
			page.setLayout(CmsUtils.noSpaceGridLayout());
			mainSection = new Section(this, SWT.NONE, textNode);
			mainSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			refresh();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot load main section", e);
		}
	}

	@Override
	public Control getControl() {
		return page;
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void refresh() {
		Runnable loadingThread = new Runnable() {

			@Override
			public void run() {
				try {
					mainSection.refresh(true, true);
					page.layout();
				} catch (RepositoryException e) {
					throw new CmsException("Cannot refresh", e);
				}
			}

		};
		page.getDisplay().asyncExec(loadingThread);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
	}

	public void edit(Composite composite) {
		try {
			if (edited != null && edited != composite)
				stopEditing(true);

			if (edited == composite)
				return;

			if (composite instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) composite;
				paragraph.startEditing();
				paragraph.updateContent();
				edited = paragraph;
			} else if (composite instanceof SectionTitle) {
				SectionTitle paragraph = (SectionTitle) composite;
				paragraph.getTitle().startEditing();
				paragraph.updateContent();
				edited = paragraph;
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot edit " + composite, e);
		}
	}

	public void saveEdit() {
		try {
			if (edited != null)
				stopEditing(true);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
		}
	}

	public void cancelEdit() {
		try {
			if (edited != null)
				stopEditing(false);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
		}
	}

	protected void stopEditing(Boolean save) throws RepositoryException {
		if (edited instanceof Paragraph) {
			Paragraph paragraph = (Paragraph) edited;
			if (save)
				paragraph.save((Node) paragraph.getData());
			paragraph.stopEditing();
			paragraph.updateContent();
			edited = null;
		} else if (edited instanceof SectionTitle) {
			SectionTitle sectionTitle = (SectionTitle) edited;
			if (save)
				sectionTitle.getTitle().save((Property) sectionTitle.getData());
			sectionTitle.getTitle().stopEditing();
			sectionTitle.updateContent();
			edited = null;
		}
	}

	Section getMainSection() {
		return mainSection;
	}

	public void layout(Composite composite) {
		composite.layout();
		parentLayout(composite.getParent());
	}

	private void parentLayout(Composite parent) {
		// TODO make it more robust
		parent.layout(true, false);
		if (!(parent instanceof ScrolledPage))
			parentLayout(parent.getParent());
	}

}
