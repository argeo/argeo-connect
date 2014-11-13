package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.viewers.Section;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/** Text editor where sections and subsections can be managed by the user. */
public class ComplexTextEditor extends FlatTextEditor {
	private static final long serialVersionUID = 6049661610883342325L;

	private StyledTools styledTools;

	public ComplexTextEditor(Composite parent, Node textNode,
			CmsEditable cmsEditable) throws RepositoryException {
		super(parent, textNode, cmsEditable);
		if (getCmsEditable().canEdit())
			styledTools = new StyledTools(this, parent.getDisplay());
	}

	@Override
	protected void initModel() throws RepositoryException {
		mainSection.getNode().setProperty(Property.JCR_TITLE,
				mainSection.getNode());
	}

	protected void refresh(TextSection section) throws RepositoryException {
		super.refresh(section);

		for (NodeIterator ni = section.getNode().getNodes(CMS_H); ni.hasNext();) {
			Node child = ni.nextNode();
			if (child.isNodeType(CmsTypes.CMS_SECTION)) {
				TextSection newSection = new TextSection(section, SWT.NONE,
						child);
				newSection.setLayoutData(CmsUtils.fillWidth());
				refresh(newSection);
			}
		}

	}

	@Override
	public StyledTools getStyledTools() {
		return styledTools;
	}

	public void deepen() {
		checkEdited();
		try {
			if (getEdited() instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) getEdited();
				Text text = (Text) paragraph.getControl();
				String txt = text.getText();
				Node paragraphNode = paragraph.getNode();
				Section section = paragraph.getSection();
				Node sectionNode = section.getNode();
				Node newSectionNode = sectionNode.addNode(CMS_H,
						CmsTypes.CMS_SECTION);
				int paragraphIndex = paragraphNode.getIndex();
				sectionNode.orderBefore(h(newSectionNode.getIndex()),
						p(paragraphIndex));
				String sectionPath = sectionNode.getPath();
				String newSectionPath = newSectionNode.getPath();
				for (int i = 1; sectionNode.hasNode(p(i + paragraphIndex)); i++) {
					sectionNode.getSession().move(
							sectionPath + p(i + paragraphIndex),
							newSectionPath + p(i));

				}
				// create property
				newSectionNode.setProperty(Property.JCR_TITLE, "");
				getTextInterpreter().write(
						newSectionNode.getProperty(Property.JCR_TITLE), txt);

				paragraphNode.remove();
				layout(section);
			} else if (getEdited() instanceof SectionTitle) {
				SectionTitle sectionTitle = (SectionTitle) getEdited();
				Section section = sectionTitle.getSection();
				Section parentSection = section.getParentSection();
				if (parentSection == null)
					return;// cannot deepen main section
				Node sectionNode = section.getNode();
				Node parentSectionNode = parentSection.getNode();
				if (sectionNode.getIndex() == 1)
					return;// cannot deepen first section
				Node previousNode = parentSectionNode.getNode(h(sectionNode
						.getIndex() - 1));
				previousNode.getSession().move(sectionNode.getPath(),
						previousNode.getPath() + "/" + CMS_H);
				previousNode.getSession().save();

			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot deepen " + getEdited(), e);
		}
	}

	public void undeepen() {
		checkEdited();
		try {
			if (getEdited() instanceof Paragraph) {

			} else if (getEdited() instanceof SectionTitle) {
				SectionTitle sectionTitle = (SectionTitle) getEdited();
				Section section = sectionTitle.getSection();
				Node sectionNode = section.getNode();
				Section parentSection = section.getParentSection();
				if (parentSection == null)
					return;// cannot undeepen main section
				Node parentSectionNode = parentSection.getNode();
				Section parentParentSection = parentSection.getParentSection();
				if (parentParentSection == null) {// first level
					Node newParagrapheNode = parentSectionNode.addNode(CMS_P,
							CmsTypes.CMS_STYLED);
					parentSectionNode.orderBefore(
							p(newParagrapheNode.getIndex()),
							h(sectionNode.getIndex()));
					String txt = getTextInterpreter().read(
							sectionNode.getProperty(Property.JCR_TITLE));
					section.getNode().remove();
					getTextInterpreter().write(newParagrapheNode, txt);

				} else {
					Node parentParentSectionNode = parentParentSection
							.getNode();
					parentParentSectionNode.getSession().move(
							sectionNode.getPath(),
							parentParentSectionNode.getPath() + "/" + CMS_H);
					NodeIterator sections = parentParentSectionNode
							.getNodes(CMS_H);

					// move it behind its former parent
					Node movedNode = null;
					while (sections.hasNext()) {
						movedNode = sections.nextNode();
					}
					parentParentSectionNode.orderBefore(
							h(movedNode.getIndex()),
							h(parentSectionNode.getIndex() + 1));

					parentParentSectionNode.getSession().save();
				}
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot undeepen " + getEdited(), e);
		}
	}

	protected String h(Integer index) {
		StringBuilder sb = new StringBuilder(5);
		sb.append(CMS_H).append('[').append(index).append(']');
		return sb.toString();
	}

}
