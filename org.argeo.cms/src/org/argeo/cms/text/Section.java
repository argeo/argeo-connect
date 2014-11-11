package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Section extends Composite implements CmsNames {
	// private final static String SECTION_INDEX_DATAKEY = "SectionIndex";
	private static final long serialVersionUID = -5933796173755739207L;

	private final Section parentSection;

	private Composite sectionHeader;

	private final Integer relativeDepth;

	// public Section(Composite parent, int style, Node node)
	// throws RepositoryException {
	// this(((Section) findParentData(parent, node)).getViewer(), parent,
	// style, node);
	// }

	public Section(Composite parent, int style, Node node)
			throws RepositoryException {
		super(parent, style);
		// this.textViewer = textViewer;
		setData(node);
		parentSection = findSection(parent);
		// if (parent instanceof Section)
		// this.parentSection = (Section) parent;
		// else
		// this.parentSection = null;

		// Node baseNode = section.getParentSection() == null ?
		// section.getNode()
		// : section.getViewer().getMainSection().getNode();
		if (parentSection != null)
			relativeDepth = getNode().getDepth()
					- parentSection.getNode().getDepth();
		else
			relativeDepth = 0;

		setLayout(CmsUtils.noSpaceGridLayout());
		CmsUtils.style(this, TextStyles.TEXT_SECTION);

		// sectionHeader.refresh(true);
		// refresh();
	}

	// protected void refresh() throws RepositoryException {
	// CmsUtils.clear(this);
	//
	// Node node = getNode();
	// if (hasHeader()) {
	// sectionHeader = new Composite(this, SWT.NONE);
	// sectionHeader.setLayoutData(CmsUtils.fillWidth());
	// sectionHeader.setLayout(CmsUtils.noSpaceGridLayout());
	//
	// boolean hasProperty = getNode().hasProperty(Property.JCR_TITLE);
	// if (hasProperty) {
	// SectionTitle title = new SectionTitle(sectionHeader, SWT.NONE,
	// getTextInterpreter(), textViewer.getMouseListener());
	// title.setData(getNode().getProperty(Property.JCR_TITLE));
	// title.setLayoutData(CmsUtils.fillWidth());
	// title.updateContent();
	// }
	// }
	//
	// for (NodeIterator ni = node.getNodes(CMS_P); ni.hasNext();) {
	// Node child = ni.nextNode();
	// if (child.isNodeType(CmsTypes.CMS_STYLED)) {
	// Paragraph paragraph = new Paragraph(this, SWT.NONE, child,
	// textViewer.getMouseListener());
	// paragraph.setLayoutData(CmsUtils.fillWidth());
	// // paragraph.refresh(updateContent);
	// // if (getViewer().getCmsEditable().canEdit())
	// // paragraph.setMouseListener( getViewer().getMouseListener());
	// }
	// }
	//
	// for (NodeIterator ni = node.getNodes(CMS_H); ni.hasNext();) {
	// Node child = ni.nextNode();
	// if (child.isNodeType(CmsTypes.CMS_SECTION)) {
	// Section composite = new Section(this, SWT.NONE, child);
	// composite.setLayoutData(CmsUtils.fillWidth());
	// // if (deep)
	// // composite.refresh(deep, updateContent);
	// // if (updateContent)
	// // composite.updateContent();
	// }
	// }
	//
	// }

	// protected Boolean hasHeader() throws RepositoryException {
	// return getNode().hasProperty(Property.JCR_TITLE);
	// }

	// public void updateContent() throws RepositoryException {
	// // Node node = (Node) getData();
	// // if (node.hasProperty(Property.JCR_TITLE)) {
	// // title.setText(node.getProperty(Property.JCR_TITLE));
	// // }
	// for (Control child : getChildren()) {
	// if (child instanceof Paragraph)
	// ((Paragraph) child).updateContent();
	// if (child instanceof SectionHeader)
	// ((SectionHeader) child).updateContent();
	// if (child instanceof Section)
	// ((Section) child).updateContent();
	//
	// }
	// }

	// public void refresh(Boolean updateContent, Boolean deep)
	// throws RepositoryException {
	// Node node = (Node) getData();
	// for (Control child : getChildren())
	// child.dispose();
	//
	// // if (sectionHeader == null) {
	// // sectionHeader = new SectionHeader(this, SWT.NONE);
	// // sectionHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
	// // false));
	// // sectionHeader.refresh(updateContent);
	// // }
	// // if (getViewer().getCmsEditable().isEditing())
	// // sectionHeader.addMouseListener((MouseListener) getViewer());
	//
	// // LinkedHashMap<String, Node> pNodes = new LinkedHashMap<String,
	// // Node>();
	// // for (NodeIterator ni = node.getNodes(CMS_P); ni.hasNext();) {
	// // Node n = ni.nextNode();
	// // pNodes.put(n.getPath(), n);
	// // }
	// //
	// // LinkedHashMap<String, Composite> pComposites = new
	// // LinkedHashMap<String, Composite>();
	// // for (Control child : getChildren()) {
	// // String path = CmsUtils.getDataPath(child);
	// // if (path != null)
	// // pComposites.put(path, (Composite) child);
	// // }
	//
	// for (NodeIterator ni = node.getNodes(CMS_P); ni.hasNext();) {
	// Node child = ni.nextNode();
	// if (child.isNodeType(CmsTypes.CMS_STYLED)) {
	// Paragraph paragraph = new Paragraph(this, SWT.NONE, child);
	// paragraph.setLayoutData(CmsUtils.FILL_WIDTH);
	// // paragraph.refresh(updateContent);
	// if (getViewer().getCmsEditable().isEditing())
	// paragraph.setMouseListener((MouseListener) getViewer());
	// }
	// }
	//
	// for (NodeIterator ni = node.getNodes(CMS_H); ni.hasNext();) {
	// Node child = ni.nextNode();
	// if (child.isNodeType(CmsTypes.CMS_SECTION)) {
	// Section composite = new Section(this, SWT.NONE, child);
	// composite.setLayoutData(CmsUtils.FILL_WIDTH);
	// if (deep)
	// composite.refresh(deep, updateContent);
	// if (updateContent)
	// composite.updateContent();
	// }
	// }
	//
	// // if (updateContent)
	// // updateContent();
	//
	// // getViewer().layout(this);
	// }

	public Control getBeforeFirst() {
		return sectionHeader;
	}

	// protected Composite getChild(Item node) throws RepositoryException {
	// for (Control child : getChildren()) {
	// if (child instanceof Composite && child.getData() != null) {
	// Item currNode = (Item) child.getData();
	// if (currNode.getPath().equals(node.getPath()))
	// return (Composite) child;
	// }
	// }
	// return null;
	// }

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

	// private TextViewer getViewer() {
	// return textViewer;
	// }

	public Node getNode() {
		return (Node) getData();
	}

	// private Node getTextNode() {
	// return getViewer().getMainSection().getNode();
	// }

	public Section getParentSection() {
		return parentSection;
	}

	// public TextInterpreter getTextInterpreter() {
	// return getViewer().getTextInterpreter();
	// }

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

	// class SectionHeader extends Composite {
	// private static final long serialVersionUID = -1787983154946583171L;
	//
	// private StyledComposite title;
	//
	// public SectionHeader(Section parent, int style)
	// throws RepositoryException {
	// super(parent, style);
	// setLayout(CmsUtils.noSpaceGridLayout());
	// refresh(true);
	// }
	//
	// private void refresh(Boolean updateContent) throws RepositoryException {
	// // for (Control child : getChildren())
	// // child.dispose();
	// boolean hasProperty = getNode().hasProperty(Property.JCR_TITLE);
	// if (title == null) {
	// if (hasProperty) {
	// title = new SectionTitle(this, SWT.NONE,
	// getTextInterpreter());
	// title.setData(getNode().getProperty(Property.JCR_TITLE));
	// title.setLayoutData(CmsUtils.fillWidth());
	// updateContent = true;
	// }
	// } else {
	// if (!hasProperty) {
	// title.dispose();
	// title = null;
	// }
	// }
	//
	// if (updateContent)
	// updateContent();
	// pack();
	// }
	//
	// public void updateContent() throws RepositoryException {
	// if (title != null)
	// title.updateContent();
	// }
	//
	// // public StyledComposite getTitle() {
	// // return title;
	// // }
	//
	// // private class SectionTitle extends StyledComposite {
	// // private static final long serialVersionUID = -238366897770187994L;
	// //
	// // public SectionTitle(Composite parent, int swtStyle,
	// // TextInterpreter textInterpreter) {
	// // super(parent, swtStyle, textInterpreter);
	// // }
	// //
	// // public void updateContent() throws RepositoryException {
	// // if (relativeDepth == 0)
	// // setStyle(TextStyles.TEXT_TITLE);
	// // else
	// // setStyle(TextStyles.TEXT_H + relativeDepth);
	// //
	// // // retrieve control AFTER setting style, since
	// // // it may have been
	// // // reset
	// // setText(getProperty());
	// //
	// // if (getViewer().getCmsEditable().isEditing())
	// // setMouseListener((MouseListener) getViewer());
	// // layout();
	// // }
	// // }
	//
	// }

}
