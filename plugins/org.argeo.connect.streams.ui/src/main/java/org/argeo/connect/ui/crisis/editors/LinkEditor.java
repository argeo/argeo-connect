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

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.eclipse.ui.Error;
import org.argeo.gis.GisNames;
import org.argeo.gis.GisTypes;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.ArgeoTypes;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

import com.sun.syndication.feed.synd.SyndEntry;

public class LinkEditor extends FormEditor {
	private String linksBasePath = "/connect/links";

	private Node context;

	private LinkBrowserPage linkBrowserPage;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		LinkEditorInput lei = (LinkEditorInput) getEditorInput();
		setPartName(lei.getUrl());
	}

	@Override
	protected void addPages() {
		try {
			LinkEditorInput lei = (LinkEditorInput) getEditorInput();
			context = lei.getContext();
			linkBrowserPage = new LinkBrowserPage(this, "browser", "Browser",
					lei);
			addPage(linkBrowserPage);
		} catch (PartInitException e) {
			Error.show("Cannot initialize editor", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doSave(IProgressMonitor monitor) {
		LinkEditorInput lei = (LinkEditorInput) getEditorInput();
		String url = linkBrowserPage.getUrl();
		try {
			Session session = context.getSession();

			Node linkNode;
			String linkPath = linksBasePath + '/' + JcrUtils.urlAsPath(url);
			if (session.itemExists(linkPath)
					&& session.getNode(linkPath)
							.getProperty(ArgeoNames.ARGEO_URI).equals(url)) {
				linkNode = session.getNode(linkPath);
			}
			// syndication entry
			else if (lei.getSyndEntry() != null) {
				SyndEntry entry = lei.getSyndEntry();
				Calendar publishedDate = new GregorianCalendar();
				publishedDate.setTime(entry.getPublishedDate());
				linkNode = JcrUtils.mkdirs(session, linkPath);
				linkNode.addMixin(ConnectTypes.CONNECT_SYND_ENTRY);
				linkNode.setProperty(ArgeoNames.ARGEO_URI, url);
				linkNode.setProperty(Property.JCR_TITLE, entry.getTitle());
				linkNode.setProperty(Property.JCR_DESCRIPTION, entry
						.getDescription().getValue());
				linkNode.setProperty(
						ConnectNames.CONNECT_AUTHOR,
						(String[]) entry.getAuthors().toArray(
								new String[entry.getAuthors().size()]));
				linkNode.setProperty(ConnectNames.CONNECT_PUBLISHED_DATE,
						publishedDate);
				linkNode.setProperty(ConnectNames.CONNECT_UPDATED_DATE,
						publishedDate);
			}
			// raw link
			else {
				linkNode = JcrUtils.mkdirs(session, linkPath);
				linkNode.addMixin(ArgeoTypes.ARGEO_LINK);
				linkNode.setProperty(ArgeoNames.ARGEO_URI, url);
			}
			linkBrowserPage.doSave(monitor);

			linkNode.getSession().save();
			context = linkNode;
			firePropertyChange(PROP_DIRTY);

		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot save link " + url, e);
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
}
