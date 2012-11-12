package org.argeo.connect.demo.gr.ui.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.GrUtils;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.commands.GenerateSiteReport;
import org.argeo.connect.demo.gr.ui.utils.AbstractHyperlinkListener;
import org.argeo.connect.demo.gr.ui.utils.CommandUtils;
import org.argeo.connect.demo.gr.ui.utils.ModifiedFieldListener;
import org.argeo.connect.demo.gr.ui.utils.ModifiedSelectionListener;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
	private final static Log log = LogFactory.getLog(SiteDetailsPage.class);

	// local constants
	public final static String ID = "grSiteEditor.siteDetailsPage";
	public final static String DATE_COLUMN_NAME = "Date";
	public final static String USER_COLUMN_NAME = "User";
	public final static String COMMENT_COLUMN_NAME = "Comment";

	// for internationalized messages
	private final static String MSG_PRE = "grSiteEditorSiteDetailsPage";

	// Main business Objects
	private Node networkNode;
	private Node siteNode;
	private Node commentsNode;
	private Node mainPointNode;
	private GrBackend grBackend;

	// This page widgets
	private FormToolkit tk;
	private Combo siteType;
	private Text pointName;
	private Text wgs84Longitude;
	private Text wgs84Latitude;
	private TableViewer commentsTableViewer;

	public SiteDetailsPage(FormEditor editor, String title) {
		super(editor, ID, title);
		siteNode = ((SiteEditor) editor).getCurrentSite();
		networkNode = ((SiteEditor) editor).getNetwork();
		grBackend = getGrBackend();
		// initialize compulsory nodes
		try {
			commentsNode = siteNode.getNode(GR_SITE_COMMENTS);
			mainPointNode = siteNode.getNode(GR_SITE_MAIN_POINT);
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot initialize compulsory nodes ", re);
		}
	}

	protected void createFormContent(IManagedForm managedForm) {
		try {
			tk = managedForm.getToolkit();
			ScrolledForm form = managedForm.getForm();
			TableWrapLayout twt = new TableWrapLayout();
			form.getBody().setLayout(twt);
			createMetadataSection(form.getBody());
			createMainPointSection(form.getBody());
			createDocumentsTable(form.getBody(), tk, siteNode);
			createCommentsTable(form.getBody());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Section createMetadataSection(Composite parent) {
		try {
			// Site metadata
			Section section = tk.createSection(parent, Section.TITLE_BAR);
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			Composite body = tk.createComposite(section, SWT.WRAP);
			body.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			section.setClient(body);

			// Set title of the section
			StringBuffer sbuf = new StringBuffer();
			sbuf.append(GrUiPlugin.getMessage(MSG_PRE + "DataSectionTitlePre"));
			sbuf.append(" ");
			sbuf.append(siteNode.getName());
			sbuf.append(" ");
			sbuf.append(GrUiPlugin.getMessage(MSG_PRE + "DataSectionTitlePost"));
			sbuf.append(networkNode.getName());

			if (log.isTraceEnabled()) {
				log.trace("Full section title: " + sbuf.toString());
			}
			section.setText(sbuf.toString());

			// Layout for the body of the section
			GridLayout layout = new GridLayout();
			layout.marginWidth = layout.marginHeight = 0;
			layout.numColumns = 2;
			body.setLayout(layout);

			// Site Type
			Label lbl = new Label(body, SWT.NONE);
			lbl.setText(GrUiPlugin.getMessage("siteTypeLbl"));

			siteType = new Combo(body, SWT.NONE);
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
			AbstractFormPart part = new SectionPart(section) {
				public void commit(boolean onSave) {
					if (onSave) {
						if (log.isDebugEnabled())
							log.debug("Save here site type");
						int index = siteType.getSelectionIndex();
						if (index > -1)
							try {
								siteNode.setProperty(GR_SITE_TYPE,
										siteType.getItem(index));
							} catch (RepositoryException re) {
								throw new ArgeoException(
										"Jcr Error while saving site matadata",
										re);
							}
					}
					super.commit(onSave);
				}
			};

			siteType.addSelectionListener(new ModifiedSelectionListener(part));
			getManagedForm().addPart(part);

			// Generate report link
			Hyperlink generateReportLink = tk.createHyperlink(body,
					GrUiPlugin.getMessage("generateSiteReportLbl"), 0);

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

	// TODO: implement handling of more than one point by site
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
			sbuf.append(GrUiPlugin
					.getMessage(MSG_PRE + "MainPointSectionTitle"));
			section.setText(sbuf.toString());

			// Layout for the body of the section
			GridLayout layout = new GridLayout();
			layout.marginWidth = layout.marginHeight = 0;
			layout.numColumns = 2;
			body.setLayout(layout);

			Label lbl;

			// Point name
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrUiPlugin.getMessage("pointNameLbl"));

			pointName = new Text(body, SWT.BORDER | SWT.SINGLE);
			pointName.setText(mainPointNode.getName());

			// read only for the time being, changing the name of a node can be
			// tricky and is not in the scope of current prototype
			// See
			// http://stackoverflow.com/questions/4164995/jcr-node-how-to-changing-name
			pointName.setEnabled(false);

			// Point Type
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrUiPlugin.getMessage("pointTypeLbl"));

			// pointType = new Combo(body, SWT.NONE);
			// List<String> pointTypesLst = grBackend.getPointTypes();
			// Iterator<String> it = pointTypesLst.iterator();
			// while (it.hasNext()) {
			// pointType.add(it.next());
			// }
			//
			// if (mainPointNode.hasProperty(GR_POINT_TYPE)) {
			// String pointTypeValue = mainPointNode
			// .getProperty(GR_POINT_TYPE).getString();
			// if (pointTypeValue != null && !"".equals(pointTypeValue)
			// && pointType.indexOf(pointTypeValue) > -1) {
			// pointType.select(pointType.indexOf(pointTypeValue));
			// }
			// }

			// Longitude
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrUiPlugin.getMessage("wgs84LongitudeLbl"));

			wgs84Longitude = new Text(body, SWT.BORDER | SWT.SINGLE);
			wgs84Longitude.setEnabled(true);

			if (mainPointNode.hasProperty(GR_WGS84_LONGITUDE)) {
				String value = mainPointNode.getProperty(GR_WGS84_LONGITUDE)
						.getString();
				wgs84Longitude.setText(value);
			}

			// Latitude
			lbl = new Label(body, SWT.NONE);
			lbl.setText(GrUiPlugin.getMessage("wgs84LatitudeLbl"));

			wgs84Latitude = new Text(body, SWT.BORDER | SWT.SINGLE);
			wgs84Latitude.setEnabled(true);

			if (mainPointNode.hasProperty(GR_WGS84_LATITUDE)) {
				String value = mainPointNode.getProperty(GR_WGS84_LATITUDE)
						.getString();
				wgs84Latitude.setText(value);
			}

			AbstractFormPart part = new SectionPart(section) {
				public void commit(boolean onSave) {
					if (onSave) {
						try {
							// Point Type
							// int index = pointType.getSelectionIndex();
							// if (index > -1)
							// mainPointNode.setProperty(GR_POINT_TYPE,
							// pointType.getItem(index));

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
									"Jcr Error while saving point", re);

						}
					}
					super.commit(onSave);
				}
			};

			// pointType.addSelectionListener(new
			// ModifiedSelectionListener(part));
			wgs84Longitude.addModifyListener(new ModifiedFieldListener(part));
			wgs84Latitude.addModifyListener(new ModifiedFieldListener(part));
			getManagedForm().addPart(part);

			return section;
		} catch (RepositoryException re) {
			throw new ArgeoException("JCR Error in the main point section", re);
		}
	}

	/*
	 * COMMENTS TABLE MANAGEMENT
	 */
	private Section createCommentsTable(Composite parent) {
		// Section
		Section section = tk.createSection(parent, Section.TITLE_BAR);
		section.setText(GrUiPlugin.getMessage(MSG_PRE + "CommentsTableTitle"));
		Composite body = tk.createComposite(section, SWT.WRAP);
		body.setLayout(new GridLayout(1, false));
		section.setClient(body);

		// Create the table containing the comments about current site
		final Table table = tk.createTable(body, SWT.NONE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridData gd = new GridData();
		gd.heightHint = 100;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		table.setLayoutData(gd);

		commentsTableViewer = new TableViewer(table);

		// Date updated
		TableViewerColumn tvc = createTableViewerColumn(commentsTableViewer,
				GrUiPlugin.getMessage("date"), 80);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_LAST_MODIFIED));

		// Date updated
		tvc = createTableViewerColumn(commentsTableViewer,
				GrUiPlugin.getMessage("userNameLbl"), 80);
		tvc.setLabelProvider(getLabelProvider(Property.JCR_LAST_MODIFIED_BY));

		// comment content
		tvc = createTableViewerColumn(commentsTableViewer,
				GrUiPlugin.getMessage("commentContent"), 350);
		tvc.setLabelProvider(getLabelProvider(GR_COMMENT_CONTENT));

		commentsTableViewer.setContentProvider(new TableContentProvider());

		// Initialize the table input
		refreshCommentsTable();

		// "Add new comment" hyperlink :
		Hyperlink addNewCommentLink = tk.createHyperlink(body,
				GrUiPlugin.getMessage("createNewCommentLbl"), 0);

		final AbstractFormPart formPart = new SectionPart(section) {
			public void commit(boolean onSave) {
				super.commit(onSave);
			}
		};

		addNewCommentLink.addHyperlinkListener(new AbstractHyperlinkListener() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				boolean success = addNewComment();
				if (success) {
					formPart.markDirty();
					refreshCommentsTable();
				}
			}
		});

		getManagedForm().addPart(formPart);
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
				GrUiPlugin.getMessage("addNewCommentDialogTitle"),
				GrUiPlugin.getMessage("addNewCommentDialogDescription"), "",
				null);

		idiag.open();
		String commentString = idiag.getValue();
		idiag.close();

		if (commentString != null && !"".equals(commentString)) {
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
