package org.argeo.connect.web;

import javax.jcr.Node;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class NavigationLink implements CmsUiProvider {
	private String label;
	private String custom;
	private String target;

	@Override
	public Control createUi(final Composite parent, Node context) {
		Label link = new Label(parent, SWT.PUSH);
		if (label != null)
			link.setText(label);
		if (custom != null)
			link.setData(RWT.CUSTOM_VARIANT, custom);
		else
			link.setData(RWT.CUSTOM_VARIANT, CmsStyles.CMS_NAVIGATION_LINK);

		// button.addSelectionListener(new SelectionAdapter() {
		// private static final long serialVersionUID = 5497235604093549630L;
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// AbstractCmsEntryPoint entryPoint = (AbstractCmsEntryPoint) parent
		// .getDisplay().getData(CmsSession.KEY);
		// entryPoint.navigateTo(target);
		// }
		// });

		link.setCursor(link.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		link.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				AbstractCmsEntryPoint entryPoint = (AbstractCmsEntryPoint) parent
						.getDisplay().getData(CmsSession.KEY);
				entryPoint.navigateTo(target);
			}
		});

		return link;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
