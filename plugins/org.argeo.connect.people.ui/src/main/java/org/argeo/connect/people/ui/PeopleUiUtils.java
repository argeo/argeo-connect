package org.argeo.connect.people.ui;

import javax.jcr.Node;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** Some helper methods that factorize widely used snippets in people UI */
public class PeopleUiUtils {
	/** shortcut to set form data while dealing with switching panel */
	public static void setSwitchingFormData(Composite composite) {
		FormData fdLabel = new FormData();
		fdLabel.top = new FormAttachment(0, 0);
		fdLabel.left = new FormAttachment(0, 0);
		fdLabel.right = new FormAttachment(100, 0);
		fdLabel.bottom = new FormAttachment(100, 0);
		composite.setLayoutData(fdLabel);
	}

	public static void setTableDefaultStyle(TableViewer viewer,
			int customItemHeight) {
		Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(customItemHeight));
	}

	/**
	 * Shortcut to refresh the value of a <code>Text</code> given a Node and a
	 * property Name
	 */
	public static void refreshTextValue(Text text, Node entity, String propName) {
		String tmpStr = CommonsJcrUtils.getStringValue(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
	}

	/**
	 * Shortcut to provide a gridlayout with no margin and no spacing (dafault
	 * are normally 5 px)
	 */
	public static GridLayout gridLayoutNoBorder() {
		return gridLayoutNoBorder(1);
	}

	/**
	 * Shortcut to add a Text Modifylistener that updates a property on a Node
	 */
	public static void addTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 1549789407363632491L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(entity, propName, propType,
						text.getText()))
					part.markDirty();
			}
		});
	}

	/**
	 * Shortcut to provide a gridlayout with no margin and no spacing (dafault
	 * are normally 5 px) with the given column number (equals width is false).
	 */
	public static GridLayout gridLayoutNoBorder(int nbOfCol) {
		GridLayout gl = new GridLayout(nbOfCol, false);
		gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
		return gl;
	}

	/** Creates a text widget with RowData already set */
	public static Text createRDText(FormToolkit toolkit, Composite parent,
			String msg, String toolTip, int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}

	/** Creates a text widget with GridData already set */
	public static Text createGDText(FormToolkit toolkit, Composite parent,
			String msg, String toolTip, int width, int colSpan) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = width;
		gd.horizontalSpan = colSpan;
		text.setLayoutData(gd);
		return text;
	}

}
