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
package org.argeo.connect.demo.gr.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.providers.GrNodeLabelProvider;
import org.argeo.connect.demo.gr.ui.utils.GrDoubleClickListener;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class NetworkDetailsPage extends AbstractGrEditorPage implements GrNames {
	// private final static Log log =
	// LogFactory.getLog(NetworkDetailsPage.class);
	public final static String ID = "grNetworkEditor.networkDetailsPage";

	// This page widgets;
	private TableViewer sitesTableViewer;
	private FormToolkit tk;
	private List<Node> sites;

	// Main business objects
	private Node network;

	public NetworkDetailsPage(FormEditor editor, String title) {
		super(editor, ID, title);
		network = ((NetworkEditor) editor).getNetwork();
	}

	protected void createFormContent(IManagedForm managedForm) {
		try {
			// Initializes business objects
			tk = managedForm.getToolkit();

			// Build current form
			ScrolledForm form = managedForm.getForm();
			TableWrapLayout twt = new TableWrapLayout();
			form.getBody().setLayout(twt);

			createMainSection(form.getBody());
			createDocumentsTable(form.getBody(), tk, network);
			createSitesSection(form.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Section createMainSection(Composite parent) {
		// Network metadata
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(GrMessages.get().networkEditor_mainSection_title);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite body = tk.createComposite(section, SWT.WRAP);
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		body.setLayoutData(twd);
		section.setClient(body);

		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = 1;
		body.setLayout(layout);

		String displayStr = NLS.bind(
				GrMessages.get().lastUpdatedLbl,
				getPropertyCalendarWithTimeAsString(network,
						Property.JCR_LAST_MODIFIED),
				getPropertyString(network, Property.JCR_LAST_MODIFIED_BY));
		tk.createLabel(body, displayStr);
		return section;
	}

	private Section createSitesSection(Composite parent) {
		// Section
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(GrMessages.get().networkEditor_sitesSection_title);

		Composite body = tk.createComposite(section, SWT.WRAP);
		body.setLayout(new GridLayout(1, false));
		section.setClient(body);

		// Create table containing the sites of the current network
		final Table table = tk.createTable(body, SWT.NONE | SWT.H_SCROLL
				| SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		GridData gd = new GridData();
		gd.heightHint = 500;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		// gd.verticalAlignment = SWT.FILL;
		// gd.grabExcessVerticalSpace = true;
		table.setLayoutData(gd);
		sitesTableViewer = new TableViewer(table);

		// UID column - invisible, used for the double click
		TableViewerColumn column = createTableViewerColumn(sitesTableViewer,
				"", 0);
		column.setLabelProvider(getLabelProvider(Property.JCR_ID));

		// site name column
		column = createTableViewerColumn(sitesTableViewer, "", 200);
		column.setLabelProvider(new ColumnLabelProvider() {
			public String getText(Object element) {
				return GrNodeLabelProvider.getName((Node) element);
			}

			public Image getImage(Object element) {
				return GrNodeLabelProvider.getIcon((Node) element);
			}
		});

		// Initialize the table input
		sites = new ArrayList<Node>();
		try {
			String stmt = "SELECT * FROM [" + GrTypes.GR_WATER_SITE
					+ "] WHERE ISDESCENDANTNODE('" + network.getPath() + "')";
			// String stmt = "SELECT site FROM [" + GrTypes.GR_WATER_SITE + "]";
			Query query = network.getSession().getWorkspace().getQueryManager()
					.createQuery(stmt, Query.JCR_SQL2);

			NodeIterator ni = query.execute().getNodes();
			// NodeIterator ni = network.getNodes();
			while (ni.hasNext())
				sites.add(ni.nextNode());
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot initialize the table that lists"
					+ " sites for the current network", re);
		}
		sitesTableViewer.setContentProvider(new TableContentProvider());
		sitesTableViewer.setInput(sites);

		// Add listener
		sitesTableViewer.addDoubleClickListener(new GrDoubleClickListener());

		// "Add new site" hyperlink :
		// FIXME site creation life cycle must be improved
		// Hyperlink addNewSiteLink = tk.createHyperlink(body,
		// GrMessages.get().createSite_lbl, 0);

		final AbstractFormPart formPart = new SectionPart(section) {
			public void commit(boolean onSave) {
				super.commit(onSave);
			}
		};

		// addNewSiteLink.addHyperlinkListener(new AbstractHyperlinkListener() {
		//
		// @Override
		// public void linkActivated(HyperlinkEvent e) {
		// if (grBackend.isUserInRole(ROLE_ADMIN)
		// || grBackend.isUserInRole(ROLE_MANAGER)) {
		// callCreateSiteCommand();
		// formPart.markDirty();
		// } else
		// MessageDialog.openError(GrUiPlugin.getDefault()
		// .getWorkbench().getActiveWorkbenchWindow()
		// .getShell(),
		// GrMessages.get().forbiddenAction_title,
		// GrMessages.get().forbiddenAction_msg);
		// }
		// });

		getManagedForm().addPart(formPart);

		return section;

	}

	// private void callCreateSiteCommand() {
	// String uid;
	// try {
	// uid = network.getIdentifier();
	// } catch (RepositoryException e) {
	// throw new ArgeoException(
	// "JCR Error while getting current network node uid", e);
	// }
	// CommandUtils.CallCommandWithOneParameter(CreateSite.ID,
	// CreateSite.PARAM_UID, uid);
	// }
}
