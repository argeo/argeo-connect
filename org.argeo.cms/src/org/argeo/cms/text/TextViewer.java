package org.argeo.cms.text;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.ScrolledPage;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class TextViewer extends StructuredViewer implements CmsNames {
	private static final long serialVersionUID = 6536978175844375304L;

	private final ScrolledText page;

	private StyledTools styledTools;
	private TextPartListener textPartListener;
	private TextTraverseListener textTraverseListener;
	private EditableTextPart edited;
	// private Text addText;
	private TextInterpreter textInterpreter = new IdentityTextInterpreter();

	private CmsEditable cmsEditable = CmsEditable.NON_EDITABLE;

	public TextViewer(Composite parent, int style) {
		this.page = new ScrolledText(parent, style);
		page.setLayout(CmsUtils.noSpaceGridLayout());
	}

	protected Node getTextNode() {
		return (Node) getInput();
	}

	protected Session getSession() {
		try {
			return getTextNode().getSession();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get session from " + getTextNode(),
					e);
		}
	}

	/*
	 * STRUCTURED CONTROLLER IMPLEMENTATION
	 */

	@Override
	protected Widget doFindInputItem(Object element) {
		if (element == null || element.equals(getInput()))
			return page;
		else
			return null;
	}

	@Override
	protected Widget doFindItem(Object element) {
		return page.find((Item) element);
	}

	@Override
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		try {
			Node node = (Node) element;
			if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				updateParagraph((EditableTextPart) item, node);
			}
		} catch (Exception e) {
			throw new CmsException("Cannot update " + element, e);
		}
	}

	@Override
	protected void internalRefresh(Object element) {
		internalRefresh(element, true);
	}

	@Override
	protected void internalRefresh(Object element, boolean updateLabels) {
		try {
			Widget item = findItem(element);
			if (item == null || !(item instanceof Composite))
				throw new CmsException("No composite found for " + element);
			Composite composite = (Composite) item;
			Node node = element != null ? (Node) element : getTextNode();

			int depth = node.getDepth() - getTextNode().getDepth();
			if (node.isNodeType(CmsTypes.CMS_SECTION)) {
				// clear
				for (Control child : composite.getChildren())
					child.dispose();
				refreshSection(composite, node, depth);
				composite.layout(true, true);
			} else if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				updateParagraph((EditableTextPart) composite, node);
				composite.getParent().layout(true, true);
			} else {
				throw new CmsException("Unsupported node " + node);
			}

		} catch (RepositoryException e) {
			throw new CmsException("Cannot refresh", e);
		}

		// TODO optimize
		// page.layout(true, true);
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		try {
			if (styledTools != null) {
				styledTools.dispose();
				styledTools = null;
			}
			textPartListener = null;

			Node node = (Node) input;
			page.setData(Property.JCR_PATH, node.getPath());
			if (cmsEditable.canEdit()) {
				textPartListener = new TextPartListener();
				textTraverseListener = new TextTraverseListener();
				styledTools = new StyledTools(this);
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot set root element", e);
		}
	}

	@Override
	public void reveal(Object element) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void setSelectionToWidget(List l, boolean reveal) {
		if (l.size() > 0)
			page.select((Node) l.get(0));
	}

	@Override
	protected List<?> getSelectionFromWidget() {
		ArrayList<Node> res = new ArrayList<Node>();
		EditableTextPart etp = page.getSelected();
		if (etp == null)
			return res;
		try {
			Node node = getSession().getNode(etp.getNodePath());
			res.add(node);
			return res;
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get selection from "
					+ etp.getNodePath(), e);
		}
	}

	@Override
	public Control getControl() {
		return page;
	}

	public ScrolledPage getPage() {
		return (ScrolledPage) getControl();
	}

	/*
	 * MODEL TO VIEW
	 */

	protected void refreshSection(Composite sectionComposite, Node sectionNode,
			int depth) throws RepositoryException {
		// title
		if (sectionNode.hasProperty(Property.JCR_TITLE)) {
			String title = sectionNode.getProperty(Property.JCR_TITLE)
					.getString();
			SectionTitle sectionTitle = new SectionTitle(sectionComposite,
					SWT.NONE, title, depth);
			sectionTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
		}

		for (NodeIterator ni = sectionNode.getNodes(); ni.hasNext();) {
			Node child = ni.nextNode();
			if (child.isNodeType(CmsTypes.CMS_SECTION)) {
				Composite composite = new Composite(sectionComposite, SWT.NONE);
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false));
				composite.setData(Property.JCR_PATH, child.getPath());
				composite.setLayout(CmsUtils.noSpaceGridLayout());
				refreshSection(composite, child, depth + 1);
			} else if (child.isNodeType(CmsTypes.CMS_STYLED)) {
				StyledComposite styledComposite = new StyledComposite(
						sectionComposite, SWT.NONE, child);
				styledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
						true, false));
				updateParagraph(styledComposite, child);
			}
		}
	}

	public void updateParagraph(EditableTextPart paragraph, Node node)
			throws RepositoryException {
		if (!node.getPath().equals(paragraph.getNodePath()))
			throw new CmsException("Trying to update paragraph "
					+ paragraph.getNodePath() + " with " + node);
		String style;
		if (node.hasProperty(CMS_STYLE))
			style = node.getProperty(CMS_STYLE).getString();
		else
			style = TextStyles.TEXT_DEFAULT;
		paragraph.setStyle(style);

		// retrieve control AFTER setting style, since it may have been reset
		Control control = paragraph.getControl();
		if (control instanceof Label) {
			String content = textInterpreter.raw(node);
			Label label = (Label) control;
			label.setText(content);
			if (textPartListener != null)
				label.addMouseListener(textPartListener);
		} else if (control instanceof Text) {
			String content = textInterpreter.read(node);
			Text text = (Text) control;
			text.setText(content);
			if (textTraverseListener != null)
				text.addTraverseListener(textTraverseListener);
		}
		// paragraph.layout(true, true);
	}

	//
	// TEXT CONTROLLER IMPLEMENTATION
	//
	public void newParagraph(String initialText) {
		// TODO Auto-generated method stub

	}

	public void setParagraphStyle(EditableTextPart etp, String style) {
		try {
			Node paragraphNode = getSession().getNode(etp.getNodePath());
			paragraphNode.setProperty(CMS_STYLE, style);
			paragraphNode.getSession().save();
			refresh(paragraphNode);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot set style " + style + " on " + etp,
					e1);
		}
	}

	public void deleteParagraph(EditableTextPart etp) {
		try {
			Node paragraphNode = getSession().getNode(etp.getNodePath());
			Node parentNode = paragraphNode.getParent();
			Session session = paragraphNode.getSession();
			paragraphNode.remove();
			session.save();
			etp.dispose();
			refresh(parentNode);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot delete " + etp, e1);
		}
	}

	public String getRawParagraphText(EditableTextPart etp) {
		try {
			Node paragraphNode = getSession().getNode(etp.getNodePath());
			return textInterpreter.raw(paragraphNode);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot delete " + etp, e1);
		}
	}

	private EditableTextPart getEtp(Object mouseEventSource) {
		// TODO make it more robust
		return (EditableTextPart) ((Control) mouseEventSource).getParent()
				.getParent().getParent();
	}

	protected void startEditing(EditableTextPart etp) {
		try {
			Node node = getSession().getNode(etp.getNodePath());
			etp.startEditing();
			updateParagraph(etp, node);
			etp.layout();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot start editing", e);
		}
	}

	protected void stopEditing(EditableTextPart etp) {
		try {
			Node node = getSession().getNode(etp.getNodePath());
			String content = ((Text) etp.getControl()).getText();
			textInterpreter.write(node, content);
			etp.stopEditing();
			updateParagraph(etp, node);
			etp.layout();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot start editing", e);
		}
	}

	public void setTextInterpreter(TextInterpreter textInterpreter) {
		this.textInterpreter = textInterpreter;
	}

	public void setCmsEditable(CmsEditable cmsEditable) {
		this.cmsEditable = cmsEditable;
	}

	public CmsEditable getCmsEditable() {
		return cmsEditable;
	}

	private class TextPartListener implements MouseListener {
		private static final long serialVersionUID = 4221123959609321884L;

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			if (e.button == 1) {
				EditableTextPart etp = getEtp(e.getSource());
				page.select(etp);
				if (cmsEditable.isEditing()) {
					if (edited != null)
						stopEditing(edited);

					edited = etp;
					startEditing(edited);

					page.layout(true, true);
				}
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {
			EditableTextPart etp = getEtp(e.getSource());
			if (e.button == 1) {
				page.select(etp);
				if (cmsEditable.isEditing()) {
					if (edited != null && etp != edited) {
						stopEditing(edited);
						edited = null;
						page.layout(true, true);
					}
				}
			} else if (e.button == 3) {
				styledTools.show(etp, new Point(e.x, e.y));
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}

	}

	// private class TextKeyListener implements KeyListener {
	// private static final long serialVersionUID = -7720848595910906899L;
	//
	// @Override
	// public void keyPressed(KeyEvent e) {
	// // if (log.isDebugEnabled())
	// // log.debug(e.character);
	// if (e.character == '\r') {
	// String text = ((Text) e.getSource()).getText();
	// EditableTextPart etp = getEtp(e.getSource());
	// String nodePath = etp.getNodePath();
	// try {
	// Node sectionNode = getTextNode().getSession()
	// .getNode(nodePath).getParent();
	// Display.getCurrent().asyncExec(
	// new AddContentAction(sectionNode, text));
	// } catch (RepositoryException e1) {
	// throw new CmsException("Cannot process " + e + " on node "
	// + nodePath, e1);
	// }
	// }
	//
	// }
	//
	// @Override
	// public void keyReleased(KeyEvent e) {
	// // if (log.isDebugEnabled())
	// // log.debug(e.time + " " + e);
	// }
	//
	// }

	private class AddContentAction implements Runnable {
		private final Node sectionNode;
		private final String text;

		public AddContentAction(Node sectionNode, String text) {
			this.sectionNode = sectionNode;
			this.text = text;
		}

		@Override
		public void run() {
			try {
				@SuppressWarnings("unchecked")
				List<String> lines = IOUtils.readLines(new StringReader(text));
				if (lines.size() == 1) {// single line
					String line = lines.get(0).trim();
					char lastC = line.charAt(line.length() - 1);
					if (Character.isLetterOrDigit(lastC)) {
						Node section = sectionNode.addNode(CMS_H,
								CmsTypes.CMS_SECTION);
						// section.addMixin(CmsTypes.CMS_SECTION);
						section.setProperty(Property.JCR_TITLE, line);
					} else {
						addParagraph(line);
					}
				} else {
					for (String line : lines) {
						addParagraph(line);
					}
				}
				sectionNode.getSession().save();
				refresh();
				// paragraphs.addAll(lines);
			} catch (Exception e1) {
				throw new CmsException("Cannot read " + text, e1);
			}
		}

		private void addParagraph(String line) throws RepositoryException {
			Node paragraph = sectionNode.addNode(CMS_P, CmsTypes.CMS_STYLED);
			textInterpreter.write(paragraph, line);
		}

	}

	private class TextTraverseListener implements TraverseListener {
		private static final long serialVersionUID = -4937065908725938117L;

		public void keyTraversed(TraverseEvent e) {
			if (e.detail == SWT.TRAVERSE_RETURN) {
				String text = ((Text) e.getSource()).getText();
				EditableTextPart etp = getEtp(e.getSource());
				String nodePath = etp.getNodePath();
				try {
					Node sectionNode = getTextNode().getSession()
							.getNode(nodePath).getParent();
					Display.getCurrent().asyncExec(
							new AddContentAction(sectionNode, text));
				} catch (RepositoryException e1) {
					throw new CmsException("Cannot process " + e + " on node "
							+ nodePath, e1);
				}
			}
		}

	}
}
