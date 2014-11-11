package org.argeo.cms.text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
		this(parent, findSection(parent), style, node);
	}

	public Section(Section section, int style, Node node)
			throws RepositoryException {
		this(section, section, style, node);
	}

	private Section(Composite parent, Section parentSection, int style,
			Node node) throws RepositoryException {
		super(parent, style);
		setData(node);
		this.parentSection = parentSection;
		if (parentSection != null) {
			relativeDepth = getNode().getDepth()
					- parentSection.getNode().getDepth();
		} else {
			relativeDepth = 0;
		}
		setLayout(CmsUtils.noSpaceGridLayout());
		CmsUtils.style(this, TextStyles.TEXT_SECTION);
	}

	public Map<String, Section> getSubSections() throws RepositoryException {
		LinkedHashMap<String, Section> result = new LinkedHashMap<String, Section>();
		children: for (Control child : getChildren()) {
			if (child instanceof Composite) {
				if (child == sectionHeader || child instanceof EditableTextPart)
					continue children;
				collectDirectSubSections((Composite) child, result);
			}
		}
		return Collections.unmodifiableMap(result);
	}

	private void collectDirectSubSections(Composite composite,
			LinkedHashMap<String, Section> subSections)
			throws RepositoryException {
		if (composite instanceof Section) {
			Section section = (Section) composite;
			subSections.put(section.getNode().getIdentifier(), section);
			return;
		}
		for (Control child : composite.getChildren())
			if (child instanceof Composite)
				collectDirectSubSections((Composite) child, subSections);
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

	SectionPart getParagraph(String nodeId) {
		for (Control child : getChildren()) {
			if (child instanceof SectionPart) {
				SectionPart paragraph = (SectionPart) child;
				if (paragraph.getNodeId().equals(nodeId))
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
