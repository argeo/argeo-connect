package org.argeo.cms.text;

import javax.jcr.Node;

import org.argeo.cms.CmsException;
import org.argeo.cms.widgets.ItemItem;
import org.argeo.cms.widgets.NodeSubTree;
import org.eclipse.swt.widgets.Composite;

public class ScrolledText extends NodeSubTree {
	private static final long serialVersionUID = 9156158156361724525L;

	private EditableTextPart selected;

	public ScrolledText(Composite parent, int style) {
		super(parent, style);
	}

	public synchronized void select(EditableTextPart etp) {
		// TODO check that it is a child
		if (selected != null)
			selected.selected(false);
		selected = etp;
		selected.selected(true);
	}

	public synchronized void select(Node node) {
//		ItemItem i = find(node);
//		if (i == null)
//			throw new CmsException("Could not select " + node
//					
//					+ " because it was not found");
//		
//		if (!(item instanceof EditableTextPart))
//			throw new CmsException(node + " is not an editable text part");
//		select((EditableTextPart) item);
	}

	public synchronized EditableTextPart getSelected() {
		return selected;
	}
}
