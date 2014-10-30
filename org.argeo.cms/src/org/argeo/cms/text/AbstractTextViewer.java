package org.argeo.cms.text;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/** Base class for all text viewers/editors. */
public abstract class AbstractTextViewer extends ContentViewer implements
		TextViewer, CmsNames, KeyListener {
	private static final long serialVersionUID = -2401274679492339668L;

	private final static Log log = LogFactory.getLog(AbstractTextViewer.class);

	private ScrolledPage page;
	private Section mainSection;

	private Composite edited;

	private StyledTools styledTools;
	private TextInterpreter textInterpreter = new IdentityTextInterpreter();
	private final CmsEditable cmsEditable;

	public AbstractTextViewer(Composite parent, Node textNode,
			CmsEditable cmsEditable) {
		try {
			page = new ScrolledPage(parent, SWT.NONE);
			page.setLayout(CmsUtils.noSpaceGridLayout());
			mainSection = new Section(this, page, SWT.NONE, textNode);
			mainSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			if (cmsEditable == null)
				this.cmsEditable = CmsEditable.NON_EDITABLE;
			else
				this.cmsEditable = cmsEditable;
			if (this.cmsEditable.canEdit()) {
				styledTools = new StyledTools(this, page.getDisplay());
			}

			// if (!textNode.hasNodes())
			// textNode.addNode(CMS_P, CmsTypes.CMS_STYLED);

			refresh();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot load main section", e);
		}
	}

	@Override
	public Control getControl() {
		return page;
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void refresh() {
		Runnable loadingThread = new Runnable() {

			@Override
			public void run() {
				try {
					mainSection.refresh(true, true);
					page.layout();
				} catch (RepositoryException e) {
					throw new CmsException("Cannot refresh", e);
				}
			}

		};
		page.getDisplay().asyncExec(loadingThread);
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
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
			section.refresh(false, false);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot delete " + paragraph, e1);
		}
	}

	public String getRawParagraphText(Paragraph paragraph) {
		return textInterpreter.raw(paragraph.getNode());
	}

	public void edit(Composite composite, Object caretPosition) {
		try {
			if (edited == composite)
				return;

			if (edited != null && edited != composite)
				stopEditing(true);

			if (composite instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) composite;
				paragraph.startEditing();
				paragraph.updateContent();
				prepare(paragraph, caretPosition);
				edited = paragraph;
			} else if (composite instanceof SectionTitle) {
				SectionTitle paragraph = (SectionTitle) composite;
				paragraph.getTitle().startEditing();
				paragraph.updateContent();
				prepare(paragraph.getTitle(), caretPosition);
				edited = paragraph;
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot edit " + composite, e);
		}
	}

	protected void prepare(StyledComposite st, Object caretPosition) {
		Text text = st.getAsText();
		if (caretPosition != null)
			if (caretPosition instanceof Integer)
				text.setSelection((Integer) caretPosition);
			else if (caretPosition instanceof Point) {
				// TODO find a way to position the carte at the right place
			}
		text.setData(RWT.ACTIVE_KEYS, new String[] { "BACKSPACE", "ESC", "TAB",
				"SHIFT+TAB", "ALT+ARROW_LEFT", "ALT+ARROW_RIGHT",
				"ALT+ARROW_UP", "ALT+ARROW_DOWN", "RETURN", "ENTER", "DELETE" });
		text.setData(RWT.CANCEL_KEYS, new String[] { "ALT+ARROW_LEFT",
				"ALT+ARROW_RIGHT" });
		text.addKeyListener(this);
	}

	public void saveEdit() {
		try {
			if (edited != null)
				stopEditing(true);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot stop editing", e);
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
					sectionNode.orderBefore(CMS_P + '[' + secondNode.getIndex()
							+ ']', CMS_P + '[' + (firstNode.getIndex() + 1)
							+ ']');
				}

				// if we die in between, at least we still have the whole text
				// in the first node
				textInterpreter.write(secondNode, second);
				textInterpreter.write(firstNode, first);

				Section section = paragraph.getSection();
				section.refresh(true, true);
				Paragraph secondParagraph = (Paragraph) section
						.getChild(secondNode);
				edit(secondParagraph, 0);
			} else if (edited instanceof SectionTitle) {
				SectionTitle sectionTitle = (SectionTitle) edited;
				Text text = (Text) sectionTitle.getTitle().getControl();
				String txt = text.getText();
				int caretPosition = text.getCaretPosition();
				Section section = sectionTitle.getSection();
				Node sectionNode = section.getNode();
				Node node = sectionNode.addNode(CMS_P, CmsTypes.CMS_STYLED);
				textInterpreter.write(node, txt.substring(caretPosition));
				textInterpreter.write(
						sectionNode.getProperty(Property.JCR_TITLE),
						txt.substring(0, caretPosition));
				sectionNode.orderBefore(CMS_P + '[' + node.getIndex() + ']',
						CMS_P + "[1]");
				sectionNode.getSession().save();
				section.refresh(true, true);
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
			Node previousNode = sectionNode.getNode(CMS_P + '['
					+ (paragraphNode.getIndex() - 1) + ']');
			String previousTxt = textInterpreter.read(previousNode);
			textInterpreter.write(previousNode, previousTxt + txt);

			Section section = paragraph.getSection();

			paragraphNode.remove();
			sectionNode.getSession().save();

			section.refresh(true, true);
			Paragraph previousParagraph = (Paragraph) section
					.getChild(previousNode);
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
			Node nextNode = sectionNode.getNode(CMS_P + '['
					+ (paragraphNode.getIndex() + 1) + ']');
			String nextTxt = textInterpreter.read(nextNode);
			textInterpreter.write(paragraphNode, txt + nextTxt);

			Section section = paragraph.getSection();

			nextNode.remove();
			sectionNode.getSession().save();

			section.refresh(true, true);
			edit(section.getChild(paragraphNode), txt.length());
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

		if (edited instanceof Paragraph) {
			Paragraph paragraph = (Paragraph) edited;
			if (save)
				paragraph.save((Node) paragraph.getData());
			paragraph.stopEditing();
			paragraph.updateContent();
			edited = null;
		} else if (edited instanceof SectionTitle) {
			SectionTitle sectionTitle = (SectionTitle) edited;
			if (save)
				sectionTitle.getTitle().save((Property) sectionTitle.getData());
			sectionTitle.getTitle().stopEditing();
			sectionTitle.updateContent();
			edited = null;
		}
	}

	public void deepen() {
		checkEdited();
		try {
			if (edited instanceof Paragraph) {
				Paragraph paragraph = (Paragraph) edited;
				Text text = (Text) paragraph.getControl();
				String txt = text.getText();
				Node paragraphNode = paragraph.getNode();
				Section section = paragraph.getSection();
				Node sectionNode = section.getNode();
				Node newSectionNode = sectionNode.addNode(CMS_H,
						CmsTypes.CMS_SECTION);
				sectionNode.orderBefore(CMS_H + '[' + newSectionNode.getIndex()
						+ ']', CMS_P + '[' + paragraphNode.getIndex() + ']');
				paragraphNode.remove();
				// create property
				newSectionNode.setProperty(Property.JCR_TITLE, "");
				textInterpreter.write(
						newSectionNode.getProperty(Property.JCR_TITLE), txt);
				section.refresh(true, true);
			} else if (edited instanceof SectionTitle) {
				SectionTitle sectionTitle = (SectionTitle) edited;
				Section section = sectionTitle.getSection();
				Section parentSection = section.getParentSection();
				if (parentSection == null)
					return;// cannot deepen main section
				Node sectionNode = section.getNode();
				Node parentSectionNode = parentSection.getNode();
				if (sectionNode.getIndex() == 1)
					return;// cannot deepen first section
				Node previousNode = parentSectionNode.getNode(CMS_H + '['
						+ (sectionNode.getIndex() - 1) + ']');
				previousNode.getSession().move(sectionNode.getPath(),
						previousNode.getPath() + "/" + CMS_H);
				previousNode.getSession().save();
				parentSection.refresh(true, true);
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot deepen " + edited, e);
		}
	}

	public void undeepen() {
		checkEdited();
		try {
			if (edited instanceof Paragraph) {

			} else if (edited instanceof SectionTitle) {
				SectionTitle sectionTitle = (SectionTitle) edited;
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
					parentSectionNode.orderBefore(CMS_P + '['
							+ newParagrapheNode.getIndex() + ']', CMS_H + '['
							+ sectionNode.getIndex() + ']');
					String txt = textInterpreter.read(sectionNode
							.getProperty(Property.JCR_TITLE));
					section.getNode().remove();
					textInterpreter.write(newParagrapheNode, txt);

					parentSection.refresh(true, true);
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
							CMS_H + '[' + movedNode.getIndex() + ']', CMS_H
									+ '[' + (parentSectionNode.getIndex() + 1)
									+ ']');

					parentParentSectionNode.getSession().save();
					parentParentSection.refresh(true, true);
				}
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot undeepen " + edited, e);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (log.isDebugEnabled())
			log.debug(e);

		if (edited == null)
			return;
		boolean shiftPressed = (e.stateMask & SWT.SHIFT) != 0;
		boolean altPressed = (e.stateMask & SWT.ALT) != 0;

		if (edited instanceof Paragraph) {
			Paragraph paragraph = (Paragraph) edited;
			if (e.character == '\t') {
				if (!shiftPressed) {
					deepen();
				} else if (shiftPressed) {
					undeepen();
				}
			} else if (altPressed && e.keyCode == SWT.ARROW_RIGHT) {
				edit(paragraph.nextParagraph(), 0);
			} else if (altPressed && e.keyCode == SWT.ARROW_LEFT) {
				edit(paragraph.previousParagraph(), 0);

			} else if (e.keyCode == SWT.ESC) {
				cancelEdit();
			} else if (e.character == '\r') {
				splitEdit();
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
		} else if (edited instanceof SectionTitle) {
			if (e.character == '\t') {
				if (!shiftPressed) {
					deepen();
				} else if (shiftPressed) {
					undeepen();
				}
			} else if (e.character == '\r') {
				splitEdit();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	private void checkEdited() {
		if (edited == null || edited.isDisposed())
			throw new CmsException(
					"Edited should not be null or disposed at this stage");
	}

	public void layout(Composite composite) {
		composite.layout();
		parentLayout(composite.getParent());
	}

	private void parentLayout(Composite parent) {
		// TODO make it more robust
		parent.layout(true, false);
		if (!(parent instanceof ScrolledPage))
			parentLayout(parent.getParent());
	}

	public Section getMainSection() {
		return mainSection;
	}

	public TextInterpreter getTextInterpreter() {
		return textInterpreter;
	}

	public CmsEditable getCmsEditable() {
		return cmsEditable;
	}

	public StyledTools getStyledTools() {
		return styledTools;
	}

}
