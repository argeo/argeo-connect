package org.argeo.cms.text;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUiProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Display the text of the context, and provide an editor if the user can edit. */
public class TextDynamicPages implements CmsUiProvider, CmsNames {
	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		if (context.isNodeType(CmsTypes.CMS_TEXT)) {
			new TextDisplay(parent, context);
		}

		return null;
	}
}
