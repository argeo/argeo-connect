package org.argeo.photo.manager.ui.commands;

import org.argeo.photo.manager.ui.wizards.ImportPicturesWizard;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/** Opens the import pictures wizard. */
public class ImportPictures extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ImportPicturesWizard wizard = new ImportPicturesWizard();
		WizardDialog dialog = new WizardDialog(
				HandlerUtil.getActiveShell(event), wizard);
		dialog.open();
		return null;
	}
}
