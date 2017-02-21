package org.argeo.connect.ui.util;

import org.eclipse.swt.widgets.Composite;

/**
 * Use this class rather than Composite as parent for the inner Control of the
 * CTabFolder to provide lazy loading abilities
 */
public abstract class LazyCTabControl extends Composite {
	private static final long serialVersionUID = -3381279482510581039L;

	public LazyCTabControl(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void setVisible(boolean visible) {
		// Creates the child controls if needed before showing the tab to avoid
		// moving content
		if (visible) {
			if (this.getChildren().length == 0)
				createPartControl(this);
			refreshPartControl();
		}
		super.setVisible(visible);
	}

	public abstract void createPartControl(Composite parent);

	public abstract void refreshPartControl();
}