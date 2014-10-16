package org.argeo.cms.text;

import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/** Editable text part displaying styled text. */
class StyledComposite extends EditableTextPart implements CmsNames, TextStyles {
	private static final long serialVersionUID = -6372283442330912755L;
	private Control child;

	private Composite container;
	private Composite box;

	private TextInterpreter textInterpreter = new IdentityTextInterpreter();

	private MouseListener mouseListener;
	private TraverseListener traverseListener;

	public StyledComposite(Composite parent, int swtStyle, Item node)
			throws RepositoryException {
		super(parent, swtStyle);
		setLayout(CmsUtils.noSpaceGridLayout());
		setData(node);
		setData(Property.JCR_PATH, node.getPath());
		setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_COMPOSITE);
		clear(true);
		child = createLabel(null);
	}

	public StyledComposite(Composite parent, int swtStyle,
			TextInterpreter textInterpreter) {
		super(parent, swtStyle);
		setLayout(CmsUtils.noSpaceGridLayout());
		setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_COMPOSITE);
		this.textInterpreter = textInterpreter;
	}

	protected Label createLabel(String style) {
		Label lbl = new Label(box, SWT.LEAD | SWT.WRAP);
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		lbl.setData(RWT.MARKUP_ENABLED, true);
		lbl.setData(RWT.CUSTOM_VARIANT, style);
		if (mouseListener != null)
			lbl.addMouseListener(mouseListener);
		lbl.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent event) {
			}

			@Override
			public void focusGained(FocusEvent event) {
				System.out.println(event);

			}
		});
		// if (traverseListener != null)
		// lbl.addTraverseListener(traverseListener);
		return lbl;
	}

	protected Text createText(String style, int height) {
		final Text text = new Text(box, SWT.MULTI | SWT.WRAP);
		GridData textLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		textLayoutData.minimumHeight = height;
		text.setLayoutData(textLayoutData);
		text.setData(RWT.CUSTOM_VARIANT, style);
		text.setFocus();
		// if (mouseListener != null)
		// text.addMouseListener(mouseListener);
		// if (traverseListener != null)
		// text.addTraverseListener(traverseListener);
		return text;
	}

	protected Composite createBox(Composite parent) {
		Composite container = new Composite(parent, SWT.INHERIT_DEFAULT);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		container.setLayout(CmsUtils.noSpaceGridLayout());
		return container;
	}

	public void startEditing() {
		int height = child.getSize().y;
		String style = (String) child.getData(RWT.CUSTOM_VARIANT);
		clear(false);
		child = createText(style, height);
	}

	public void stopEditing() {
		String style = (String) child.getData(RWT.CUSTOM_VARIANT);
		clear(false);
		child = createLabel(style);
	}

	public Control getControl() {
		return child;
	}

	public Text getAsText() {
		return (Text) getControl();
	}

	public Label getAsLabel() {
		return (Label) getControl();
	}

	@Override
	public void setStyle(String style) {
		Object currentStyle = null;
		if (child != null)
			currentStyle = child.getData(RWT.CUSTOM_VARIANT);
		if (currentStyle != null && currentStyle.equals(style))
			return;

		clear(true);
		if (child == null || child instanceof Label)
			child = createLabel(style);
		else if (child instanceof Text)
			child = createText(style, child.getSize().y);
		child.getParent().setData(RWT.CUSTOM_VARIANT, style + "_box");
		child.getParent().getParent()
				.setData(RWT.CUSTOM_VARIANT, style + "_container");
	}

	protected void clear(boolean deep) {
		if (deep) {
			for (Control control : getChildren())
				control.dispose();
			container = createBox(this);
			box = createBox(container);
		} else {
			child.dispose();
		}
	}

	public void setText(Item item) {
		if (child instanceof Label)
			((Label) child).setText(textInterpreter.raw(item));
		else if (child instanceof Text)
			((Text) child).setText(textInterpreter.read(item));
	}

	public void save(Item item) {
		textInterpreter.write(item, ((Text) child).getText());
	}

	public void setMouseListener(MouseListener mouseListener) {
		this.mouseListener = mouseListener;
		if (child != null)
			child.addMouseListener(mouseListener);
	}

	public void setTraverseListener(TraverseListener traverseListener) {
		this.traverseListener = traverseListener;
		if (child != null)
			child.addTraverseListener(traverseListener);
	}

}
