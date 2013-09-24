package org.argeo.connect.people.ui;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

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
	 * Shortcut to provide a gridlayout with no margin and no spacing (dafault
	 * are normally 5 px)
	 */
	public static GridLayout gridLayoutNoBorder() {
		return gridLayoutNoBorder(1);
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

}
