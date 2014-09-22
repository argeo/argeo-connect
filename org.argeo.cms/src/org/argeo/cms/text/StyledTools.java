package org.argeo.cms.text;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.CmsEditable;
import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/** Dialog to edit a text part. */
class StyledTools extends Shell implements CmsNames, TextStyles {
	private final static String[] DEFAULT_TEXT_STYLES = {
			TextStyles.TEXT_DEFAULT, TextStyles.TEXT_PRE, TextStyles.TEXT_QUOTE };

	private final CmsEditable cmsEditable;

	private static final long serialVersionUID = -3826246895162050331L;
	private EditableTextPart source;
	private List<StyleButton> styleButtons = new ArrayList<StyledTools.StyleButton>();

	private Label deleteButton, publishButton, editButton;

	public StyledTools(Display display, CmsEditable cmsEditable) {
		super(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.cmsEditable = cmsEditable;

		setLayout(new GridLayout());
		setData(RWT.CUSTOM_VARIANT, TEXT_STYLED_TOOLS_DIALOG);

		StyledToolMouseListener stml = new StyledToolMouseListener();
		if (cmsEditable.isEditing()) {

			for (String style : DEFAULT_TEXT_STYLES) {
				StyleButton styleButton = new StyleButton(this, SWT.NONE);
				styleButton.setData(RWT.CUSTOM_VARIANT, style);
				styleButton.setData(RWT.MARKUP_ENABLED, true);
				styleButton.addMouseListener(stml);
				styleButtons.add(styleButton);
			}

			// Delete
			deleteButton = new Label(this, SWT.NONE);
			deleteButton.setText("Delete");
			deleteButton.addMouseListener(stml);

			// Publish
			publishButton = new Label(this, SWT.NONE);
			publishButton.setText("Publish");
			publishButton.addMouseListener(stml);
		} else if (cmsEditable.canEdit()) {
			// Edit
			editButton = new Label(this, SWT.NONE);
			editButton.setText("Edit");
			editButton.addMouseListener(stml);

		}

		addShellListener(new ToolsShellListener());
	}

	public void show(EditableTextPart source, Point location) {
		if (isVisible())
			setVisible(false);

		this.source = source;

		if (source instanceof StyledComposite) {
			StyledComposite sc = (StyledComposite) source;
			final int size = 16;
			String text = sc.getText();
			String textToShow = text.length() > size ? sc.getText().substring(
					0, size - 3)
					+ "..." : text;
			for (StyleButton styleButton : styleButtons) {
				styleButton.setText(textToShow);
			}
		}
		pack();
		layout();
		setLocation(source.toDisplay(location.x, location.y));
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
			Object eventSource = e.getSource();
			if (eventSource instanceof StyleButton) {
				StyleButton sb = (StyleButton) e.getSource();
				String style = sb.getData(RWT.CUSTOM_VARIANT).toString();
				StyledComposite sc = (StyledComposite) source;
				sc.setTextStyle(style);
				try {
					Node paragraphNode = sc.getParagraphNode();
					paragraphNode.setProperty(CMS_STYLE, style);
					paragraphNode.getSession().save();
				} catch (RepositoryException e1) {
					throw new CmsException("Cannot set style " + style + " on "
							+ sc, e1);
				}
			} else if (eventSource == deleteButton) {
				StyledComposite sc = (StyledComposite) source;
				try {
					Node paragraphNode = sc.getParagraphNode();
					Session session = paragraphNode.getSession();
					paragraphNode.remove();
					session.save();
					source.dispose();
					// page.layout(true, true);
				} catch (RepositoryException e1) {
					throw new CmsException("Cannot delete " + sc, e1);
				}
			} else if (eventSource == editButton) {
				cmsEditable.startEditing();
			} else if (eventSource == publishButton) {
				cmsEditable.stopEditing();
			}
			setVisible(false);
		}
	}

	class ToolsShellListener extends org.eclipse.swt.events.ShellAdapter {
		private static final long serialVersionUID = 8432350564023247241L;

		@Override
		public void shellDeactivated(ShellEvent e) {
			setVisible(false);
		}

	}
}
