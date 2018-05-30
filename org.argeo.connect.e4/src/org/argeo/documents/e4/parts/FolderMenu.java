package org.argeo.documents.e4.parts;

import java.util.Date;
import java.util.List;

import org.eclipse.e4.ui.di.AboutToHide;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class FolderMenu {

	@AboutToShow
	public void aboutToShow(List<MMenuElement> items, MApplication app, EModelService modelService) {
		System.out.println("aboutToShow: " + items);
		// MDirectMenuItem dynamicItem = MMenuFactory.INSTANCE.createDirectMenuItem();
		MDirectMenuItem dynamicItem = modelService.createModelElement(MDirectMenuItem.class);
		dynamicItem.setLabel("Dynamic Menu Item (" + new Date() + ")");
		dynamicItem.setContributorURI("platform:/plugin/org.argeo.suite.e4");
		dynamicItem.setContributionURI(
				"bundleclass://org.argeo.connect.e4/org.argeo.documents.e4.parts.TestDirectMenuItem");
		dynamicItem.setIconURI("platform:/plugin/org.argeo.theme.argeo2/icons/types/folder.png");
		dynamicItem.setEnabled(true);
		dynamicItem.setObject(new TestDirectMenuItem());
		items.add(dynamicItem);

		MHandledMenuItem handledItem = modelService.createModelElement(MHandledMenuItem.class);
		handledItem.setContributorURI("platform:/plugin/org.argeo.suite.e4");
		List<MCommand> cmds = modelService.findElements(app, null, MCommand.class, null);
		for (MCommand cmd : cmds) {
			if (cmd.getElementId().equals("org.argeo.suite.e4.command.closeAll")) {
				handledItem.setCommand(cmd);
				System.out.println("set command");
				break;
			}
		}
		items.add(handledItem);
	}

	@AboutToHide
	public void aboutToHide() {

	}
}
