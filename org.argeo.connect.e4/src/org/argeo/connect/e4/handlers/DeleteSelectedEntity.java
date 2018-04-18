package org.argeo.connect.e4.handlers;

import java.util.List;

import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.ConnectException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class DeleteSelectedEntity {

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part, ESelectionService selectionService) {
		try {
			List<?> selection = (List<?>) selectionService.getSelection();
			if (selection == null)
				return;
			// confirmation
			StringBuffer buf = new StringBuffer("");
			for (Object o : selection) {
				if (o instanceof Node) {
					Node sjn = (Node) o;
					buf.append(sjn.getName()).append(' ');
				}
			}
			Boolean doRemove = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm deletion",
					"Do you want to delete " + buf + "?");

			if (doRemove) {
				for (Object obj : selection) {
					if (obj instanceof Node) {
						Node node = (Node) obj;
						node.remove();
						node.getSession().save();
					}
				}
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot delete entities", e);
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart part, ESelectionService selectionService) {
		return selectionService.getSelection() != null;
	}

}
