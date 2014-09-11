package org.argeo.cms.text;

import java.io.StringReader;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Read-only display of text. */
public class TextDisplay implements CmsNames {
	private final static Log log = LogFactory.getLog(TextDisplay.class);

	private Node textNode;

	private Composite textCmp;
	private ScrolledComposite scrolledArea;

	private Boolean editing = false;
	private Text editor;

	public TextDisplay(Composite parent, Node textNode) {
		this.textNode = textNode;

		if (log.isDebugEnabled())
			log.debug("Editing " + textNode);

		scrolledArea = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledArea.setExpandVertical(true);
		scrolledArea.setExpandHorizontal(true);
		scrolledArea
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		textCmp = new Composite(scrolledArea, SWT.NONE);
		textCmp.addMouseListener(new TextMouseListener());
		// textCmp.setLayout(CmsUtils.noSpaceGridLayout());
		scrolledArea.setContent(textCmp);

		// http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/CreateaScrolledCompositewithwrappingcontent.htm
		scrolledArea.addControlListener(new ScrollControlListener());
		refresh();
	}

	protected void refresh() {
		try {
			if (textCmp == null)
				return;
			// clear
			for (Control child : textCmp.getChildren())
				child.dispose();

			if (editing) {
				textCmp.setLayout(CmsUtils.noSpaceGridLayout(new GridLayout(2,
						false)));
			} else {
				textCmp.setLayout(CmsUtils.noSpaceGridLayout());
			}

			for (NodeIterator ni = textNode.getNodes(); ni.hasNext();) {
				Node child = ni.nextNode();

				if (editing) {
					new Label(textCmp, SWT.NONE).setText("*");
				}

				if (child.isNodeType(CmsTypes.CMS_STYLED)) {
					Label lbl = new Label(textCmp, SWT.LEAD | SWT.WRAP);
					lbl.setText(child.getProperty(CMS_CONTENT).getString());
					lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false));
					lbl.setData(RWT.MARKUP_ENABLED, true);
					lbl.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_DEFAULT);
				}
			}

			if (editing) {
				new Label(textCmp, SWT.NONE).setText("*");

				editor = new Text(textCmp, SWT.MULTI | SWT.WRAP);
				GridData textLayoutData = new GridData(GridData.FILL,
						GridData.FILL, true, true);
				textLayoutData.minimumHeight = 200;
				editor.setLayoutData(textLayoutData);
				editor.setData(RWT.CUSTOM_VARIANT, TextStyles.TEXT_EDITOR);
				editor.addKeyListener(new TextKeyListener());
				editor.setFocus();
			}

			textCmp.layout(true, true);
			updateScroll();
		} catch (RepositoryException e) {
			throw new CmsException("Cannot refresh text " + textNode, e);
		}
	}

	protected void updateScroll() {
		Rectangle r = scrolledArea.getClientArea();
		Point preferredSize = textCmp.computeSize(r.width, SWT.DEFAULT);
		scrolledArea.setMinHeight(preferredSize.y);
	}

	private class ScrollControlListener extends ControlAdapter {
		private static final long serialVersionUID = -3586986238567483316L;

		public void controlResized(ControlEvent e) {
			updateScroll();
		}

	}

	private class TextMouseListener extends MouseAdapter {
		private static final long serialVersionUID = 4876233808807862257L;

		@Override
		public void mouseDoubleClick(MouseEvent e) {
			editing = !editing;
			refresh();
		}

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
				List<String> lines = IOUtils.readLines(new StringReader(editor
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
				throw new CmsException("Cannot read " + editor.getText(), e1);
			}
		}

	}
}
