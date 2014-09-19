package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Editable text part displaying styled text. */
class StyledComposite extends EditableTextPart implements CmsNames, TextStyles {
	private static final long serialVersionUID = -6372283442330912755L;
	private Control child;
	private final Session session;
	private final String nodePath;
	private final TextInterpreter textInterpreter;
	private MouseListener childMouseListener;

	public StyledComposite(Composite parent, int swtStyle, Node node,
			TextInterpreter textInterpreter, MouseListener childMouseListener)
			throws RepositoryException {
		super(parent, swtStyle);
		this.session = node.getSession();
		assert textInterpreter != null;
		this.textInterpreter = textInterpreter;
		this.childMouseListener = childMouseListener;

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		setLayout(CmsUtils.noSpaceGridLayout());
		String content = textInterpreter.raw(node);
		String style = null;
		if (node.hasProperty(CMS_STYLE))
			style = node.getProperty(CMS_STYLE).getString();
		else
			style = TextStyles.TEXT_DEFAULT;
		nodePath = node.getPath();
		setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_COMPOSITE);
		child = createLabel(content, style);
	}

	public Label createLabel(String content, String style) {
		Composite container = createBox(this, style + "_container");
		Composite box = createBox(container, style + "_box");

		Label lbl = new Label(box, SWT.LEAD | SWT.WRAP);
		lbl.setText(content);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lbl.setData(RWT.MARKUP_ENABLED, true);
		lbl.setData(RWT.CUSTOM_VARIANT, style);
		if (childMouseListener != null)
			lbl.addMouseListener(childMouseListener);
		return lbl;
	}

	public Text createText(String content, String style, int height) {
		Composite container = createBox(this, style + "_container");
		Composite box = createBox(container, style + "_box");

		Text text = new Text(box, SWT.MULTI | SWT.WRAP);
		GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
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
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		container.setLayout(CmsUtils.noSpaceGridLayout());
		container.setData(RWT.CUSTOM_VARIANT, style);
		return container;
	}

	protected void startEditing() {
		String content = textInterpreter.read(session, nodePath);
		Object style = child.getData(RWT.CUSTOM_VARIANT);
		int height = child.getSize().y;
		clear();
		child = createText(content, style == null ? null : style.toString(),
				height);
		layout();
	}

	protected void stopEditing() {
		String content = ((Text) child).getText();
		Node paragraphNode = textInterpreter.write(session, nodePath, content);
		Object style = child.getData(RWT.CUSTOM_VARIANT);
		clear();
		String raw = textInterpreter.raw(paragraphNode);
		child = createLabel(raw, style == null ? null : style.toString());
		layout();
	}

	public String getText() {
		// TODO compatibility with interpreter
		assert child != null;
		if (child instanceof Label)
			return ((Label) child).getText();
		else if (child instanceof Text)
			return ((Text) child).getText();
		else
			return null;
	}

	public Node getParagraphNode() throws RepositoryException {
		return session.getNode(nodePath);
	}

	public Control getChild() {
		return child;
	}

	// public String getNodePath() {
	// return nodePath;
	// }

	// public String getNodePath() {
	// return nodePath;
	// }
	//
	public void setTextStyle(String style) {
		assert child != null;
		String content = getText();
		clear();
		child = createLabel(content, style == null ? null : style.toString());
		getParent().layout(true, true);
	}

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

}
