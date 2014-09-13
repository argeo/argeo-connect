package org.argeo.cms.text;

import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.CmsNames;
import org.argeo.cms.CmsTypes;
import org.argeo.cms.CmsUiProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Display the text of the context, and provide an editor if the user can edit. */
public class NewTextPage implements CmsUiProvider, CmsNames {
	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		// Node userHome = UserJcrUtils.getUserHome(context.getSession());
		Node drafts = JcrUtils.getOrAdd(context, CMS_DRAFTS);
		Node textNode = JcrUtils.getOrAdd(drafts, UUID.randomUUID().toString(),
				CmsTypes.CMS_TEXT);

		new TextDisplay(parent, textNode);
		return null;
	}

}
