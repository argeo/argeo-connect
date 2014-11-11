package org.argeo.cms.text;

import java.util.Observable;
import java.util.Observer;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.widgets.EditableText;
import org.argeo.cms.widgets.ScrolledPage;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/** Base class for all text viewers/editors. */
public abstract class AbstractTextViewer extends ContentViewer implements
		CmsNames, KeyListener, Observer {
	private static final long serialVersionUID = -2401274679492339668L;

	private final static Log log = LogFactory.getLog(AbstractTextViewer.class);

	protected final Section mainSection;
	/** The basis for the layouts, typically a ScrolledPage. */
	protected final Composite page;

	private EditableTextPart edited;

	private TextInterpreter textInterpreter = new IdentityTextInterpreter();
	private MouseListener mouseListener;
	private final CmsEditable cmsEditable;

	public AbstractTextViewer(Composite parent, Node textNode,
			CmsEditable cmsEditable) {
		try {
			// CMS Editable (before main section!)
			this.cmsEditable = cmsEditable == null ? CmsEditable.NON_EDITABLE
					: cmsEditable;
			if (this.cmsEditable instanceof Observable)
				((Observable) this.cmsEditable).addObserver(this);

			if (cmsEditable.canEdit())
				mouseListener = new ML();

			page = findPage(parent);
			mainSection = new Section(parent, SWT.NONE, textNode);
			mainSection.setLayoutData(CmsUtils.fillWidth());

			if (!textNode.hasProperty(Property.JCR_TITLE)
					&& !textNode.hasNodes())
				if (cmsEditable.canEdit())
					initModel();
			refresh();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot load main section", e);
		}
	}

	protected void refresh(Section section) throws RepositoryException {
		CmsUtils.clear(section);

		Node node = section.getNode();
		if (hasHeader(section)) {
			section.createHeader();
			if (node.hasProperty(Property.JCR_TITLE)) {
				SectionTitle title = newSectionTitle(section, node);
				title.setLayoutData(CmsUtils.fillWidth());
				updateContent(title);
			}
		}

		for (NodeIterator ni = node.getNodes(CMS_P); ni.hasNext();) {
			Node child = ni.nextNode();
			if (child.isNodeType(CmsTypes.CMS_STYLED)) {
				Paragraph paragraph = newParagraph(section, child);
				paragraph.setLayoutData(CmsUtils.fillWidth());
			}
		}
	}

	protected Boolean hasHeader(Section section) throws RepositoryException {
		return section.getNode().hasProperty(Property.JCR_TITLE);
	}

	/** Called if user can edit and model is not initialized */
	protected void initModel() throws RepositoryException {
		mainSection.getNode().addNode(CMS_P, CmsTypes.CMS_STYLED);
	}

	private Composite findPage(Composite composite) {
		if (composite instanceof ScrolledPage) {
			return (ScrolledPage) composite;
		} else {
			if (composite.getParent() == null)
				return composite;
			return findPage(composite.getParent());
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		refresh();
	}

	@Override
	public Control getControl() {
		return mainSection;
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void refresh() {
		try {
			if (cmsEditable.canEdit())
				mouseListener = new ML();
			refresh(mainSection);
			layout(mainSection);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot refresh", e);
		}
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
	}

	// CRUD
	protected Paragraph newParagraph(Section parent, Node node)
			throws RepositoryException {
		Paragraph paragraph = new Paragraph(parent, parent.getStyle(), node);
		updateContent(paragraph);
		paragraph.setMouseListener(mouseListener);
		return paragraph;
	}

	protected SectionTitle newSectionTitle(Section parent, Node node)
			throws RepositoryException {
		SectionTitle sectionTitle = new SectionTitle(parent.getHeader(),
				parent.getStyle());
		// sectionTitle.setData(node.getProperty(Property.JCR_TITLE));
		updateContent(sectionTitle);
		sectionTitle.setMouseListener(mouseListener);
		return sectionTitle;
	}

	protected void updateContent(EditableTextPart part)
			throws RepositoryException {
		// Item item = CmsUtils.getDataItem((Composite) part,
		// mainSection.getNode());
		if (part instanceof Paragraph) {
			Paragraph paragraph = (Paragraph) part;
			Node node = paragraph.getNode();
			String style = node.hasProperty(CMS_STYLE) ? node.getProperty(
					CMS_STYLE).getString() : TextStyles.TEXT_DEFAULT;
			paragraph.setStyle(style);
			// retrieve control AFTER setting style, since it may have been
			// reset
			setText(paragraph, node);
		} else if (part instanceof SectionTitle) {
			SectionTitle sectionTitle = (SectionTitle) part;
			Integer relativeDepth = sectionTitle.getSection()
					.getRelativeDepth();
			String style = relativeDepth == 0 ? TextStyles.TEXT_TITLE
					: TextStyles.TEXT_H + relativeDepth;
			sectionTitle.setStyle(style);
			// retrieve control AFTER setting style, since
			// it may have been reset
			setText(sectionTitle, sectionTitle.getProperty());
		}
	}

	protected void setText(EditableText textPart, Item item) {
		Control child = textPart.getControl();
		if (child instanceof Label)
			((Label) child).setText(textInterpreter.raw(item));
		else if (child instanceof Text)
			((Text) child).setText(textInterpreter.read(item));
	}

	// GENERIC EDITION
	public void edit(EditableTextPart composite, Object caretPosition) {
		try {
			if (edited == composite)
				return;

			if (edited != null && edited != composite)
				stopEditing(true);

			if (composite instanceof EditableTextPart) {
				EditableTextPart paragraph = (EditableTextPart) composite;
				paragraph.startEditing();
				updateContent(paragraph);
				prepare(paragraph, caretPosition);
				edited = composite;
				layout(paragraph.getControl());
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot edit " + composite, e);
		}
	}

	protected void save(EditableTextPart part) throws RepositoryException {
		if (part instanceof Paragraph) {
			textInterpreter.write(((Paragraph) part).getNode(),
					((Text) part.getControl()).getText());
		} else if (part instanceof SectionTitle) {
			textInterpreter.write(((SectionTitle) part).getProperty(),
					((Text) part.getControl()).getText());
		}
	}

	public void saveEdit() {
		try {
			if (edited != null)
				stopEditing(true);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
		}
	}

	public void cancelEdit() {
		try {
			if (edited != null)
				stopEditing(false);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
		}
	}

	protected void stopEditing(Boolean save) throws RepositoryException {
		if (edited instanceof Widget && ((Widget) edited).isDisposed()) {
			edited = null;
			return;
		}

		if (edited instanceof EditableTextPart) {
			EditableTextPart paragraph = (EditableTextPart) edited;
			if (save) {
				save(paragraph);
			}
			paragraph.stopEditing();
			updateContent(paragraph);
			layout(((EditableTextPart) edited).getControl());
			edited = null;
		}
	}

	protected void prepare(EditableTextPart st, Object caretPosition) {
		Control control = st.getControl();
		if (control instanceof Text) {
			Text text = (Text) control;
			if (caretPosition != null)
				if (caretPosition instanceof Integer)
					text.setSelection((Integer) caretPosition);
				else if (caretPosition instanceof Point) {
					// TODO find a way to position the caret at the right place
				}
			text.setData(RWT.ACTIVE_KEYS, new String[] { "BACKSPACE", "ESC",
					"TAB", "SHIFT+TAB", "ALT+ARROW_LEFT", "ALT+ARROW_RIGHT",
					"ALT+ARROW_UP", "ALT+ARROW_DOWN", "RETURN", "ENTER",
					"DELETE" });
			text.setData(RWT.CANCEL_KEYS, new String[] { "ALT+ARROW_LEFT",
					"ALT+ARROW_RIGHT" });
			text.addKeyListener(this);
		}
	}

	public void setParagraphStyle(Paragraph paragraph, String style) {
		try {
			Node paragraphNode = paragraph.getNode();
			paragraphNode.setProperty(CMS_STYLE, style);
			paragraphNode.getSession().save();
			updateContent(paragraph);
			layout(paragraph);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot set style " + style + " on "
					+ paragraph, e1);
		}
	}

	public void deleteParagraph(Paragraph paragraph) {
		try {
			Node paragraphNode = paragraph.getNode();
			Section section = paragraph.getSection();
			Session session = paragraphNode.getSession();
			paragraphNode.remove();
			session.save();
			paragraph.dispose();
			layout(section);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot delete " + paragraph, e1);
		}
	}

	// TEXT SPECIFIC EDITION
	public String getRawParagraphText(Paragraph paragraph) {
		try {
			return textInterpreter.raw(paragraph.getNode());
		} catch (RepositoryException e) {
			throw new CmsException("Cannot get raw paragraph text", e);
		}
	}

	public void splitEdit() {
		checkEdited();
		try {
			if (edited instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) edited;
				Text text = (Text) paragraph.getControl();
				int caretPosition = text.getCaretPosition();
				String txt = text.getText();
				String first = txt.substring(0, caretPosition);
				String second = txt.substring(caretPosition);
				Node firstNode = paragraph.getNode();
				Node sectionNode = firstNode.getParent();
				firstNode.setProperty(CMS_CONTENT, first);
				Node secondNode = sectionNode.addNode(CMS_P,
						CmsTypes.CMS_STYLED);
				// second node was create as last, if it is not the next one, it
				// means there are some in between and we can take the one at
				// index+1 for the re-order
				if (secondNode.getIndex() > firstNode.getIndex() + 1) {
					sectionNode.orderBefore(p(secondNode.getIndex()),
							p(firstNode.getIndex() + 1));
				}

				// if we die in between, at least we still have the whole text
				// in the first node
				textInterpreter.write(secondNode, second);
				textInterpreter.write(firstNode, first);

				Paragraph secondParagraph = paragraphSplitted(paragraph,
						secondNode);
				edit(secondParagraph, 0);
			} else if (edited instanceof SectionTitle) {
				SectionTitle sectionTitle = (SectionTitle) edited;
				Text text = (Text) sectionTitle.getControl();
				String txt = text.getText();
				int caretPosition = text.getCaretPosition();
				Section section = sectionTitle.getSection();
				Node sectionNode = section.getNode();
				Node paragraphNode = sectionNode.addNode(CMS_P,
						CmsTypes.CMS_STYLED);
				textInterpreter.write(paragraphNode,
						txt.substring(caretPosition));
				textInterpreter.write(
						sectionNode.getProperty(Property.JCR_TITLE),
						txt.substring(0, caretPosition));
				sectionNode.orderBefore(p(paragraphNode.getIndex()), p(1));
				sectionNode.getSession().save();

				Paragraph paragraph = sectionTitleSplitted(sectionTitle,
						paragraphNode);
				edit(paragraph, 0);
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot split " + edited, e);
		}
	}

	public void mergeWithPrevious() {
		checkEdited();
		try {
			Paragraph paragraph = (Paragraph) edited;
			Text text = (Text) paragraph.getControl();
			String txt = text.getText();
			Node paragraphNode = paragraph.getNode();
			if (paragraphNode.getIndex() == 1)
				return;// do nothing
			Node sectionNode = paragraphNode.getParent();
			Node previousNode = sectionNode
					.getNode(p(paragraphNode.getIndex() - 1));
			String previousTxt = textInterpreter.read(previousNode);
			textInterpreter.write(previousNode, previousTxt + txt);
			paragraphNode.remove();
			sectionNode.getSession().save();

			Paragraph previousParagraph = paragraphMergedWithPrevious(
					paragraph, previousNode);
			edit(previousParagraph, previousTxt.length());
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
		}
	}

	public void mergeWithNext() {
		checkEdited();
		try {
			Paragraph paragraph = (Paragraph) edited;
			Text text = (Text) paragraph.getControl();
			String txt = text.getText();
			Node paragraphNode = paragraph.getNode();
			Node sectionNode = paragraphNode.getParent();
			NodeIterator paragraphNodes = sectionNode.getNodes(CMS_P);
			long size = paragraphNodes.getSize();
			if (paragraphNode.getIndex() == size)
				return;// do nothing
			Node nextNode = sectionNode
					.getNode(p(paragraphNode.getIndex() + 1));
			String nextTxt = textInterpreter.read(nextNode);
			textInterpreter.write(paragraphNode, txt + nextTxt);

			Section section = paragraph.getSection();
			Paragraph removed = (Paragraph) section.getParagraph(nextNode
					.getIdentifier());

			nextNode.remove();
			sectionNode.getSession().save();

			paragraphMergedWithNext(paragraph, removed);
			edit(paragraph, txt.length());
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
		}
	}

	protected void deepen() {

	}

	protected void undeepen() {

	}

	//
	// UI CHANGES
	//
	protected Paragraph paragraphSplitted(Paragraph paragraph, Node newNode)
			throws RepositoryException {
		Section section = paragraph.getSection();
		updateContent(paragraph);
		Paragraph newParagraph = newParagraph(section, newNode);
		newParagraph.setLayoutData(CmsUtils.fillWidth());
		newParagraph.moveBelow(paragraph);
		layout(paragraph.getControl(), newParagraph.getControl());
		return newParagraph;
	}

	protected Paragraph sectionTitleSplitted(SectionTitle sectionTitle,
			Node newNode) throws RepositoryException {
		updateContent(sectionTitle);
		Paragraph newParagraph = newParagraph(sectionTitle.getSection(),
				newNode);
		// we assume beforeFirst is not null since there was a sectionTitle
		newParagraph.moveBelow(sectionTitle.getSection().getHeader());
		layout(sectionTitle.getControl(), newParagraph.getControl());
		return newParagraph;
	}

	protected Paragraph paragraphMergedWithPrevious(Paragraph removed,
			Node remaining) throws RepositoryException {
		Section section = removed.getSection();
		removed.dispose();

		Paragraph paragraph = (Paragraph) section.getParagraph(remaining
				.getIdentifier());
		updateContent(paragraph);
		layout(paragraph.getControl());
		return paragraph;
	}

	protected void paragraphMergedWithNext(Paragraph remaining,
			Paragraph removed) throws RepositoryException {
		removed.dispose();
		updateContent(remaining);
		layout(remaining.getControl());
	}

	protected void layout(Control... controls) {
		page.layout(controls);
	}

	//
	// LISTENERS
	//
	@Override
	public void keyPressed(KeyEvent e) {
		if (log.isTraceEnabled())
			log.trace(e);

		if (edited == null)
			return;
		boolean altPressed = (e.stateMask & SWT.ALT) != 0;
		boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;

		// Common
		if (e.keyCode == SWT.ESC) {
			cancelEdit();
		} else if (e.character == '\r') {
			splitEdit();
		} else if (e.character == '\t') {
			if (!shiftPressed) {
				deepen();
			} else if (shiftPressed) {
				undeepen();
			}
		} else {
			if (edited instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) edited;
				if (altPressed && e.keyCode == SWT.ARROW_RIGHT) {
					edit(paragraph.nextParagraph(), 0);
				} else if (altPressed && e.keyCode == SWT.ARROW_LEFT) {
					edit(paragraph.previousParagraph(), 0);
				} else if (e.character == SWT.BS) {
					Text text = (Text) paragraph.getControl();
					int caretPosition = text.getCaretPosition();
					if (caretPosition == 0) {
						mergeWithPrevious();
					}
				} else if (e.character == SWT.DEL) {
					Text text = (Text) paragraph.getControl();
					int caretPosition = text.getCaretPosition();
					int charcount = text.getCharCount();
					if (caretPosition == charcount) {
						mergeWithNext();
					}
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	protected void checkEdited() {
		if (edited == null || (edited instanceof Widget)
				&& ((Widget) edited).isDisposed())
			throw new CmsException(
					"Edited should not be null or disposed at this stage");
	}

	protected String p(Integer index) {
		StringBuilder sb = new StringBuilder(6);
		sb.append(CMS_P).append('[').append(index).append(']');
		return sb.toString();
	}

	protected EditableTextPart getEdited() {
		return edited;
	}

	public Section getMainSection() {
		return mainSection;
	}

	public MouseListener getMouseListener() {
		return mouseListener;
	}

	public TextInterpreter getTextInterpreter() {
		return textInterpreter;
	}

	public CmsEditable getCmsEditable() {
		return cmsEditable;
	}

	public StyledTools getStyledTools() {
		return null;
	}

	private class ML extends MouseAdapter {
		private static final long serialVersionUID = 8526890859876770905L;

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			if (cmsEditable.canEdit() && !cmsEditable.isEditing())
				cmsEditable.startEditing();
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (cmsEditable.isEditing()) {
				if (e.button == 1) {
					Control source = (Control) e.getSource();
					EditableTextPart composite = findDataParent(source);
					Point point = new Point(e.x, e.y);
					edit(composite, source.toDisplay(point));
				} else if (e.button == 3) {
					EditableTextPart composite = findDataParent((Control) e
							.getSource());
					if (getStyledTools() != null)
						getStyledTools().show(composite, new Point(e.x, e.y));
				}
			}
		}

		private EditableTextPart findDataParent(Control parent) {
			if (parent instanceof EditableTextPart) {
				return (EditableTextPart) parent;
			}
			if (parent.getParent() != null)
				return findDataParent(parent.getParent());
			else
				throw new CmsException("No data parent found");
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}

	}
}
