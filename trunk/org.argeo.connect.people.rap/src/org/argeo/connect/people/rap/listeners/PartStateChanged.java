package org.argeo.connect.people.rap.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.CmsEditable;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.editors.util.IVersionedItemEditor;
import org.argeo.connect.people.rap.util.EditionSourceProvider;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Manage enable state of the CheckOut command depending on the active part.
 * DRAFT we also use this to insure a correct people service has been attached
 * to the display
 */
public class PartStateChanged implements IPartListener, IStartup {
	private final static Log log = LogFactory.getLog(PartStateChanged.class);
	IContextActivation contextActivation;

	private PeopleService peopleService;

	@Override
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchPage iwp = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					if (iwp != null)
						iwp.addPartListener(new PartStateChanged());
					Display.getCurrent().setData(
							PeopleRapConstants.KEY_PEOPLE_SERVICE,
							peopleService);
				} catch (Exception e) {
					throw new PeopleException(
							"Error while registering the PartStateChangedListener",
							e);
				}
			}
		});
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (log.isTraceEnabled())
			log.trace("Part activated: " + part.getTitle() + " - "
					+ part.getClass());

		// Try to avoid NPE when closing the application
		IWorkbenchPartSite site = part.getSite();
		if (site == null)
			return;
		IWorkbenchWindow window = site.getWorkbenchWindow();
		if (window == null)
			return;

		// Retrieve the service to enable/disable checkout button
		ISourceProviderService sourceProviderService = (ISourceProviderService) window
				.getService(ISourceProviderService.class);
		EditionSourceProvider esp = (EditionSourceProvider) sourceProviderService
				.getSourceProvider(EditionSourceProvider.EDITING_STATE);
		if (part instanceof IVersionedItemEditor) {
			IStatusLineManager manager = ((IEditorPart) part).getEditorSite()
					.getActionBars().getStatusLineManager();
			manager.setMessage(((IVersionedItemEditor) part)
					.getlastUpdateMessage());

		}
		if (part instanceof CmsEditable) {
			CmsEditable editor = (CmsEditable) part;

			// Processing the ability to checkout is delegated to the editor
			esp.setCurrentItemEditingState(editor.canEdit(), editor.isEditing());
		} else {// force button to be disabled if another part has the focus.
			esp.setCurrentItemEditingState(false, true);
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// Nothing to do
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		// Nothing to do
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// Always remove checkOut button when the editor is left.
		if (part instanceof IVersionedItemEditor) {
			// Try to avoid NPE when closing the application
			IWorkbenchPartSite site = part.getSite();
			if (site == null)
				return;
			IWorkbenchWindow window = site.getWorkbenchWindow();
			if (window == null)
				return;

			// Retrieve the service to enable/disable checkout button
			ISourceProviderService sourceProviderService = (ISourceProviderService) window
					.getService(ISourceProviderService.class);
			EditionSourceProvider esp = (EditionSourceProvider) sourceProviderService
					.getSourceProvider(EditionSourceProvider.EDITING_STATE);
			esp.setCurrentItemEditingState(false, true);
		}
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

	/* DEPENDENCY INJECTION */
	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}