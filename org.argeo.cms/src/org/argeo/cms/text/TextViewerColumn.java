package org.argeo.cms.text;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.widgets.Widget;

class TextViewerColumn extends ViewerColumn {

	private static final long serialVersionUID = -4189256253366654982L;

	public TextViewerColumn(ColumnViewer viewer, Widget columnOwner) {
		super(viewer, columnOwner);
	}

}