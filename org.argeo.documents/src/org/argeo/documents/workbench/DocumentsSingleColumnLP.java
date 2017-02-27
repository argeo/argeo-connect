package org.argeo.documents.workbench;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.connect.ui.ConnectUiUtils;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.documents.DocumentsException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Provide a single column label provider for file and directory lists. Icon and
 * displayed text vary with the element node type
 */
public class DocumentsSingleColumnLP extends LabelProvider {
	private static final long serialVersionUID = 4514040809538981909L;

	private AppWorkbenchService appWbService;

	public DocumentsSingleColumnLP(AppWorkbenchService appWbService) {
		this.appWbService = appWbService;
	}

	@Override
	public String getText(Object element) {
		try {
			Node entity = (Node) element;
			String result;
			if (entity.isNodeType(NodeType.NT_FILE))
				result = entity.getName();
			// result = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			else if (entity.isNodeType(NodeType.NT_FOLDER))
				result = entity.getName();
			// result = ConnectJcrUtils.get(entity, Property.JCR_TITLE);
			else
				result = "";
			return ConnectUiUtils.replaceAmpersand(result);
		} catch (RepositoryException re) {
			throw new DocumentsException("Unable to get formatted value for node", re);
		}
	}

	/** Overwrite this method to provide project specific images */
	@Override
	public Image getImage(Object element) {
		return appWbService.getIconForType((Node) element);
	}

}
