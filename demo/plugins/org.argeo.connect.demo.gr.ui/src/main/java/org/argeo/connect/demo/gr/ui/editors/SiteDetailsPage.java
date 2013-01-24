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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.GrUtils;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.commands.GenerateSiteReport;
import org.argeo.connect.demo.gr.ui.utils.AbstractHyperlinkListener;
import org.argeo.connect.demo.gr.ui.utils.ModifiedFieldListener;
import org.argeo.connect.demo.gr.ui.utils.ModifiedSelectionListener;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class SiteDetailsPage extends AbstractGrEditorPage implements GrNames {
	// private final static Log log = LogFactory.getLog(SiteDetailsPage.class);

	// local constants
	public final static String ID = "grSiteEditor.siteDetailsPage";
	// public final static String DATE_COLUMN_NAME = "Date";
	// public final static String USER_COLUMN_NAME = "User";
	// public final static String COMMENT_COLUMN_NAME = "Comment";

	private DecimalFormat nf;
	// Main business Objects
	// private Node networkNode;
	private Node siteNode;
	private Node commentsNode;
	private Node mainPointNode;

	// This page widgets
	private FormToolkit tk;
	private Combo siteType;
	private Text wgs84Longitude;
	private Text wgs84Latitude;
	private TableViewer commentsTableViewer;

	public SiteDetailsPage(FormEditor editor, String title) {
		super(editor, ID, title);
		siteNode = ((SiteEditor) editor).getCurrentSite();
		// networkNode = ((SiteEditor) editor).getNetwork();
		// grBackend = getGrBackend();
		// initialize compulsory nodes
		try {
			commentsNode = siteNode.getNode(GR_SITE_COMMENTS);
			mainPointNode = siteNode.getNode(GR_SITE_MAIN_POINT);
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot initialize compulsory nodes ", re);
		}
		nf = (DecimalFormat) DecimalFormat.getInstance();
		nf.applyPattern(GrConstants.NUMBER_FORMAT);
	}

	protected void createFormContent(IManagedForm managedForm) {
		try {
			tk = managedForm.getToolkit();
			ScrolledForm form = managedForm.getForm();
			TableWrapLayout twt = new TableWrapLayout();
			form.getBody().setLayout(twt);
			createMetadataSection(form.getBody());
			createMainPointSection(form.getBody());
			// createDocumentsTable(form.getBody(), tk, siteNode);
			createCommentsSection(form.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Section createMetadataSection(Composite parent) {
		try {
			// Site metadata
			Section section = tk.createSection(parent, Section.TITLE_BAR
					| Section.DESCRIPTION);
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			Composite body = tk.createComposite(section, SWT.WRAP);
			body.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			section.setClient(body);

			// Set title of the section
			section.setText(GrMessages.get().siteEditor_detailPage_title);

			if (siteNode.isNodeType(NodeType.MIX_LAST_MODIFIED))
				section.setDescription(NLS.bind(
						GrMessages.get().lastUpdatedLbl,
						getPropertyCalendarWithTimeAsString(siteNode,
								Property.JCR_LAST_MODIFIED),
						getPropertyString(siteNode,
								Property.JCR_LAST_MODIFIED_BY)));

			// Layout for the body of the section
			GridLayout layout = new GridLayout();
			layout.marginWidth = layout.marginHeight = 0;
			layout.numColumns = 2;
			body.setLayout(layout);

			// Site Type
			Label lbl = new Label(body, SWT.NONE);
			lbl.setText(GrMessages.get().siteTypeLbl);
			GridData gd = new GridData();
			gd.widthHint = 200;
			lbl.setLayoutData(gd);

			siteType = new Combo(body, SWT.FILL);
			gd = new GridData(SWT.FILL);
			gd.widthHint = 100;
			siteType.setLayoutData(gd);

			List<String> siteTypesLst = grBackend.getSiteTypes();
			Iterator<String> it = siteTypesLst.iterator();
			while (it.hasNext()) {
				siteType.add(it.next());
			}

			if (siteNode.hasProperty(GR_SITE_TYPE)) {
				String siteTypeValue = siteNode.getProperty(GR_SITE_TYPE)
						.getString();
				if (siteTypeValue != null && !"".equals(siteTypeValue)
						&& siteType.indexOf(siteTypeValue) > -1) {
					siteType.select(siteType.indexOf(siteTypeValue));
				}
			}

			// Longitude
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrMessages.get().longitudeLbl);

			wgs84Longitude = new Text(body, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
			gd = new GridData();
			gd.widthHint = 100;
			wgs84Longitude.setLayoutData(gd);
			wgs84Longitude.setEnabled(true);

			if (mainPointNode.hasProperty(GR_WGS84_LONGITUDE)) {
				String value = nf.format(mainPointNode.getProperty(
						GR_WGS84_LONGITUDE).getDouble());

				wgs84Longitude.setText(value);
			}

			// Latitude
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrMessages.get().latitudeLbl);

			wgs84Latitude = new Text(body, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
			wgs84Latitude.setEnabled(true);
			gd = new GridData();
			gd.widthHint = 100;
			wgs84Latitude.setLayoutData(gd);

			if (mainPointNode.hasProperty(GR_WGS84_LATITUDE)) {
				String value = nf.format(mainPointNode.getProperty(
						GR_WGS84_LATITUDE).getDouble());
				wgs84Latitude.setText(value);
			}

			AbstractFormPart part = new SectionPart(section) {
				public void commit(boolean onSave) {
					if (onSave) {
						try {
							int index = siteType.getSelectionIndex();
							if (index > -1)

								siteNode.setProperty(GR_SITE_TYPE,
										siteType.getItem(index));
							// Gps coordinate
							String tmpStr = wgs84Longitude.getText();
							if (tmpStr != null && !"".equals(tmpStr)) {
								mainPointNode.setProperty(GR_WGS84_LONGITUDE,
										tmpStr);
							}

							tmpStr = wgs84Latitude.getText();
							if (tmpStr != null && !"".equals(tmpStr)) {
								mainPointNode.setProperty(GR_WGS84_LATITUDE,
										tmpStr);
							}
							// Geometry
							GrUtils.syncPointGeometry(mainPointNode);
						} catch (RepositoryException re) {
							throw new ArgeoException(
									"Jcr Error while saving site matadata", re);
						}

					}
					super.commit(onSave);
				}
			};

			// Manage authorizations
			siteType.setEnabled(isEditable());
			wgs84Longitude.setEditable(isEditable());
			wgs84Latitude.setEditable(isEditable());

			siteType.addSelectionListener(new ModifiedSelectionListener(part));
			wgs84Longitude.addModifyListener(new ModifiedFieldListener(part));
			wgs84Latitude.addModifyListener(new ModifiedFieldListener(part));

			getManagedForm().addPart(part);

			// Generate report link
			Hyperlink generateReportLink = tk.createHyperlink(body,
					GrMessages.get().generateSiteReport_lbl, 0);
			gd = new GridData();
			gd.horizontalSpan = 2;
			generateReportLink.setLayoutData(gd);

			generateReportLink
					.addHyperlinkListener(new AbstractHyperlinkListener() {

						@Override
						public void linkActivated(HyperlinkEvent e) {
							try {
								CommandUtils.CallCommandWithOneParameter(
										GenerateSiteReport.ID,
										GenerateSiteReport.PARAM_UID,
										siteNode.getIdentifier());
							} catch (RepositoryException re) {
								throw new ArgeoException(
										"Error while getting current site node id",
										re);
							}

						}

					});
			return section;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Error during creation of network details section", re);
		}

	}

	private Section createMainPointSection(Composite parent) {
		try {

			mainPointNode = siteNode.getNode(GR_SITE_MAIN_POINT);

			// Site metadata
			Section section = tk.createSection(parent, Section.TITLE_BAR);
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			Composite body = tk.createComposite(section, SWT.WRAP);
			body.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			section.setClient(body);

			// Set title of the section
			StringBuffer sbuf = new StringBuffer();
			sbuf.append(GrMessages.get().siteEditor_lastUpdatedInfoSection_title);
			section.setText(sbuf.toString());

			// Layout for the body of the section
			GridLayout layout = new GridLayout();
			layout.marginWidth = layout.marginHeight = 0;
			layout.numColumns = 2;
			body.setLayout(layout);

			Label lbl;

			// Water level
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrMessages.get().waterLevelLbl);
			GridData gd = new GridData();
			gd.widthHint = 200;
			lbl.setLayoutData(gd);

			final Text waterLevelTxt = new Text(body, SWT.BORDER | SWT.SINGLE
					| SWT.RIGHT);
			gd = new GridData(SWT.FILL);
			gd.widthHint = 100;
			waterLevelTxt.setLayoutData(gd);

			if (siteNode.hasProperty(GR_WATER_LEVEL)) {
				String value = nf.format(siteNode.getProperty(GR_WATER_LEVEL)
						.getDouble());
				waterLevelTxt.setText(value);
			}

			// Withdrawn water
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrMessages.get().withdrawnWaterLbl);

			final Text withdrawnWaterTxt = new Text(body, SWT.BORDER
					| SWT.SINGLE | SWT.RIGHT);
			gd = new GridData(SWT.FILL);
			gd.widthHint = 100;
			withdrawnWaterTxt.setLayoutData(gd);

			if (siteNode.hasProperty(GR_WITHDRAWN_WATER)) {
				String value = nf.format(siteNode.getProperty(
						GR_WITHDRAWN_WATER).getDouble());
				withdrawnWaterTxt.setText(value);
			}

			// E-Coli rate
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrMessages.get().eColiRateLbl);

			final Text eColiRateTxt = new Text(body, SWT.BORDER | SWT.SINGLE
					| SWT.RIGHT);
			gd = new GridData(SWT.FILL);
			gd.widthHint = 100;
			eColiRateTxt.setLayoutData(gd);

			if (siteNode.hasProperty(GR_ECOLI_RATE)) {
				String value = nf.format(siteNode.getProperty(GR_ECOLI_RATE)
						.getDouble());
				eColiRateTxt.setText(value);
			}

			AbstractFormPart part = new SectionPart(section) {
				public void commit(boolean onSave) {
					if (onSave) {
						try {
							// Waterlevel
							String tmpStr = waterLevelTxt.getText();
							if (tmpStr != null && !"".equals(tmpStr)) {
								mainPointNode.setProperty(GR_WATER_LEVEL,
										tmpStr);
							}
							// Ecoli rate
							tmpStr = eColiRateTxt.getText();
							if (tmpStr != null && !"".equals(tmpStr)) {
								mainPointNode
										.setProperty(GR_ECOLI_RATE, tmpStr);
							}
							// Withdrawn water
							tmpStr = withdrawnWaterTxt.getText();
							if (tmpStr != null && !"".equals(tmpStr)) {
								mainPointNode.setProperty(GR_WITHDRAWN_WATER,
										tmpStr);
							}
						} catch (RepositoryException re) {
							throw new ArgeoException(
									"Jcr Error while saving site matadata", re);
						}

					}
					super.commit(onSave);
				}
			};

			waterLevelTxt.setEditable(isEditable());
			withdrawnWaterTxt.setEditable(isEditable());
			eColiRateTxt.setEditable(isEditable());

			waterLevelTxt.addModifyListener(new ModifiedFieldListener(part));
			withdrawnWaterTxt
					.addModifyListener(new ModifiedFieldListener(part));
			eColiRateTxt.addModifyListener(new ModifiedFieldListener(part));

			getManagedForm().addPart(part);

			return section;
		} catch (RepositoryException re) {
			throw new ArgeoException("JCR Error in the main point section", re);
		}
	}

	/*
	 * COMMENTS TABLE MANAGEMENT
	 */
	private Section createCommentsSection(Composite parent) {
		// Section
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(GrMessages.get().siteEditor_commentsSection_title);
		Composite body = tk.createComposite(section, SWT.WRAP);
		body.setLayout(new GridLayout(1, false));
		section.setClient(body);

		// "Add new comment" hyperlink :
		Hyperlink addNewCommentLink = tk.createHyperlink(body,
				GrMessages.get().addComment_lbl, 0);

		final AbstractFormPart formPart = new SectionPart(section) {
			public void commit(boolean onSave) {
				super.commit(onSave);
			}
		};

		addNewCommentLink.addHyperlinkListener(new AbstractHyperlinkListener() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (isEditable()) {
					boolean success = addNewComment();
					if (success) {
						formPart.markDirty();
						refreshCommentsTable();
					}
				} else
					MessageDialog.openWarning(e.display.getActiveShell(),
							GrMessages.get().forbiddenAction_title,
							GrMessages.get().forbiddenAction_msg);
			}
		});

		getManagedForm().addPart(formPart);

		// Create the table containing the comments about current site
		final Table table = tk.createTable(body, SWT.NONE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gd = new GridData();
		gd.heightHint = 250;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);

		commentsTableViewer = new TableViewer(table);

		// Date updated
		TableViewerColumn tvc = createTableViewerColumn(commentsTableViewer,
				GrMessages.get().dateLbl, 80);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_LAST_MODIFIED));

		// Date updated
		tvc = createTableViewerColumn(commentsTableViewer,
				GrMessages.get().userNameLbl, 80);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_LAST_MODIFIED_BY));

		// comment content
		tvc = createTableViewerColumn(commentsTableViewer,
				GrMessages.get().commentTxtLbl, 350);
		tvc.setLabelProvider(getLabelProvider(GR_COMMENT_CONTENT));

		commentsTableViewer.setContentProvider(new TableContentProvider());

		// Initialize the table input
		refreshCommentsTable();

		return section;
	}

	/**
	 * Request the Repository to get last comments nodes and update the
	 * corresponding table in the SiteDetailPage
	 */
	public void refreshCommentsTable() {
		List<Node> comments = new ArrayList<Node>();
		try {
			NodeIterator ni = siteNode.getNode(GR_SITE_COMMENTS).getNodes();
			while (ni.hasNext()) {
				Node node = ni.nextNode();
				if (node.getPrimaryNodeType().isNodeType(GrTypes.GR_COMMENT))
					comments.add(node);
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot initialize the table that lists"
					+ " comments for the current site", re);
		}
		commentsTableViewer.setInput(comments);
	}

	private boolean addNewComment() {

		NewCommentDialog idiag = new NewCommentDialog(getSite().getShell(),
				GrMessages.get().dialog_createComment_title,
				GrMessages.get().dialog_createComment_msg, "", null);

		int result = idiag.open();
		String commentString = idiag.getValue();
		idiag.close();

		if (result == Window.OK
				&& !(commentString == null || "".equals(commentString))) {
			try {
				Node commentNode = commentsNode.addNode("gr:comment",
						GrTypes.GR_COMMENT);
				commentNode.setProperty(GR_COMMENT_CONTENT, commentString);
				JcrUtils.updateLastModified(commentNode);
			} catch (RepositoryException re) {
				throw new ArgeoException("Cannot add comment node", re);
			}
			return true;
		} else
			return false;

	}

	private class NewCommentDialog extends InputDialog {

		public NewCommentDialog(Shell parentShell, String dialogTitle,
				String dialogMessage, String initialValue,
				IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue,
					validator);
		}

		// To change default Text input widget height style
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			GridData gd = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.HORIZONTAL_ALIGN_FILL);
			gd.heightHint = 100;
			getText().setLayoutData(gd);
			return composite;
		}

		// Enable multiple line InputText
		@Override
		protected int getInputTextStyle() {
			return SWT.MULTI | SWT.BORDER;
		}
	}
}
