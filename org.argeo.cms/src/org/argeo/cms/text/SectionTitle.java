package org.argeo.cms.text;

import org.argeo.cms.CmsUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/** The title of a section. */
public class SectionTitle extends Composite {
	private static final long serialVersionUID = -1787983154946583171L;

	public SectionTitle(Composite parent, int style, String title,
			int depth) {
		super(parent, style);
		setLayout(CmsUtils.noSpaceGridLayout());

		Label label = new Label(this, SWT.NONE);
		if (depth == 0)
			label.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_TITLE);
		else
			label.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_H + depth);
		label.setText(title);
	}

}
