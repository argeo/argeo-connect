package org.argeo.connect.demo.gr.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackend;
import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.GrTypes;
import org.argeo.connect.demo.gr.ui.GrImages;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.commands.CreateSite;
import org.argeo.connect.demo.gr.ui.providers.GrNodeLabelProvider;
import org.argeo.connect.demo.gr.ui.utils.AbstractHyperlinkListener;
import org.argeo.connect.demo.gr.ui.utils.CommandUtils;
import org.argeo.connect.demo.gr.ui.utils.GrDoubleClickListener;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
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
	private GrBackend grBackend;

	public NetworkDetailsPage(FormEditor editor, String title) {
		super(editor, ID, title);
		network = ((NetworkEditor) editor).getNetwork();
		grBackend = getGrBackend();
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
				| SWT.V_SCROLL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		GridData gd = new GridData();
		gd.heightHint = 100;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
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
				return GrNodeLabelProvider.getName((Node)element);
			}

			public Image getImage(Object element) {
				return GrNodeLabelProvider.getIcon((Node)element);
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
		sitesTableViewer
				.addDoubleClickListener(new GrDoubleClickListener(null));

		// "Add new site" hyperlink :
		Hyperlink addNewSiteLink = tk.createHyperlink(body,GrMessages.get().createSite_lbl, 0);

		final AbstractFormPart formPart = new SectionPart(section) {
			public void commit(boolean onSave) {
				super.commit(onSave);
			}
		};

		addNewSiteLink.addHyperlinkListener(new AbstractHyperlinkListener() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (grBackend.isUserInRole(ROLE_ADMIN)
						|| grBackend.isUserInRole(ROLE_MANAGER)) {
					callCreateSiteCommand();
					formPart.markDirty();
				} else
					MessageDialog.openError(GrUiPlugin.getDefault()
							.getWorkbench().getActiveWorkbenchWindow()
							.getShell(),
							GrUiPlugin.getMessage("forbiddenActionTitle"),
							GrUiPlugin.getMessage("forbiddenActionText"));
			}
		});

		getManagedForm().addPart(formPart);

		return section;

	}

	private void callCreateSiteCommand() {
		String uid;
		try {
			uid = network.getIdentifier();
		} catch (RepositoryException e) {
			throw new ArgeoException(
					"JCR Error while getting current network node uid", e);
		}
		CommandUtils.CallCommandWithOneParameter(CreateSite.ID,
				CreateSite.PARAM_UID, uid);
	}
}
