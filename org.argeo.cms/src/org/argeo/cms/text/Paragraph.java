package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Paragraph extends StyledComposite {
	private static final long serialVersionUID = 3746457776229542887L;

	public Paragraph(Section parent, int style, Node node) {
		super(parent, style, parent.getViewer().getTextInterpreter());
		setLayout(CmsUtils.noSpaceGridLayout());
		setData(node);
	}

	public void refresh(Boolean updateContent) throws RepositoryException {
		clear(true);
		createLabel(TextStyles.TEXT_DEFAULT);
		if (updateContent)
			updateContent();
	}

	public void updateContent() throws RepositoryException {
		Node node = getNode();

		String style;
		if (node.hasProperty(CMS_STYLE))
			style = node.getProperty(CMS_STYLE).getString();
		else
			style = TextStyles.TEXT_DEFAULT;
		setStyle(style);

		// retrieve control AFTER setting style, since it may have been reset
		setText(node);

		getViewer().layout(this);
	}

	protected Node getNode() {
		return (Node) getData();
	}

	protected Section getSection() {
		return (Section) getParent();
	}

	protected TextViewer3 getViewer() {
		return getSection().getViewer();
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
}
