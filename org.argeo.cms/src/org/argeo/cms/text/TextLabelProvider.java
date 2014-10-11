package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUtils;
import org.argeo.cms.viewers.CompositeItem;
import org.argeo.cms.viewers.CompositeLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

public class TextLabelProvider extends CompositeLabelProvider implements
		CmsNames {
	private static final long serialVersionUID = 733437761057800926L;

	@Override
	public void update(ViewerCell cell) {
		CompositeItem item = (CompositeItem) cell.getItem();
		Node node = (Node) cell.getElement();
		try {
			if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				Label label = new Label(item.getComposite(), SWT.LEAD
						| SWT.WRAP);
				label.setText(node.getProperty(CMS_CONTENT).getString());
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false));
				label.setData(RWT.MARKUP_ENABLED, true);
				if (node.hasProperty(CMS_STYLE))
					CmsUtils.style(label, node.getProperty(CMS_STYLE)
							.getString());
				else
					CmsUtils.style(label, TextStyles.TEXT_DEFAULT);
			} else if (node.isNodeType(CmsTypes.CMS_SECTION)) {
				if (node.hasProperty(Property.JCR_TITLE)) {
					Label label = new Label(item.getComposite(), SWT.LEAD
							| SWT.WRAP);
					label.setText(node.getProperty(Property.JCR_TITLE)
							.getString());
					label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false));
				}
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot update viewer cell for " + node, e);
		}
	}
}
