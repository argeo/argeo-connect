package org.argeo.connect.people.rap.editors.util;

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
		super.setVisible(visible);

		if (this.getChildren().length == 0)
			createPartControl(this);

		refreshPartControl();
	}

	public abstract void createPartControl(Composite parent);

	public abstract void refreshPartControl();
}