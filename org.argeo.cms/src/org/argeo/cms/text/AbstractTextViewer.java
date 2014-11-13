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
import org.argeo.cms.CmsImageManager;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsSession;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.IdentityTextInterpreter;
import org.argeo.cms.TextInterpreter;
import org.argeo.cms.viewers.Section;
import org.argeo.cms.viewers.SectionPart;
import org.argeo.cms.widgets.EditableImage;
import org.argeo.cms.widgets.EditablePart;
import org.argeo.cms.widgets.EditableText;
import org.argeo.cms.widgets.ScrolledPage;
import org.argeo.cms.widgets.StyledControl;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.rap.addons.fileupload.FileDetails;
import org.eclipse.rap.addons.fileupload.FileUploadEvent;
import org.eclipse.rap.addons.fileupload.FileUploadHandler;
import org.eclipse.rap.addons.fileupload.FileUploadListener;
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
import org.eclipse.swt.widgets.Widget;

/** Base class for all text viewers/editors. */
abstract class AbstractTextViewer extends ContentViewer implements CmsNames,
		KeyListener, Observer {
	private static final long serialVersionUID = -2401274679492339668L;

	private final static Log log = LogFactory.getLog(AbstractTextViewer.class);

	protected final TextSection mainSection;
	/** The basis for the layouts, typically a ScrolledPage. */
	protected final Composite page;

	private final CmsEditable cmsEditable;

	private TextInterpreter textInterpreter = new IdentityTextInterpreter();
	private CmsImageManager imageManager = CmsSession.current.get()
			.getImageManager();

	private MouseListener mouseListener;
	private FileUploadListener fileUploadListener;

	// THE edited part
	private EditablePart edited;

	public AbstractTextViewer(Composite parent, Node textNode,
			CmsEditable cmsEditable) {
		try {
			// CMS Editable (before main section!)
			this.cmsEditable = cmsEditable == null ? CmsEditable.NON_EDITABLE
					: cmsEditable;
			if (this.cmsEditable instanceof Observable)
				((Observable) this.cmsEditable).addObserver(this);

			if (cmsEditable.canEdit()) {
				mouseListener = new ML();
				fileUploadListener = new FUL();
			}

			page = findPage(parent);
			mainSection = new TextSection(parent, SWT.NONE, textNode);
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

	protected void refresh(TextSection section) throws RepositoryException {
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

	protected Boolean hasHeader(TextSection section) throws RepositoryException {
		return section.getNode().hasProperty(Property.JCR_TITLE);
	}

	/** Called if user can edit and model is not initialized */
	protected void initModel() throws RepositoryException {
		mainSection.getNode().addNode(CMS_P).addMixin(CmsTypes.CMS_STYLED);
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
		if (o == cmsEditable)
			editingStateChanged(cmsEditable);
	}

	/** Does nothing, to be overridden */
	protected void editingStateChanged(CmsEditable cmsEditable) {

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
	protected Paragraph newParagraph(TextSection parent, Node node)
			throws RepositoryException {
		Paragraph paragraph = new Paragraph(parent, parent.getStyle(), node);
		updateContent(paragraph);
		paragraph.setMouseListener(mouseListener);
		return paragraph;
	}

	protected Img newImg(TextSection parent, Node node)
			throws RepositoryException {
		Img img = new Img(parent, parent.getStyle(), node);
		updateContent(img);
		img.setMouseListener(mouseListener);
		return img;
	}

	protected SectionTitle newSectionTitle(TextSection parent, Node node)
			throws RepositoryException {
		SectionTitle sectionTitle = new SectionTitle(parent.getHeader(),
				parent.getStyle());
		updateContent(sectionTitle);
		sectionTitle.setMouseListener(mouseListener);
		return sectionTitle;
	}

	protected void updateContent(EditablePart part) throws RepositoryException {
		if (part instanceof SectionPart) {
			SectionPart sectionPart = (SectionPart) part;
			Node partNode = sectionPart.getNode();
			TextSection section = (TextSection) sectionPart.getSection();

			if (part instanceof StyledControl) {
				StyledControl styledControl = (StyledControl) part;
				if (partNode.isNodeType(CmsTypes.CMS_STYLED)) {
					String style = partNode.hasProperty(CMS_STYLE) ? partNode
							.getProperty(CMS_STYLE).getString() : section
							.getDefaultTextStyle();
					styledControl.setStyle(style);
				}
			}
			// use control AFTER setting style, since it may have been reset

			if (part instanceof EditableText) {
				EditableText paragraph = (EditableText) part;
				if (paragraph == edited)
					paragraph.setText(textInterpreter.read(partNode));
				else
					paragraph.setText(textInterpreter.raw(partNode));
			} else if (part instanceof EditableImage) {
				imageManager.load(partNode, part.getControl(), null);
			}
		} else if (part instanceof SectionTitle) {
			SectionTitle sectionTitle = (SectionTitle) part;
			sectionTitle.setStyle(sectionTitle.getSection().getTitleStyle());
			// use control AFTER setting style
			if (sectionTitle == edited)
				sectionTitle.setText(textInterpreter.read(sectionTitle
						.getProperty()));
			else
				sectionTitle.setText(textInterpreter.raw(sectionTitle
						.getProperty()));
		}
	}

	// protected void setText(EditableText textPart, Item item) {
	// Control child = textPart.getControl();
	// if (child instanceof Text) {
	// ((Label) child).setText(textInterpreter.raw(item));
	// } else {
	// ((Text) child).setText(textInterpreter.read(item));
	// }
	// }

	// GENERIC EDITION
	public void edit(EditablePart part) {
		edit(part, null);
	}

	public void edit(EditablePart part, Object caretPosition) {
		try {
			if (edited == part)
				return;

			if (edited != null && edited != part)
				stopEditing(true);

			part.startEditing();
			updateContent(part);
			prepare(part, caretPosition);
			edited = part;
			layout(part.getControl());
		} catch (RepositoryException e) {
			throw new CmsException("Cannot edit " + part, e);
		}
	}

	protected void save(EditablePart part) throws RepositoryException {
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

		if (edited == null) {
			log.warn("Told to stop editing while not editing anything");
			return;
		}

		if (save)
			save(edited);

		edited.stopEditing();
		updateContent(edited);
		layout(((EditablePart) edited).getControl());
		edited = null;
	}

	protected void prepare(EditablePart part, Object caretPosition) {
		Control control = part.getControl();
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
		} else if (part instanceof Img) {
			((Img) part).setFileUploadListener(fileUploadListener);
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
				Node secondNode = sectionNode.addNode(CMS_P);
				secondNode.addMixin(CmsTypes.CMS_STYLED);
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
				Node paragraphNode = sectionNode.addNode(CMS_P);
				paragraphNode.addMixin(CmsTypes.CMS_STYLED);
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
			Paragraph removed = (Paragraph) section.getSectionPart(nextNode
					.getIdentifier());

			nextNode.remove();
			sectionNode.getSession().save();

			paragraphMergedWithNext(paragraph, removed);
			edit(paragraph, txt.length());
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
		}
	}

	protected void upload(EditablePart part) {
		try {
			if (part instanceof SectionPart) {
				SectionPart sectionPart = (SectionPart) part;
				Node partNode = sectionPart.getNode();
				int partIndex = partNode.getIndex();
				Section section = sectionPart.getSection();
				Node sectionNode = section.getNode();

				if (part instanceof Paragraph) {
					Node newNode = sectionNode.addNode(CMS_P,
							CmsTypes.CMS_STYLED);
					if (partIndex < newNode.getIndex() - 1) {
						// was not last
						sectionNode.orderBefore(p(newNode.getIndex()),
								p(partIndex - 1));
					}
					sectionNode.orderBefore(p(partNode.getIndex()),
							p(newNode.getIndex()));
					Img img = newImg((TextSection) section, newNode);
					edit(img, null);
					layout(img.getControl());
				} else if (part instanceof Img) {
					edit(part, null);
					layout(((Img) part).getControl());
				}
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot upload", e);
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
		Paragraph newParagraph = newParagraph((TextSection) section, newNode);
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

		Paragraph paragraph = (Paragraph) section.getSectionPart(remaining
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

	public void layout(Control... controls) {
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
				Section section = paragraph.getSection();
				if (altPressed && e.keyCode == SWT.ARROW_RIGHT) {
					edit(section.nextSectionPart(paragraph), 0);
				} else if (altPressed && e.keyCode == SWT.ARROW_LEFT) {
					edit(section.previousSectionPart(paragraph), 0);
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

	protected EditablePart getEdited() {
		return edited;
	}

	public TextSection getMainSection() {
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
			if (cmsEditable.canEdit())
				if (cmsEditable.isEditing()) {
					if (e.button == 1) {
						Control source = (Control) e.getSource();
						EditablePart part = findDataParent(source);
						upload(part);
					}
				} else
					cmsEditable.startEditing();
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (cmsEditable.isEditing()) {
				if (e.button == 1) {
					Control source = (Control) e.getSource();
					EditablePart composite = findDataParent(source);
					Point point = new Point(e.x, e.y);
					edit(composite, source.toDisplay(point));
				} else if (e.button == 3) {
					EditablePart composite = findDataParent((Control) e
							.getSource());
					if (getStyledTools() != null)
						getStyledTools().show(composite, new Point(e.x, e.y));
				}
			}
		}

		private EditablePart findDataParent(Control parent) {
			if (parent instanceof EditablePart) {
				return (EditablePart) parent;
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

	private class FUL implements FileUploadListener {
		public void uploadProgress(FileUploadEvent event) {
			// handle upload progress
		}

		public void uploadFailed(FileUploadEvent event) {
			throw new CmsException("Upload failed " + event,
					event.getException());
		}

		public void uploadFinished(FileUploadEvent event) {
			for (FileDetails file : event.getFileDetails()) {
				if (log.isDebugEnabled())
					log.debug("Received: " + file.getFileName());
			}
			FileUploadHandler uploadHandler = (FileUploadHandler) event
					.getSource();
			uploadHandler.dispose();

			try {
				stopEditing(true);
			} catch (RepositoryException e) {
				throw new CmsException("Cannot stop editing", e);
			}
		}

	}
}
