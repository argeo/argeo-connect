package org.argeo.connect.streams.web.pages;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.streams.web.providers.RssListLblProvider;
import org.argeo.connect.streams.web.providers.SimpleNodeListContentProvider;
import org.argeo.connect.web.CmsUiProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * A simple list of all posts with a search.
 * 
 * This must be fixed and the dependency to people should be removed.
 */
public class SearchPostPage implements CmsUiProvider {
	final static Log log = LogFactory.getLog(SearchPostPage.class);

	// Business Objects
	private Session session;
	private String entityType = NodeType.NT_UNSTRUCTURED;
	// TODO use this type instead as soon as the import is fixed.
	// private String entityType = RssTypes.RSS_ITEM;

	// This page widgets
	private TableViewer entityViewer;
	private Text filterTxt;
	private final static String FILTER_HELP_MSG = "Enter filter criterion";

	/** Passed node is only used to retrieve JCR Session */
	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {
		try {
			session = context.getSession();
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get session for node "
					+ context, re);
		}
		createPartControl(parent);
		return null;
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		createFilterPanel(parent);
		createListPart(parent);
		refreshFilteredList();
	}

	protected ILabelProvider getCurrentLabelProvider() {
		return new RssListLblProvider(false);
	}

	protected int getCurrRowHeight() {
		return 60;
	}

	public void createFilterPanel(Composite parent) {
		// Text Area for the filter
		filterTxt = new Text(parent, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH
				| SWT.ICON_CANCEL);
		filterTxt.setMessage(FILTER_HELP_MSG);
		filterTxt.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		filterTxt.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 5003010530960334977L;

			public void modifyText(ModifyEvent event) {
				// might be better to use an asynchronous Refresh();
				refreshFilteredList();
			}
		});
	}

	protected void refreshFilteredList() {
		try {
			String filter = filterTxt.getText();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();

			Selector source = factory.selector(entityType, entityType);

			// no Default Constraint
			Constraint defaultC = null;

			// Parse the String
			String[] strs = filter.trim().split(" ");
			if (strs.length == 0) {
				StaticOperand so = factory.literal(session.getValueFactory()
						.createValue("*"));
				defaultC = factory.fullTextSearch("selector", null, so);
			} else {
				for (String token : strs) {
					StaticOperand so = factory.literal(session
							.getValueFactory().createValue("*" + token + "*"));
					Constraint currC = factory.fullTextSearch(
							source.getSelectorName(), null, so);
					if (defaultC == null)
						defaultC = currC;
					else
						defaultC = factory.and(defaultC, currC);
				}
			}
			QueryObjectModel query;
			query = factory.createQuery(source, defaultC, null, null);
			// TODO use virtual table viewer
			QueryResult result = query.execute();
			entityViewer
					.setInput(JcrUtils.nodeIteratorToList(result.getNodes()));
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list entity", e);
		}
	}

	protected void createListPart(Composite parent) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		tableComposite.setLayoutData(gd);

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(getCurrentLabelProvider());

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(85));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT,
				Integer.valueOf(getCurrRowHeight()));
		v.setContentProvider(new SimpleNodeListContentProvider());
		// v.addDoubleClickListener(peopleUiService
		// .getNewNodeListDoubleClickListener(peopleService));
		entityViewer = v;
	}
}