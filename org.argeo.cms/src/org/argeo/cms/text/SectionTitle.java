package org.argeo.cms.text;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** The title of a section. */
public class SectionTitle extends Composite {
	private static final long serialVersionUID = -1787983154946583171L;

	private StyledComposite title;
	private int relativeDepth;

	public SectionTitle(Section parent, int style, Property title,
			int relativeDepth) throws RepositoryException {
		super(parent, style);
		this.relativeDepth = relativeDepth;
		setLayout(CmsUtils.noSpaceGridLayout());
		setData(title);
		// editableTextPart = new StyledComposite(parent, SWT.NONE, title);
		// editableTextPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		// false));
		// if (relativeDepth == 0)
		// editableTextPart.setStyle(TextStyles.TEXT_TITLE);
		// else
		// editableTextPart.setStyle(TextStyles.TEXT_H + relativeDepth);
	}

	protected Property getProperty() {
		return (Property) getData();
	}

	public void refresh(Boolean updateContent) throws RepositoryException {
		for (Control child : getChildren())
			child.dispose();
		if (getProperty() != null) {
			title = new StyledComposite(this, SWT.NONE, getSection()
					.getTextInterpreter());
			title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			if (updateContent)
				updateContent();
		}
	}

	public void updateContent() throws RepositoryException {
		if (title != null) {
			if (relativeDepth == 0)
				title.setStyle(TextStyles.TEXT_TITLE);
			else
				title.setStyle(TextStyles.TEXT_H + relativeDepth);

			// retrieve control AFTER setting style, since it may have been
			// reset
			title.setText(getProperty());

			layout();
		}
	}

	public StyledComposite getTitle() {
		return title;
	}

	public Section getSection() {
		return (Section) getParent();
	}

	// public TextViewer getViewer() {
	// return getSection().getViewer();
	// }
}
