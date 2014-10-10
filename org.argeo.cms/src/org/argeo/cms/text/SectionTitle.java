package org.argeo.cms.text;

import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/** The title of a section. */
public class SectionTitle extends Composite {
	private static final long serialVersionUID = -1787983154946583171L;

	private final StyledComposite editableTextPart;

	public SectionTitle(Composite parent, int style, Property title,
			int relativeDepth) throws RepositoryException {
		super(parent, style);
		setLayout(CmsUtils.noSpaceGridLayout());
		editableTextPart = new StyledComposite(parent, SWT.NONE, title);
		editableTextPart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		if (relativeDepth == 0)
			editableTextPart.setStyle(TextStyles.TEXT_TITLE);
		else
			editableTextPart.setStyle(TextStyles.TEXT_H + relativeDepth);
	}

	public EditableTextPart getEditableTextPart() {
		return editableTextPart;
	}
}
