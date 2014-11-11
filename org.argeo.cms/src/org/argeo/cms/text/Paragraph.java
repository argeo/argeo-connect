package org.argeo.cms.text;

import javax.jcr.Node;

import org.argeo.cms.CmsUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Paragraph extends StyledComposite {
	private static final long serialVersionUID = 3746457776229542887L;

	private final Section section;

	public Paragraph(Section parent, int style) {
		super(parent, style);
		setLayout(CmsUtils.noSpaceGridLayout());
		CmsUtils.style(this, TextStyles.TEXT_PARAGRAPH);
		section = parent;
	}

	public Node getNode() {
		return (Node) CmsUtils.getDataItem(this, null);
	}

	public Section getSection() {
		return section;
	}

	public Paragraph nextParagraph() {
		Control[] children = getSection().getChildren();
		for (int i = 0; i < children.length; i++) {
			if (this == children[i])
				if (i + 1 < children.length) {
					Composite next = (Composite) children[i + 1];
					return (Paragraph) next;
				} else {
					// next section
				}
		}
		return null;
	}

	public Paragraph previousParagraph() {
		Control[] children = getSection().getChildren();
		for (int i = 0; i < children.length; i++) {
			if (this == children[i])
				if (i != 0) {
					Composite previous = (Composite) children[i - 1];
					return (Paragraph) previous;
				} else {
					// next section
				}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Paragraph " + getData();
	}
}
