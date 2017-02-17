package org.argeo.connect.ui;

import javax.jcr.Node;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;

/** Provide interface to manage a connect apps in a RCP/RAP Workbench */
public interface AppUiService {

	/** Centralize icon management for a given app */
	default public Image getIconForType(Node entity) {
		return null;
	}

	/** Creates the correct wizard depending on the type of the given node. */
	default public Wizard getCreationWizard(Node node) {
		return null;
	}

	/* CONFIGURE QUERIES */
	default public boolean lazyLoadLists() {
		return true;
	}

	default public boolean queryWhenTyping() {
		return false;
	}
}
