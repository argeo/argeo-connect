package org.argeo.cms.text;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Section extends Composite implements CmsNames {
	private static final long serialVersionUID = -5933796173755739207L;

	private final TextViewer textViewer;
	private final Section parentSection;

	private SectionTitle sectionTitle;

	// public Section(TextViewer textViewer, int style, Node node)
	// throws RepositoryException {
	// this(textViewer, (Composite) textViewer.getControl(), style, node);
	// }

	public Section(Composite parent, int style, Node node)
			throws RepositoryException {
		this(((Section) findParentData(parent, node)).getViewer(), parent,
				style, node);
	}

	private static Composite findParentData(Control parent, Node node) {
		if (parent instanceof Composite && parent.getData() != null) {
			Node parentNode = (Node) parent.getData();
			if (node != null) {
				try {
					String parentPath = parentNode.getPath();
					if (!parentPath.equals(node.getParent().getPath()))
						throw new CmsException("Parent " + parentPath
								+ " not compatible ");
				} catch (RepositoryException e) {
					throw new CmsException("Cannot check parent data", e);
				}
			}
			return (Composite) parent;
		}
		if (parent.getParent() != null)
			return findParentData(parent.getParent(), node);
		else
			throw new CmsException("No data parent found for " + node);
	}

	Section(TextViewer textViewer, Composite parent, int style, Node node)
			throws RepositoryException {
		super(parent, style);
		this.textViewer = textViewer;
		if (parent instanceof Section)
			this.parentSection = (Section) parent;
		else
			this.parentSection = null;
		setLayout(CmsUtils.noSpaceGridLayout());
		setData(node);
		CmsUtils.style(this, TextStyles.TEXT_SECTION);
	}

	public void updateContent() throws RepositoryException {
		// Node node = (Node) getData();
		// if (node.hasProperty(Property.JCR_TITLE)) {
		// title.setText(node.getProperty(Property.JCR_TITLE));
		// }
		for (Control child : getChildren()) {
			if (child instanceof Paragraph)
				((Paragraph) child).updateContent();
			if (child instanceof SectionTitle)
				((SectionTitle) child).updateContent();

		}
	}

	public void refresh(Boolean updateContent, Boolean deep)
			throws RepositoryException {
		Node node = (Node) getData();
		for (Control child : getChildren())
			child.dispose();

		if (getNode().hasProperty(Property.JCR_TITLE)) {
			int relativeDepth = getNode().getDepth() - getTextNode().getDepth();
			Property title = getNode().getProperty(Property.JCR_TITLE);
			sectionTitle = new SectionTitle(this, SWT.NONE, title,
					relativeDepth);
			sectionTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			sectionTitle.refresh(updateContent);
			if (getViewer().getCmsEditable().isEditing())
				sectionTitle.getTitle().setMouseListener(
						(MouseListener) getViewer());
		}

		for (NodeIterator ni = node.getNodes(); ni.hasNext();) {
			Node child = ni.nextNode();
			if (child.isNodeType(CmsTypes.CMS_SECTION)) {
				Section composite = new Section(this, SWT.NONE, child);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false));
				if (deep)
					composite.refresh(deep, updateContent);
				if (updateContent)
					composite.updateContent();
			} else if (child.isNodeType(CmsTypes.CMS_STYLED)) {
				Paragraph paragraph = new Paragraph(this, SWT.NONE, child);
				paragraph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false));
				paragraph.refresh(updateContent);
				if (getViewer().getCmsEditable().isEditing())
					paragraph.setMouseListener((MouseListener) getViewer());
			}
		}

		// if (updateContent)
		// updateContent();

		getViewer().layout(this);
	}

	protected Composite getChild(Item node) throws RepositoryException {
		for (Control child : getChildren()) {
			if (child instanceof Composite && child.getData() != null) {
				Item currNode = (Item) child.getData();
				if (currNode.getPath().equals(node.getPath()))
					return (Composite) child;
			}
		}
		return null;
	}

	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// }
	//
	// @Override
	// public void mouseDown(MouseEvent e) {
	// if (e.button == 1) {
	// Control source = (Control) e.getSource();
	// Composite composite = findParentData(source, null);
	// Point point = new Point(e.x, e.y);
	// getViewer().edit(composite, source.toDisplay(point));
	// } else if (e.button == 3) {
	// Composite composite = findParentData((Control) e.getSource(), null);
	// if (getViewer() instanceof VersatileTextViewer)
	// ((VersatileTextViewer) getViewer()).getStyledTools().show(
	// composite, new Point(e.x, e.y));
	// }
	// }
	//
	// @Override
	// public void mouseUp(MouseEvent e) {
	// }

	// protected Composite findDataParent(Object source) {
	// Control control = (Control) source;
	// if (control.getData() != null)
	// return (Composite) control;
	// return findDataParent(control.getParent());
	//
	// }

	@Override
	public String toString() {
		if (parentSection == null)
			return "Main section " + getData();
		return "Section " + getData();
	}

	private TextViewer getViewer() {
		return textViewer;
	}

	public Node getNode() {
		return (Node) getData();
	}

	private Node getTextNode() {
		return getViewer().getMainSection().getNode();
	}

	public Section getParentSection() {
		return parentSection;
	}

	public TextInterpreter getTextInterpreter() {
		return getViewer().getTextInterpreter();
	}
}
