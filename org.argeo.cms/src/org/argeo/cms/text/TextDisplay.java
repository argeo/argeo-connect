package org.argeo.cms.text;

import java.io.StringReader;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;

import org.apache.commons.io.IOUtils;
import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.ScrolledPage;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/** Read-only display of text. */
public class TextDisplay implements CmsEditable, CmsNames, TextStyles {
	// private final static Log log = LogFactory.getLog(TextDisplay.class);

	private final Node textNode;
	private final String textNodePath;// cache
	private VersionManager versionManager;

	private ScrolledPage page;
	private StyledTools styledTools;
	private TextPartListener textPartListener;

	private Boolean canEdit = false;
	private EditableTextPart edited;
	private Text addText;
	private TextInterpreter textInterpreter = new IdentityTextInterpreter();

	public TextDisplay(Composite parent, Node textNode) {
		this.textNode = textNode;
		try {
			this.textNodePath = textNode.getPath();
			if (textNode.getSession().hasPermission(textNode.getPath(),
					Session.ACTION_ADD_NODE)) {
				canEdit = true;
				versionManager = textNode.getSession().getWorkspace()
						.getVersionManager();
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot initialize text display for "
					+ textNode, e);
		}

		page = new ScrolledPage(parent, SWT.NONE);
		styledTools = new StyledTools(page.getDisplay(), this);
		if (canEdit())
			textPartListener = new TextPartListener();

		refresh();
	}

	protected void refresh() {
		try {
			if (page == null)
				return;
			// clear
			for (Control child : page.getChildren())
				child.dispose();

			page.setLayout(CmsUtils.noSpaceGridLayout());

			for (NodeIterator ni = textNode.getNodes(); ni.hasNext();) {
				Node child = ni.nextNode();
				if (child.isNodeType(CmsTypes.CMS_STYLED)) {
					new StyledComposite(page, SWT.NONE, child, textInterpreter,
							textPartListener);
				}
			}

			if (isEditing()) {
				addText = new Text(page, SWT.MULTI | SWT.WRAP);
				GridData textLayoutData = new GridData(GridData.FILL,
						GridData.FILL, true, true);
				textLayoutData.minimumHeight = 200;
				addText.setLayoutData(textLayoutData);
				addText.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_DEFAULT);
				addText.addKeyListener(new TextKeyListener());
				addText.setFocus();
			}

			page.layout(true, true);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot refresh text " + textNode, e);
		}
	}

	public Boolean isEditing() {
		try {
			if (!canEdit())
				return false;
			return versionManager.isCheckedOut(textNodePath);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot check whether " + textNodePath
					+ " is editing", e);
		}
	}

	public void setLayoutData(Object layoutData) {
		page.getScrolledComposite().setLayoutData(layoutData);
	}

	private class TextPartListener implements MouseListener {
		private static final long serialVersionUID = 4221123959609321884L;

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			EditableTextPart etp = getEtp(e.getSource());
			if (isEditing()) {
				if (edited != null)
					edited.stopEditing();

				edited = etp;
				edited.startEditing();

				page.layout(true, true);
			}
		}

		@Override
		public void mouseDown(MouseEvent e) {
			EditableTextPart etp = getEtp(e.getSource());
			if (e.button == 1) {
				if (isEditing()) {
					if (edited != null && etp != edited) {
						edited.stopEditing();
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

		private EditableTextPart getEtp(Object mouseEventSource) {
			// TODO make it more robust
			return (EditableTextPart) ((Control) mouseEventSource).getParent()
					.getParent().getParent();
		}
	}

	@Override
	public Boolean canEdit() {
		return canEdit;
	}

	@Override
	public void startEditing() {
		try {
			versionManager.checkout(textNodePath);
			refresh();
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot publish " + textNodePath);
		}
		refresh();
	}

	@Override
	public void stopEditing() {
		try {
			versionManager.checkin(textNodePath);
		} catch (RepositoryException e1) {
			throw new CmsException("Cannot publish " + textNodePath);
		}
		refresh();
	}

	private class TextKeyListener implements KeyListener {
		private static final long serialVersionUID = -7720848595910906899L;

		@Override
		public void keyPressed(KeyEvent e) {
			// if (log.isDebugEnabled())
			// log.debug(e.character);
			if (e.character == '\r') {
				Display.getCurrent().asyncExec(new AddContentAction(textNode));
			}

		}

		@Override
		public void keyReleased(KeyEvent e) {
			// if (log.isDebugEnabled())
			// log.debug(e.time + " " + e);
		}

	}

	private class AddContentAction implements Runnable {
		private final Node textNode;

		public AddContentAction(Node textNode) {
			this.textNode = textNode;
		}

		@Override
		public void run() {
			try {
				@SuppressWarnings("unchecked")
				List<String> lines = IOUtils.readLines(new StringReader(addText
						.getText()));
				for (String line : lines) {
					Node paragraph = textNode.addNode(CMS_P,
							CmsTypes.CMS_STYLED);
					textInterpreter.write(textNode.getSession(),
							paragraph.getPath(), line);
					// paragraph.setProperty(CMS_CONTENT, line);
				}
				textNode.getSession().save();
				refresh();
				// paragraphs.addAll(lines);
			} catch (Exception e1) {
				throw new CmsException("Cannot read " + addText.getText(), e1);
			}
		}

	}

	// private class StyledComposite extends EditableTextPart implements
	// MouseListener {
	// private static final long serialVersionUID = -6372283442330912755L;
	// private Control child;
	// private String nodePath;
	//
	// public StyledComposite(Composite parent, int swtStyle, Node node)
	// throws RepositoryException {
	// super(parent, swtStyle);
	// setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	// setLayout(CmsUtils.noSpaceGridLayout());
	// String content = textInterpreter.raw(node);
	// String style = null;
	// if (node.hasProperty(CMS_STYLE))
	// style = node.getProperty(CMS_STYLE).getString();
	// else
	// style = TextStyles.TEXT_DEFAULT;
	// nodePath = node.getPath();
	// setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_COMPOSITE);
	// child = createLabel(content, style);
	// }
	//
	// protected Label createLabel(String content, String style) {
	// Composite container = createBox(this, style + "_container");
	// Composite box = createBox(container, style + "_box");
	//
	// Label lbl = new Label(box, SWT.LEAD | SWT.WRAP);
	// lbl.setText(content);
	// lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	// lbl.setData(RWT.MARKUP_ENABLED, true);
	// lbl.setData(RWT.CUSTOM_VARIANT, style);
	// if (canEdit)
	// lbl.addMouseListener(this);
	// return lbl;
	// }
	//
	// protected Text createText(String content, String style, int height) {
	// Composite container = createBox(this, style + "_container");
	// Composite box = createBox(container, style + "_box");
	//
	// Text text = new Text(box, SWT.MULTI | SWT.WRAP);
	// GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
	// false);
	// textLayoutData.minimumHeight = height;
	// text.setLayoutData(textLayoutData);
	// text.setText(content);
	// text.setData(RWT.CUSTOM_VARIANT, style);
	// text.setData(RWT.MARKUP_ENABLED, true);// for the future
	// text.setFocus();
	// return text;
	// }
	//
	// protected Composite createBox(Composite parent, String style) {
	// Composite container = new Composite(parent, SWT.INHERIT_DEFAULT);
	// container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
	// false));
	// container.setLayout(CmsUtils.noSpaceGridLayout());
	// container.setData(RWT.CUSTOM_VARIANT, style);
	// return container;
	// }
	//
	// protected void startEditing() {
	// String content = textInterpreter.read(textNode, nodePath);
	// Object style = child.getData(RWT.CUSTOM_VARIANT);
	// int height = child.getSize().y;
	// clear();
	// child = createText(content,
	// style == null ? null : style.toString(), height);
	// layout();
	// }
	//
	// protected void stopEditing() {
	// String content = ((Text) child).getText();
	// Node paragraphNode = textInterpreter.write(textNode, nodePath,
	// content);
	// Object style = child.getData(RWT.CUSTOM_VARIANT);
	// clear();
	// String raw = textInterpreter.raw(paragraphNode);
	// child = createLabel(raw, style == null ? null : style.toString());
	// layout();
	// }
	//
	// public String getText() {
	// // TODO compatibility with interpreter
	// assert child != null;
	// if (child instanceof Label)
	// return ((Label) child).getText();
	// else if (child instanceof Text)
	// return ((Text) child).getText();
	// else
	// return null;
	// }
	//
	// // public String getNodePath() {
	// // return nodePath;
	// // }
	// //
	// // public void setTextStyle(String style) {
	// // assert child != null;
	// // String content = getText();
	// // clear();
	// // child = createLabel(content,
	// // style == null ? null : style.toString());
	// // getParent().layout(true, true);
	// // }
	//
	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// if (isEditing()) {
	// if (edited != null)
	// edited.stopEditing();
	//
	// edited = this;
	// edited.startEditing();
	//
	// page.layout(true, true);
	// }
	// }
	//
	// @Override
	// public void mouseDown(MouseEvent e) {
	// if (e.button == 1) {
	// if (isEditing()) {
	// if (edited != null && this != edited) {
	// edited.stopEditing();
	// edited = null;
	// page.layout(true, true);
	// }
	// }
	// } else if (e.button == 3) {
	// styledTools.show(this, new Point(e.x, e.y));
	// }
	// }
	//
	// @Override
	// public void mouseUp(MouseEvent e) {
	// }
	//
	// }

	// class StyledTools extends Shell {
	// private static final long serialVersionUID = -3826246895162050331L;
	// private StyledComposite source;
	// private List<StyleButton> styleButtons = new
	// ArrayList<TextDisplay.StyledTools.StyleButton>();
	//
	// public StyledTools() {
	// super(page.getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
	// // setLayout(CmsUtils.noSpaceGridLayout());
	// setLayout(new GridLayout());
	// setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_TOOLS_DIALOG);
	//
	// StyledToolMouseListener stml = new StyledToolMouseListener();
	// if (isEditing()) {
	//
	// for (String style : DEFAULT_TEXT_STYLES) {
	// StyleButton styleButton = new StyleButton(this, SWT.NONE);
	// styleButton.setData(RWT.CUSTOM_VARIANT, style);
	// styleButton.setData(RWT.MARKUP_ENABLED, true);
	// styleButton.addMouseListener(stml);
	// styleButtons.add(styleButton);
	// }
	//
	// // Delete
	// DeleteButton deleteButton = new DeleteButton(this, SWT.NONE);
	// deleteButton.setText("Delete");
	// deleteButton.addMouseListener(stml);
	//
	// // Publish
	// Label publish = new Label(this, SWT.NONE);
	// publish.setText("Publish");
	// publish.addMouseListener(new MouseAdapter() {
	//
	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// try {
	// versionManager.checkin(textNodePath);
	// } catch (RepositoryException e1) {
	// throw new CmsException("Cannot publish "
	// + textNodePath);
	// }
	// refresh();
	// setVisible(false);
	// }
	//
	// });
	// } else if (canEdit) {
	// // Edit
	// Label publish = new Label(this, SWT.NONE);
	// publish.setText("Edit");
	// publish.addMouseListener(new MouseAdapter() {
	//
	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// try {
	// versionManager.checkout(textNodePath);
	// refresh();
	// } catch (RepositoryException e1) {
	// throw new CmsException("Cannot publish "
	// + textNodePath);
	// }
	// refresh();
	// setVisible(false);
	// }
	//
	// });
	// }
	// }
	//
	// public void show(StyledComposite source, Point location) {
	// if (isVisible())
	// setVisible(false);
	//
	// this.source = source;
	//
	// final int size = 16;
	// String text = source.getText();
	// String textToShow = text.length() > size ? source.getText()
	// .substring(0, size - 3) + "..." : text;
	// for (StyleButton styleButton : styleButtons) {
	// styleButton.setText(textToShow);
	// }
	//
	// pack();
	// layout();
	// setLocation(source.toDisplay(location.x, location.y));
	// // setLocation(location);
	// open();
	//
	// }
	//
	// class StyleButton extends Label {
	// private static final long serialVersionUID = 7731102609123946115L;
	//
	// public StyleButton(Composite parent, int swtStyle) {
	// super(parent, swtStyle);
	// }
	//
	// }
	//
	// class DeleteButton extends Label {
	// private static final long serialVersionUID = -7488098923992809857L;
	//
	// public DeleteButton(Composite parent, int swtStyle) {
	// super(parent, swtStyle);
	// }
	//
	// }
	//
	// class StyledToolMouseListener extends MouseAdapter {
	// private static final long serialVersionUID = 8516297091549329043L;
	//
	// @Override
	// public void mouseDown(MouseEvent e) {
	// Object eventSource = e.getSource();
	// if (eventSource instanceof StyleButton) {
	// StyleButton sb = (StyleButton) e.getSource();
	// String style = sb.getData(RWT.CUSTOM_VARIANT).toString();
	// source.setTextStyle(style);
	// String nodePath = source.getNodePath();
	// try {
	// Node paragraphNode = textNode.getSession().getNode(
	// nodePath);
	// paragraphNode.setProperty(CMS_STYLE, style);
	// paragraphNode.getSession().save();
	// } catch (RepositoryException e1) {
	// throw new CmsException("Cannot set style " + style
	// + " on " + nodePath, e1);
	// }
	// } else if (eventSource instanceof DeleteButton) {
	// String nodePath = source.getNodePath();
	// try {
	// Node paragraphNode = textNode.getSession().getNode(
	// nodePath);
	// paragraphNode.remove();
	// textNode.getSession().save();
	// source.dispose();
	// page.layout(true, true);
	// } catch (RepositoryException e1) {
	// throw new CmsException("Cannot delete " + nodePath, e1);
	// }
	// }
	// setVisible(false);
	// }
	// }
	// }

}
