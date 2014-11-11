package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Section extends Composite implements CmsNames {
	private static final long serialVersionUID = -5933796173755739207L;

	private final Section parentSection;
	private Composite sectionHeader;
	private final Integer relativeDepth;

	public Section(Composite parent, int style, Node node)
			throws RepositoryException {
		super(parent, style);
		setData(node);
		parentSection = findSection(parent);
		if (parentSection != null)
			relativeDepth = getNode().getDepth()
					- parentSection.getNode().getDepth();
		else
			relativeDepth = 0;

		setLayout(CmsUtils.noSpaceGridLayout());
		CmsUtils.style(this, TextStyles.TEXT_SECTION);
	}

	public void createHeader() {
		if (sectionHeader != null)
			throw new CmsException("Seciton header was already created");

		sectionHeader = new Composite(this, SWT.NONE);
		sectionHeader.setLayoutData(CmsUtils.fillWidth());
		sectionHeader.setLayout(CmsUtils.noSpaceGridLayout());
	}

	public Composite getHeader() {
		return sectionHeader;
	}

	Paragraph getParagraph(Node node) throws RepositoryException {
		for (Control child : getChildren()) {
			if (child instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) child;
				Node currNode = (Node) CmsUtils.getDataItem(paragraph,
						getNode());
				if (currNode.getIdentifier().equals(node.getIdentifier()))
					return paragraph;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		if (parentSection == null)
			return "Main section " + getData();
		return "Section " + getData();
	}

	public Node getNode() {
		return (Node) getData();
	}

	public Section getParentSection() {
		return parentSection;
	}

	public Integer getRelativeDepth() {
		return relativeDepth;
	}

	/** Recursively finds the related section in the parents (can be itself) */
	public static Section findSection(Control control) {
		if (control == null)
			return null;
		if (control instanceof Section)
			return (Section) control;
		else
			return findSection(control.getParent());
	}
}
