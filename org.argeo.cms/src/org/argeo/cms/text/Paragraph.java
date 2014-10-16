package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsUtils;

public class Paragraph extends StyledComposite {
	private static final long serialVersionUID = 3746457776229542887L;

	private TextInterpreter textInterpreter = new IdentityTextInterpreter();

	public Paragraph(Section parent, int style, Node node) {
		super(parent, style);
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
}
