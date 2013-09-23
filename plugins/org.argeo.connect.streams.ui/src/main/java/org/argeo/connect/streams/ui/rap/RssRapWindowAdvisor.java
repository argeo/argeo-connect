/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.connect.streams.ui.rap;

import org.argeo.connect.people.ui.PeopleUiPlugin;
import org.argeo.connect.people.ui.editors.SearchEntityEditorInput;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.ui.editors.RssSearchPostEditor;
import org.argeo.security.ui.rap.RapWindowAdvisor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;

/**
 * Extends the default advisor to access pre window open method and remove cool
 * bar
 */
public class RssRapWindowAdvisor extends RapWindowAdvisor {

	private String username;

	public RssRapWindowAdvisor(IWorkbenchWindowConfigurer configurer,
			String username) {
		super(configurer, username);
		this.username = username;
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new RssRapActionBarAdvisor(configurer, username);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setShowCoolBar(false);
		configurer.setShowMenuBar(false);
		configurer.setShowStatusLine(false);
		configurer.setShowPerspectiveBar(false);
		// Full screen, see
		// http://wiki.eclipse.org/RAP/FAQ#How_to_create_a_fullscreen_application
		configurer.setShellStyle(SWT.NO_TRIM);
		Rectangle bounds = Display.getCurrent().getBounds();
		configurer.setInitialSize(new Point(bounds.width, bounds.height));
	}

	@Override
	public void postWindowOpen() {
		super.postWindowOpen();
		try {
			SearchEntityEditorInput eei = new SearchEntityEditorInput(
					RssTypes.RSS_ITEM);
			PeopleUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.openEditor(eei, RssSearchPostEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
			// Silently fail
		}
	}
}
