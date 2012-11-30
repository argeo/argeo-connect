/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.demo.gr.ui.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.demo.gr.GrTypes;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.forms.AbstractFormPart;

/** Listen to modified selections. */
public class ModifiedSelectionListener implements SelectionListener, GrTypes {
	private final static Log log = LogFactory
			.getLog(ModifiedSelectionListener.class);
	private AbstractFormPart formPart;

	public ModifiedSelectionListener(AbstractFormPart generalPart) {
		this.formPart = generalPart;
	}

	public void widgetSelected(SelectionEvent e) {

		if (e.getSource() instanceof Combo) {
			formPart.markDirty();
		} else if (log.isWarnEnabled())
			log.warn("Unimplemented listener for widget of class: "
					+ e.getSource().getClass());
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}
}
