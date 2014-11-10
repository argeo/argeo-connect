package org.argeo.cms.text;

import java.util.Observable;
import java.util.Observer;

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
import org.eclipse.swt.widgets.Text;

/** Base class for all text viewers/editors. */
public abstract class AbstractTextViewer extends ContentViewer implements
		TextViewer, CmsNames, KeyListener, Observer {
	private static final long serialVersionUID = -2401274679492339668L;

	private final static Log log = LogFactory.getLog(AbstractTextViewer.class);

	protected final Section mainSection;
	/** The basis for the layouts, typically a ScrolledPage. */
	protected final Composite page;

	private Composite edited;

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
			mainSection = new Section(this, parent, SWT.NONE, textNode);
			mainSection.setLayoutData(CmsUtils.fillWidth());

			if (!textNode.hasProperty(Property.JCR_TITLE)
					&& !textNode.hasNodes())
				if (cmsEditable.canEdit())
					initModel();

			// page.layout(true, true);
			// refresh();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot load main section", e);
		}
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
		// CmsEditionEvent evt = (CmsEditionEvent) arg;
		refresh();
		// FIXME notify cms editable change
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
			mainSection.refresh();
			layout(mainSection);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot refresh", e);
		}
	}

	// public void layout(Composite composite) {
	// composite.layout();
	// parentLayout(composite.getParent());
	// }

	// private void parentLayout(Composite parent) {
	// if (parent == null)
	// return;
	// // TODO make it more robust
	// parent.layout(true, false);
	// if (!(parent instanceof ScrolledPage))
	// parentLayout(parent.getParent());
	// }

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
	}

	// GENERIC EDITION

	public void edit(Composite composite, Object caretPosition) {
		try {
			if (edited == composite)
				return;

			if (edited != null && edited != composite)
				stopEditing(true);

			if (composite instanceof EditableTextPart) {
				EditableTextPart paragraph = (EditableTextPart) composite;
				paragraph.startEditing();
				paragraph.updateContent();
				prepare(paragraph, caretPosition);
				edited = composite;
				layout(paragraph.getControl());
				// if(edited instanceof Paragraph)
				// ((Paragraph)edited).getSection().layout(true, true);
			}
			// else if (composite instanceof SectionTitle) {
			// SectionTitle paragraph = (SectionTitle) composite;
			// paragraph.getTitle().startEditing();
			// paragraph.updateContent();
			// prepare(paragraph.getTitle(), caretPosition);
			// edited = paragraph;
			// }
		} catch (RepositoryException e) {
			throw new CmsException("Cannot edit " + composite, e);
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
		if (edited.isDisposed()) {
			edited = null;
			return;
		}

		if (edited instanceof EditableTextPart) {
			EditableTextPart paragraph = (EditableTextPart) edited;
			if (save)
				paragraph.save(CmsUtils.getDataItem(edited,
						mainSection.getNode()));
			paragraph.stopEditing();
			paragraph.updateContent();
			layout(((EditableTextPart) edited).getControl());
			edited = null;
		}
		// else if (edited instanceof SectionTitle) {
		// SectionTitle sectionTitle = (SectionTitle) edited;
		// if (save)
		// sectionTitle.getTitle().save((Property) sectionTitle.getData());
		// sectionTitle.getTitle().stopEditing();
		// sectionTitle.updateContent();
		// edited = null;
		// }
	}

	protected void prepare(EditableTextPart st, Object caretPosition) {
		Control control = st.getControl();
		if (control instanceof Text) {
			Text text = (Text) control;
			if (caretPosition != null)
				if (caretPosition instanceof Integer)
					text.setSelection((Integer) caretPosition);
				else if (caretPosition instanceof Point) {
					// TODO find a way to position the carte at the right place
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
			paragraph.updateContent();
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
			// section.refresh(false, false);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot delete " + paragraph, e1);
		}
	}

	// TEXT SPECIFIC EDITION
	public String getRawParagraphText(Paragraph paragraph) {
		return textInterpreter.raw(paragraph.getNode());
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

				// Section section = paragraph.getSection();
				// section.refresh(true, true);
				// Paragraph secondParagraph = (Paragraph) section
				// .getChild(secondNode);
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
				// section.refresh(true, true);
				// Paragraph paragraph = (Paragraph) section
				// .getChild(paragraphNode);
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

			// Section section = paragraph.getSection();

			paragraphNode.remove();
			sectionNode.getSession().save();

			// section.refresh(true, true);
			Paragraph previousParagraph = paragraphMergedWithPrevious(
					paragraph, previousNode);
			// Paragraph previousParagraph = (Paragraph) section
			// .getChild(previousNode);
			// previousParagraph.updateContent();
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
			Paragraph removed = section.getParagraph(nextNode);

			nextNode.remove();
			sectionNode.getSession().save();

			paragraphMergedWithNext(paragraph, removed);
			edit(paragraph, txt.length());
			// section.refresh(true, true);
			// edit(section.getChild(paragraphNode), txt.length());
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
		paragraph.updateContent();
		Paragraph newParagraph = new Paragraph(section, SWT.NONE, newNode,
				getMouseListener());
		newParagraph.setLayoutData(CmsUtils.fillWidth());
		newParagraph.moveBelow(paragraph);
		layout(paragraph.getControl(), newParagraph.getControl());
		// page.getParent().getParent().layout(true,true);
		return newParagraph;
	}

	protected Paragraph sectionTitleSplitted(SectionTitle sectionTitle,
			Node newNode) throws RepositoryException {
		sectionTitle.updateContent();
		Paragraph newParagraph = new Paragraph(sectionTitle.getSection(),
				SWT.NONE, newNode, getMouseListener());
		// we assume beforeFirst is not null since there was a sectionTitle
		newParagraph.moveBelow(sectionTitle.getSection().getBeforeFirst());
		layout(sectionTitle.getControl(), newParagraph.getControl());
		return newParagraph;
	}

	protected Paragraph paragraphMergedWithPrevious(Paragraph removed,
			Node remaining) throws RepositoryException {
		Section section = removed.getSection();
		removed.dispose();

		Paragraph paragraph = section.getParagraph(remaining);
		paragraph.updateContent();
		layout(paragraph.getControl());
		return paragraph;
	}

	protected void paragraphMergedWithNext(Paragraph remaining,
			Paragraph removed) throws RepositoryException {
		removed.dispose();
		remaining.updateContent();
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
		if (log.isDebugEnabled())
			log.debug(e);

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
		if (edited == null || edited.isDisposed())
			throw new CmsException(
					"Edited should not be null or disposed at this stage");
	}

	protected String p(Integer index) {
		StringBuilder sb = new StringBuilder(6);
		sb.append(CMS_P).append('[').append(index).append(']');
		return sb.toString();
	}

	protected Composite getEdited() {
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
					Composite composite = findDataParent(source);
					Point point = new Point(e.x, e.y);
					edit(composite, source.toDisplay(point));
				} else if (e.button == 3) {
					Composite composite = findDataParent((Control) e
							.getSource());
					if (getStyledTools() != null)
						getStyledTools().show(composite, new Point(e.x, e.y));
				}
			}
		}

		private Composite findDataParent(Control parent) {
			if (parent instanceof Composite && parent.getData() != null) {
				return (Composite) parent;
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
