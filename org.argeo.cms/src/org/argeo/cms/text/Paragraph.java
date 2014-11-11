package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUtils;
import org.argeo.cms.widgets.EditableText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Paragraph extends EditableText implements SectionPart {
	private static final long serialVersionUID = 3746457776229542887L;

	private final Section section;

	private transient Node node;
	private String nodeId;

	public Paragraph(Section section, int style, Node node)
			throws RepositoryException {
		super(section, style);
		if (SWT.READ_ONLY != (style & SWT.READ_ONLY)) {
			this.node = node;
		}
		this.nodeId = node.getIdentifier();
		this.section = section;
		setLayout(CmsUtils.noSpaceGridLayout());
		CmsUtils.style(this, TextStyles.TEXT_PARAGRAPH);
	}

	public Node getNode() throws RepositoryException {
		return node;
	}

	public String getNodeId() {
		return nodeId;
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
