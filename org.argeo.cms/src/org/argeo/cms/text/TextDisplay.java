package org.argeo.cms.text;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.ScrolledPage;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** Read-only display of text. */
public class TextDisplay implements CmsNames, TextStyles {
	private final static Log log = LogFactory.getLog(TextDisplay.class);

	private final static String[] DEFAULT_TEXT_STYLES = {
			TextStyles.TEXT_DEFAULT, TextStyles.TEXT_PRE, TextStyles.TEXT_QUOTE };

	private Node textNode;

	private Composite textCmp;
	private StyledTools styledTools;

	private Boolean editing = false;
	private EditableTextPart edited;
	private Text addText;

	public TextDisplay(Composite parent, Node textNode) {
		this.textNode = textNode;
		try {
			if (textNode.getSession().hasPermission(textNode.getPath(),
					Session.ACTION_ADD_NODE)) {
				editing = true;
				if (log.isDebugEnabled())
					log.debug("Editing " + textNode);
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot initialize text display for "
					+ textNode, e);
		}

		textCmp = new ScrolledPage(parent, SWT.NONE);
		styledTools = new StyledTools();

		refresh();
	}

	protected void refresh() {
		try {
			if (textCmp == null)
				return;
			// clear
			for (Control child : textCmp.getChildren())
				child.dispose();

			textCmp.setLayout(CmsUtils.noSpaceGridLayout());

			for (NodeIterator ni = textNode.getNodes(); ni.hasNext();) {
				Node child = ni.nextNode();
				if (child.isNodeType(CmsTypes.CMS_STYLED)) {
					new StyledComposite(textCmp, SWT.NONE, child);
				}
			}

			if (editing) {
				addText = new Text(textCmp, SWT.MULTI | SWT.WRAP);
				GridData textLayoutData = new GridData(GridData.FILL,
						GridData.FILL, true, true);
				textLayoutData.minimumHeight = 200;
				addText.setLayoutData(textLayoutData);
				addText.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_EDITOR);
				addText.addKeyListener(new TextKeyListener());
				addText.setFocus();
			}

			textCmp.layout(true, true);
		} catch (RepositoryException e) {
			throw new CmsException("Cannot refresh text " + textNode, e);
		}
	}

	// private class TextMouseListener extends MouseAdapter {
	// private static final long serialVersionUID = 4876233808807862257L;
	//
	// @Override
	// public void mouseDoubleClick(MouseEvent e) {
	// editing = !editing;
	// refresh();
	// }
	//
	// }

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
					paragraph.setProperty(CMS_CONTENT, line);
				}
				textNode.getSession().save();
				refresh();
				// paragraphs.addAll(lines);
			} catch (Exception e1) {
				throw new CmsException("Cannot read " + addText.getText(), e1);
			}
		}

	}

	private class StyledComposite extends EditableTextPart implements
			MouseListener {
		private static final long serialVersionUID = -6372283442330912755L;
		private Control child;

		public StyledComposite(Composite parent, int swtStyle, Node node)
				throws RepositoryException {
			super(parent, swtStyle);
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			setLayout(CmsUtils.noSpaceGridLayout());
			String content = node.getProperty(CMS_CONTENT).getString();
			String style = null;
			if (node.hasProperty(CMS_STYLE))
				style = node.getProperty(CMS_STYLE).getString();
			else
				style = TextStyles.TEXT_DEFAULT;
			setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_COMPOSITE);
			child = createLabel(content, style);
		}

		protected Label createLabel(String content, String style) {
			Composite container = createBox(this, style + "_container");
			Composite box = createBox(container, style + "_box");

			Label lbl = new Label(box, SWT.LEAD | SWT.WRAP);
			lbl.setText(content);
			lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			lbl.setData(RWT.MARKUP_ENABLED, true);
			lbl.setData(RWT.CUSTOM_VARIANT, style);
			if (editing)
				lbl.addMouseListener(this);
			return lbl;
		}

		protected Text createText(String content, String style, int height) {
			Composite container = createBox(this, style + "_container");
			Composite box = createBox(container, style + "_box");

			Text text = new Text(box, SWT.MULTI | SWT.WRAP);
			GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
					false);
			textLayoutData.minimumHeight = height;
			text.setLayoutData(textLayoutData);
			text.setText(content);
			text.setData(RWT.CUSTOM_VARIANT, style);
			text.setData(RWT.MARKUP_ENABLED, true);// for the future
			text.setFocus();
			return text;
		}

		protected Composite createBox(Composite parent, String style) {
			Composite container = new Composite(parent, SWT.INHERIT_DEFAULT);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					false));
			container.setLayout(CmsUtils.noSpaceGridLayout());
			container.setData(RWT.CUSTOM_VARIANT, style);
			return container;
		}

		protected void startEditing() {
			String content = ((Label) child).getText();
			Object style = child.getData(RWT.CUSTOM_VARIANT);
			int height = child.getSize().y;
			clear();
			child = createText(content,
					style == null ? null : style.toString(), height);
			layout();
		}

		protected void stopEditing() {
			String content = ((Text) child).getText();
			Object style = child.getData(RWT.CUSTOM_VARIANT);
			clear();
			child = createLabel(content,
					style == null ? null : style.toString());
			layout();
		}

		public String getText() {
			assert child != null;
			if (child instanceof Label)
				return ((Label) child).getText();
			else if (child instanceof Text)
				return ((Text) child).getText();
			else
				return null;
		}

		public void setTextStyle(String style) {
			assert child != null;
			String content = getText();
			clear();
			child = createLabel(content,
					style == null ? null : style.toString());
			getParent().layout(true, true);
		}

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			if (edited != null)
				edited.stopEditing();

			edited = this;
			edited.startEditing();

			getParent().layout(true, true);
		}

		@Override
		public void mouseDown(MouseEvent e) {
			if (e.button == 1) {
				if (edited != null && this != edited) {
					edited.stopEditing();
					edited = null;
					getParent().layout(true, true);
				}
			} else if (e.button == 3) {
				styledTools.show(this, new Point(e.x, e.y));
			}
		}

		@Override
		public void mouseUp(MouseEvent e) {
		}

	}

	class StyledTools extends Shell {
		private static final long serialVersionUID = -3826246895162050331L;
		private StyledComposite source;
		private List<StyleButton> styleButtons = new ArrayList<TextDisplay.StyledTools.StyleButton>();

		public StyledTools() {
			super(textCmp.getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
			// setLayout(CmsUtils.noSpaceGridLayout());
			setLayout(new GridLayout());
			setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_TOOLS_DIALOG);

			StyledToolMouseListener stml = new StyledToolMouseListener();
			for (String style : DEFAULT_TEXT_STYLES) {
				StyleButton styleButton = new StyleButton(this, SWT.NONE);
				styleButton.setData(RWT.CUSTOM_VARIANT, style);
				styleButton.setData(RWT.MARKUP_ENABLED, true);
				styleButton.addMouseListener(stml);
				styleButtons.add(styleButton);
			}
		}

		public void show(StyledComposite source, Point location) {
			if (isVisible())
				setVisible(false);

			this.source = source;

			final int size = 16;
			String text = source.getText();
			String textToShow = text.length() > size ? source.getText()
					.substring(0, size - 3) + "..." : text;
			for (StyleButton styleButton : styleButtons) {
				styleButton.setText(textToShow);
			}

			pack();
			layout();
			setLocation(source.toDisplay(location.x, location.y));
			// setLocation(location);
			open();

		}

		class StyleButton extends Label {
			private static final long serialVersionUID = 7731102609123946115L;

			public StyleButton(Composite parent, int swtStyle) {
				super(parent, swtStyle);
			}

		}

		class StyledToolMouseListener extends MouseAdapter {
			private static final long serialVersionUID = 8516297091549329043L;

			@Override
			public void mouseDown(MouseEvent e) {
				StyleButton sb = (StyleButton) e.getSource();
				String style = sb.getData(RWT.CUSTOM_VARIANT).toString();
				source.setTextStyle(style);
				setVisible(false);
			}

		}
	}

}
