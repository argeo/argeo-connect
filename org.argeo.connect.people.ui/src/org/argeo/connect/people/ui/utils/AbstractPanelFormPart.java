package org.argeo.connect.people.ui.utils;

import javax.jcr.Node;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Extends the usual Abstract form part to ease the management of panel that
 * must totally be redrawn on each refresh, typically to display different
 * layouts depending on the checkout state
 */
public abstract class AbstractPanelFormPart extends AbstractFormPart {
	private boolean isCurrentlyCheckedOut;
	private final Composite parent;
	private final Node entity;

	public AbstractPanelFormPart(Composite parent, Node entity) {
		this.parent = parent;
		this.entity = entity;
		// will force creation on first pass
		isCurrentlyCheckedOut = !CommonsJcrUtils.isNodeCheckedOutByMe(entity);
	}

	@Override
	public void refresh() {
		super.refresh();

		if (isCurrentlyCheckedOut != CommonsJcrUtils
				.isNodeCheckedOutByMe(entity)) {
			isCurrentlyCheckedOut = CommonsJcrUtils
					.isNodeCheckedOutByMe(entity);

			for (Control control : parent.getChildren()) {
				control.dispose();
			}
			reCreateChildComposite(parent, entity);
			parent.layout();
		} else
			refreshContent(parent, entity);
	}

	protected boolean isCurrentlyCheckedOut() {
		return isCurrentlyCheckedOut;
	}

	abstract protected void reCreateChildComposite(Composite parent, Node entity);

	abstract protected void refreshContent(Composite parent, Node entity);
}