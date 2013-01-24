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
package org.argeo.connect.ui.crisis.editors;

import javax.jcr.Node;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.sun.syndication.feed.synd.SyndEntry;

public class LinkEditorInput implements IEditorInput {
	private final String url;
	private final SyndEntry syndEntry;
	private final Node context;

	public LinkEditorInput(Node context, String url) {
		this.context = context;
		this.url = url;
		this.syndEntry = null;
	}

	public LinkEditorInput(Node context, SyndEntry syndEntry) {
		this.context = context;
		this.url = syndEntry.getLink();
		this.syndEntry = syndEntry;
	}

	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return url;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return url;
	}

	public String getUrl() {
		return url;
	}

	public SyndEntry getSyndEntry() {
		return syndEntry;
	}

	public Node getContext() {
		return context;
	}

}
