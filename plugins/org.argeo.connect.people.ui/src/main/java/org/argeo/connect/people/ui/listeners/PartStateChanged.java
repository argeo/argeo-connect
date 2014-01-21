package org.argeo.connect.people.ui.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.editors.utils.IVersionedItemEditor;
import org.argeo.connect.people.ui.utils.CheckoutSourceProvider;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Enable/disable CheckOut command depending on the active part.
 * 
 */
public class PartStateChanged implements IPartListener, IStartup {
	private final static Log log = LogFactory.getLog(PartStateChanged.class);
	IContextActivation contextActivation;

	@Override
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage()
							.addPartListener(new PartStateChanged());
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

		IWorkbenchWindow window = part.getSite().getWorkbenchWindow();

		// Get the service to enable / disable checkout button
		ISourceProviderService sourceProviderService = (ISourceProviderService) window
				.getService(ISourceProviderService.class);

		// Now get my service
		CheckoutSourceProvider esp = (CheckoutSourceProvider) sourceProviderService
				.getSourceProvider(CheckoutSourceProvider.CHECKOUT_STATE);
		if (part instanceof IVersionedItemEditor) {
			IVersionedItemEditor editor = (IVersionedItemEditor) part;
			// Processing the ability to checkout is delegated to the editor
			boolean canBeCheckoutedByMe = editor.canBeCheckedOutByMe();
			esp.setIsCurrentItemCheckedOut(!canBeCheckoutedByMe);

			IStatusLineManager manager = ((IEditorPart) editor).getEditorSite()
					.getActionBars().getStatusLineManager();
			manager.setMessage("Information for the status line");

		} else {// force button to be disabled if another part has the focus.
			esp.setIsCurrentItemCheckedOut(true);
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
		// if (log.isTraceEnabled())
		// log.trace("part deactivated " + part.getTitle() + " - "
		// + part.getClass());
		// we always remove checkOut button when the editor is left.
		if (part instanceof IVersionedItemEditor) {
			// TODO : enhence that to avoid npe when closing the application :
			IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
			// Get the service to enable / disable checkout button
			ISourceProviderService sourceProviderService = (ISourceProviderService) window
					.getService(ISourceProviderService.class);
			// Now get my service
			CheckoutSourceProvider esp = (CheckoutSourceProvider) sourceProviderService
					.getSourceProvider(CheckoutSourceProvider.CHECKOUT_STATE);
			esp.setIsCurrentItemCheckedOut(true);
		}
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}
}