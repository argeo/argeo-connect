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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrException;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.utils.AbstractHyperlinkListener;
import org.argeo.connect.demo.gr.ui.utils.GrDoubleClickListener;
import org.argeo.connect.demo.gr.ui.wizards.UploadFileWizard;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Factorizes type independent method for the editor, among others table and
 * documents management
 */
public abstract class AbstractGrEditorPage extends FormPage implements
		GrConstants, GrNames {
	protected final static Log log = LogFactory
			.getLog(AbstractGrEditorPage.class);

	protected GrBackend grBackend;
	private TableViewer documentsTableViewer;

	protected DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
	protected DateFormat timeFormatter = new SimpleDateFormat(DATE_TIME_FORMAT);

	private boolean editable = false;

	public AbstractGrEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
		grBackend = ((AbstractGrEditor) editor).getGrBackend();
		if (grBackend.isUserInRole(GrConstants.ROLE_ADMIN))
			editable = true;

	}

	/* Management of tables whose rows are nodes */
	/** Factorize the creation of table columns */
	protected TableViewerColumn createTableViewerColumn(TableViewer viewer,
			String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/** Specific content provider for tables that display nodes as rows. */
	protected class TableContentProvider implements IStructuredContentProvider {
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return ((java.util.List<Node>) inputElement).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// viewer.refresh();
		}
	}

	/**
	 * provides a label provider that returns the string content of a specific
	 * cell. We assume that all rows of the table are nodes. Specific treatment
	 * is done for some columns.
	 */
	protected ColumnLabelProvider getLabelProvider(final String columnName) {
		if (Property.JCR_LAST_MODIFIED.equals(columnName)
				|| Property.JCR_CREATED.equals(columnName)) {
			return new ColumnLabelProvider() {
				public String getText(Object element) {
					Node node = (Node) element;
					try {
						return timeFormatter.format(node
								.getProperty(columnName).getDate().getTime());
					} catch (RepositoryException e) {
						throw new GrException(
								"Cannot get last modified date for node "
										+ node, e);
					}
				}

				public Image getImage(Object element) {
					return null;
				}
			};
		} else if (Property.JCR_LAST_MODIFIED_BY.equals(columnName)
				|| Property.JCR_CREATED_BY.equals(columnName)) {
			return new ColumnLabelProvider() {
				public String getText(Object element) {
					Node node = (Node) element;
					try {
						return grBackend.getUserDisplayName(node.getProperty(
								columnName).getString());
					} catch (RepositoryException e) {
						throw new GrException(
								"Cannot get last modified user for node "
										+ node, e);
					}
				}

				public Image getImage(Object element) {
					return null;
				}
			};
		} else if (Property.JCR_ID.equals(columnName)) {
			return new ColumnLabelProvider() {
				public String getText(Object element) {
					Node node = (Node) element;
					try {
						return node.getIdentifier();
					} catch (RepositoryException e) {
						throw new GrException("Cannot jcr:id for node " + node,
								e);
					}
				}

				public Image getImage(Object element) {
					return null;
				}
			};
		} else if (Property.JCR_NAME.equals(columnName)) {
			return new ColumnLabelProvider() {
				public String getText(Object element) {
					Node node = (Node) element;
					try {
						return node.getName();
					} catch (RepositoryException e) {
						throw new GrException(
								"Cannot get name of node " + node, e);
					}
				}

				public Image getImage(Object element) {
					return null;
				}
			};
		} else
			return new ColumnLabelProvider() {
				public String getText(Object element) {
					Node node = (Node) element;
					try {
						return node.getProperty(columnName).getString();
					} catch (RepositoryException e) {
						throw new GrException(
								"Cannot get string property for node " + node,
								e);
					}
				}

				public Image getImage(Object element) {
					return null;
				}
			};
	}

	/*
	 * CREATE AND HANDLE DOCUMENT TABLE SECTION
	 */

	/** Factorize the creation of document list sections */
	protected Section createDocumentsTable(Composite parent, FormToolkit tk,
			final Node node) {
		// Section
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(GrMessages.get().editor_docSection_title);
		Composite body = tk.createComposite(section, SWT.WRAP);
		body.setLayout(new GridLayout(1, false));
		section.setClient(body);

		// Create the table containing documents linked to the current node
		final Table table = tk.createTable(body, SWT.NONE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gd = new GridData();
		gd.heightHint = 100;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		table.setLayoutData(gd);

		documentsTableViewer = new TableViewer(table);

		// Document Node UID - invisible but useful for the double click
		TableViewerColumn tvc = createTableViewerColumn(documentsTableViewer,
				"", 0);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_ID));

		// Date updated
		tvc = createTableViewerColumn(documentsTableViewer,
				GrMessages.get().dateLbl, 80);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_CREATED));

		// Updated By
		tvc = createTableViewerColumn(documentsTableViewer,
				GrMessages.get().userNameLbl, 80);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_CREATED_BY));

		// Document name
		tvc = createTableViewerColumn(documentsTableViewer,
				GrMessages.get().docNameLbl, 350);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_NAME));

		documentsTableViewer.setContentProvider(new TableContentProvider());
		getDocumentsTableViewer().addDoubleClickListener(
				new GrDoubleClickListener());

		// Initialize the table input
		refreshDocumentsTable(node);

		// "Upload new Document" hyperlink :
		Hyperlink addNewDocLink = tk.createHyperlink(body,
				GrMessages.get().addDocument_lbl, 0);

		final AbstractFormPart formPart = new SectionPart(section) {
			public void commit(boolean onSave) {
				super.commit(onSave);
			}
		};

		addNewDocLink.addHyperlinkListener(new AbstractHyperlinkListener() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (isEditable()) {

					UploadFileWizard wizard = new UploadFileWizard(node);
					WizardDialog dialog = new WizardDialog(GrUiPlugin
							.getDefault().getWorkbench()
							.getActiveWorkbenchWindow().getShell(), wizard);
					Object returnCode = dialog.open();
					if (returnCode instanceof Integer) {
						int result = ((Integer) returnCode).intValue();
						if (result == 0) {
							formPart.markDirty();
							refreshDocumentsTable(node);
						}
					} else
						// return code type might be plateform dependent
						refreshDocumentsTable(node);
				} else
					MessageDialog.openWarning(e.display.getActiveShell(),
							GrMessages.get().forbiddenAction_title,
							GrMessages.get().forbiddenAction_msg);
			}

		});

		getManagedForm().addPart(formPart);
		return section;
	}

	/**
	 * Request the Repository to get last documents nodes and update the
	 * corresponding table in the current page
	 */
	public void refreshDocumentsTable(Node node) {
		java.util.List<Node> documents = new ArrayList<Node>();
		try {
			NodeIterator ni = node.getNodes();
			while (ni.hasNext()) {
				Node curNode = ni.nextNode();
				if (curNode.getPrimaryNodeType().isNodeType(NodeType.NT_FILE))
					documents.add(curNode);
			}
		} catch (RepositoryException re) {
			throw new GrException("Cannot refresh list of documents node"
					+ " for the current node : " + node, re);
		}
		getDocumentsTableViewer().setInput(documents);
	}

	/** will return null on a page that has no documentsTable */
	protected TableViewer getDocumentsTableViewer() {
		return documentsTableViewer;
	}

	protected String getPropertyString(Node node, String propertyName) {
		String prop = null;
		try {
			prop = node.getProperty(propertyName).getString();
		} catch (PathNotFoundException pnfe) {
			// Property not yet set
			if (log.isTraceEnabled())
				log.warn("Property " + propertyName + " not found");
		} catch (Exception e) {
			throw new GrException("Getting property " + propertyName, e);
		}
		return prop;
	}

	protected String getPropertyCalendarAsString(Node node, String propertyName) {
		try {
			Calendar date = node.getProperty(propertyName).getDate();
			return formatter.format(date.getTime());
		} catch (PathNotFoundException pnfe) {
			// Property not yet set
			if (log.isTraceEnabled())
				log.warn("Property " + propertyName + " not found");
		} catch (Exception e) {
			throw new GrException("Getting property " + propertyName, e);
		}
		return "";
	}

	protected String getPropertyCalendarWithTimeAsString(Node node,
			String propertyName) {
		try {
			Calendar date = node.getProperty(propertyName).getDate();
			return timeFormatter.format(date.getTime());
		} catch (PathNotFoundException pnfe) {
			// Property not yet set
			if (log.isTraceEnabled())
				log.warn("Property " + propertyName + " not found");
		} catch (Exception e) {
			throw new GrException("Getting property " + propertyName, e);
		}
		return "";
	}

	protected Boolean getPropertyBolean(Node node, String propertyName) {
		try {
			return node.getProperty(propertyName).getBoolean();
		} catch (PathNotFoundException pnfe) {
			// Property not yet set
			if (log.isTraceEnabled())
				log.warn("Property " + propertyName + " not found");
		} catch (Exception e) {
			throw new GrException("Getting property " + propertyName, e);
		}
		return false;
	}

	/* Exposes private business objects to children classes */
	protected boolean isEditable() {
		return editable;
	}
}