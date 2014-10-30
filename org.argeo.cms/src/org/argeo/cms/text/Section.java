package org.argeo.cms.text;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsStyles;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class Section extends Composite implements CmsNames, TraverseListener,
		MouseListener {
	private static final long serialVersionUID = -5933796173755739207L;

	private final TextViewer textViewer;
	private final Section parentSection;

	private SectionTitle sectionTitle;

	public Section(TextViewer textViewer, int style, Node node)
			throws RepositoryException {
		this(textViewer, (Composite) textViewer.getControl(), style, node);
	}

	public Section(Section parent, int style, Node node)
			throws RepositoryException {
		this(parent.textViewer, parent, style, node);
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
			if (getViewer().getCmsEditable().canEdit()) {
				sectionTitle.getTitle().setMouseListener(this);
				sectionTitle.getTitle().setTraverseListener(this);
			}
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
				if (getViewer().getCmsEditable().canEdit()) {
					paragraph.setMouseListener(this);
					paragraph.setTraverseListener(this);
				}
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

	// protected void updateParagraph(StyledComposite paragraph)
	// throws RepositoryException {
	// // if (!node.getPath().equals(paragraph.getNodePath()))
	// // throw new CmsException("Trying to update paragraph "
	// // + paragraph.getNodePath() + " with " + node);
	// Node node = (Node) paragraph.getData();
	// String style;
	// if (node.hasProperty(CMS_STYLE))
	// style = node.getProperty(CMS_STYLE).getString();
	// else
	// style = TextStyles.TEXT_DEFAULT;
	// paragraph.setStyle(style);
	//
	// // retrieve control AFTER setting style, since it may have been reset
	// Control control = paragraph.getControl();
	// if (control instanceof Label) {
	// String content = textInterpreter.raw(node);
	// Label label = (Label) control;
	// label.setText(content);
	// if (textPartListener != null) {
	// label.removeMouseListener(textPartListener);
	// label.addMouseListener(textPartListener);
	// }
	// } else if (control instanceof Text) {
	// String content = textInterpreter.read(node);
	// Text text = (Text) control;
	// text.setText(content);
	// text.addTraverseListener(this);
	// }
	//
	// }

	@Override
	public void keyTraversed(TraverseEvent e) {
		System.out.println(e);
		// Composite composite = findDataParent(e.getSource());
		// if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
		// Control[] children = getChildren();
		// for (int i = 0; i < children.length; i++) {
		// if (composite == children[i] && i + 1 < children.length) {
		// Composite nextEdited = (Composite) children[i + 1];
		// System.out.println(nextEdited.isVisible());
		// getViewer().edit(nextEdited);
		// break;
		// }
		// }
		// } else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
		// Control[] children = getChildren();
		// for (int i = 0; i < children.length; i++) {
		// if (composite == children[i] && i != 0) {
		// getViewer().edit((Composite) children[i - 1]);
		// break;
		// }
		// }
		// } else if (e.detail == SWT.TRAVERSE_ESCAPE) {
		// getViewer().cancelEdit();
		// } else if (e.detail == SWT.TRAVERSE_RETURN) {
		// getViewer().splitEdit();
		// }
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}

	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button == 1) {
			Control source = (Control) e.getSource();
			Composite composite = findDataParent(source);
			Point point = new Point(e.x, e.y);
			getViewer().edit(composite, source.toDisplay(point));
		} else if (e.button == 3) {
			Composite composite = findDataParent(e.getSource());
			getViewer().getStyledTools().show(composite, new Point(e.x, e.y));
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}

	protected Composite findDataParent(Object source) {
		Control control = (Control) source;
		if (control.getData() != null)
			return (Composite) control;
		return findDataParent(control.getParent());

	}

	@Override
	public String toString() {
		if (parentSection == null)
			return "Main section " + getData();
		return "Section " + getData();
	}

	protected TextViewer getViewer() {
		return textViewer;
	}

	protected Node getNode() {
		return (Node) getData();
	}

	protected Node getTextNode() {
		return getViewer().getMainSection().getNode();
	}

	public Section getParentSection() {
		return parentSection;
	}
}
