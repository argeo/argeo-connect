package org.argeo.connect.ui;

import org.argeo.cms.ui.CmsEditable;
import org.argeo.cms.ui.eclipse.forms.FormToolkit;
import org.argeo.cms.ui.eclipse.forms.IManagedForm;

/** Marker interface for an editor. */
public interface ConnectEditor extends CmsEditable {
	String PARAM_JCR_ID = "param.jcrId";
	String PARAM_OPEN_FOR_EDIT = "param.openForEdit";
	// public final static String PARAM_CTAB_ID = "param.cTabId";

	public FormToolkit getFormToolkit();

	public IManagedForm getManagedForm();
}
