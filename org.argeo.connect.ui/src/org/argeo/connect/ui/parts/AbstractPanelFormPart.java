package org.argeo.connect.ui.parts;

import javax.jcr.Node;

import org.argeo.cms.ui.eclipse.forms.AbstractFormPart;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ui.ConnectEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends the usual Abstract form part to ease the management of panel that
 * must totally be redrawn on each refresh, typically to display different
 * layouts depending on the linked editor state
 */
public abstract class AbstractPanelFormPart extends AbstractFormPart {

	private ConnectEditor editor;
	private Boolean isEditing;
	private final Composite parent;
	private final Node entity;

	public AbstractPanelFormPart(Composite parent, ConnectEditor editor, Node entity) {
		this.parent = parent;
		this.entity = entity;
		this.editor = editor;
	}

	@Override
	public void refresh() {
		super.refresh();
		if (parent == null || parent.isDisposed())
			return;
		// if (editor == null)
		// editor = ((ConnectManagedForm) getManagedForm()).getEditor();

		if ((isEditing == null) || isEditing != editor.isEditing()) {
			isEditing = editor.isEditing();
			CmsUtils.clear(parent);
			reCreateChildComposite(parent, entity);
			parent.layout();
		} else
			refreshContent(parent, entity);
	}

	protected boolean isEditing() {
		return isEditing;
	}

	abstract protected void reCreateChildComposite(Composite parent, Node entity);

	abstract protected void refreshContent(Composite parent, Node entity);
}