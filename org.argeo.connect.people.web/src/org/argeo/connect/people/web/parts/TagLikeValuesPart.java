package org.argeo.connect.people.web.parts;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.util.JcrUiUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Displays existing values of a multi-valued String property that has the
 * injected name
 **/
public class TagLikeValuesPart implements CmsUiProvider {

	private String propertyName;

	/** Don't forget to inject propertyName */
	public TagLikeValuesPart() {
	}

	public TagLikeValuesPart(String propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public Control createUi(Composite preParent, final Node context) throws RepositoryException {
		Composite parent = new Composite(preParent, SWT.NO_FOCUS);

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.spacing = 8;
		parent.setLayout(rl);
		if (context.hasProperty(propertyName)) {

			Value[] values = context.getProperty(propertyName).getValues();
			for (Value value : values) {
				final String valueStr = value.getString();
				new Label(parent, SWT.NONE).setText(valueStr);

				Button icon = new Button(parent, SWT.NONE);
				icon.setLayoutData(CmsUtils.rowData16px());
				icon.setData(RWT.CUSTOM_VARIANT, "cms_icon_delete");
				icon.addSelectionListener(new SelectionAdapter() {
					private static final long serialVersionUID = 1L;

					@Override
					public void widgetSelected(SelectionEvent e) {
						JcrUiUtils.removeStringFromMultiValuedProp(context, propertyName, valueStr);
						// FIXME won't work: node is checked in
						// TODO refresh this part or the whole body
					}
				});
			}
		}
		return parent;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}
