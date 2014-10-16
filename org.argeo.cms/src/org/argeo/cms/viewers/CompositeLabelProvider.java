package org.argeo.cms.viewers;

import org.argeo.cms.CmsNames;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerColumn;

public abstract class CompositeLabelProvider extends ColumnLabelProvider
		implements CmsNames {
	private static final long serialVersionUID = 733437761057800926L;

	@Override
	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		// RAP: [if] Cell tooltips support
		// CellToolTipProvider.attach( viewer, this );
		// RAPEND: [if]
	}

	public void dispose(ColumnViewer viewer, ViewerColumn column) {
		dispose();
		// RAP: [if] Cell tooltips support
		// CellToolTipProvider.attach( viewer, null );
		// RAPEND: [if]
	}

}
