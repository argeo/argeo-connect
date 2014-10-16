package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsException;
import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.viewers.CompositeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TextViewer2 extends CompositeViewer implements CmsNames {
	private static final long serialVersionUID = 4115974214685276862L;

	public TextViewer2(Composite composite) {
		super(composite);
	}

	@Override
	protected void addControls(Composite parent, Object element) {
		Node node = (Node) element;
		try {
			if (node.isNodeType(CmsTypes.CMS_STYLED)) {
				Label label = new Label(parent, SWT.LEAD | SWT.WRAP);
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
						false));
				label.setData(RWT.MARKUP_ENABLED, true);
				hookControl(label);
			} else if (node.isNodeType(CmsTypes.CMS_SECTION)) {
				if (node.hasProperty(Property.JCR_TITLE)) {
					Label label = new Label(parent, SWT.LEAD | SWT.WRAP);
					label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							false));
					hookControl(label);
				}
			}
		} catch (RepositoryException e) {
			throw new CmsException("Cannot update viewer cell for " + node, e);
		}
	}

	@Override
	protected ColumnViewerEditor createViewerEditor() {
		
		return new TextViewerEditor(this,
				new ColumnViewerEditorActivationStrategy(this),
				ColumnViewerEditor.DEFAULT);
	}

}
