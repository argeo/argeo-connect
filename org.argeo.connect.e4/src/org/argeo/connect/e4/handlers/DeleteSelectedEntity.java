package org.argeo.connect.e4.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.cms.ui.dialogs.CmsMessageDialog;
import org.argeo.connect.ConnectException;
import org.argeo.connect.e4.ConnectE4Constants;
import org.argeo.connect.e4.ConnectE4Msg;
import org.argeo.connect.e4.parts.AbstractConnectEditor;
import org.argeo.jcr.JcrUtils;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;

public class DeleteSelectedEntity {
	@Inject
	EPartService partService;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_PART) MPart part, ESelectionService selectionService) {
		Session newSession = null;
		try {
			@SuppressWarnings("unchecked")
			List<Object> selection = (List<Object>) selectionService.getSelection();
			if (selection == null) {
				if (part.getObject() instanceof AbstractConnectEditor) {
					Node entity = ((AbstractConnectEditor) part.getObject()).getNode();
					newSession = entity.getSession().getRepository().login();
					Node newNode = newSession.getNodeByIdentifier(entity.getIdentifier());
					selection = new ArrayList<>();
					selection.add(newNode);
				} else
					return;
			}
			// confirmation
			List<String> idsToDelete = new ArrayList<>();
			StringBuffer buf = new StringBuffer("");
			for (int i = 0; i < selection.size(); i++) {
				if (selection.get(i) instanceof Node) {
					Node sjn = (Node) selection.get(i);
					idsToDelete.add(sjn.getIdentifier());
					String name;
					if (sjn.hasProperty(Property.JCR_TITLE)) {
						name = sjn.getProperty(Property.JCR_TITLE).getString();
					} else {
						name = sjn.getName();
					}
					if (i != 0)
						buf.append(", ");
					buf.append('\'').append(name).append('\'');
				}
			}
			// Boolean doRemove =
			// MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Confirm
			// deletion",
			// "Do you want to delete " + buf + "?");
			String msg = ConnectE4Msg.confirmEntityDeletion.format(new String[] { buf.toString() });
			Boolean doRemove = CmsMessageDialog.openConfirm(msg);

			if (doRemove) {
				closeEditors(idsToDelete);
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
		} finally {
			JcrUtils.logoutQuietly(newSession);
		}
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) MPart part, ESelectionService selectionService) {
		return selectionService.getSelection() != null || part.getObject() instanceof AbstractConnectEditor;
	}

	private void closeEditors(List<String> idsToDelete) {
		for (MPart part : partService.getParts()) {
			String id = part.getPersistedState().get(ConnectE4Constants.ENTITY_ID);
			if (idsToDelete.contains(id)) {
				partService.hidePart(part, true);
			}
		}

	}
}
