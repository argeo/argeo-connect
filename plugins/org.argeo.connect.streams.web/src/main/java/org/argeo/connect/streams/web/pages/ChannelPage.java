package org.argeo.connect.streams.web.pages;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.web.providers.RssListLblProvider;
import org.argeo.connect.web.CmsUiProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Manage a single channel
 * <ul>
 * <li>enrich meta data for a given source.</li>
 * <li>display corresponding items</li>
 * </ul>
 */
public class ChannelPage implements CmsUiProvider, RssNames {
	final static Log log = LogFactory.getLog(ChannelPage.class);

	// Main business Objects
	private Session session;
	private Node channel;
	private TableViewer feeds;

	@Override
	public Control createUi(Composite parent, Node context)
			throws RepositoryException {

		this.channel = context;
		try {
			session = channel.getSession();
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get session for node "
					+ context, re);
		}
		createMainInfoPanel(parent);
		return null;
	}

	/**
	 * Overwrite default main panel creation to spare some space.
	 */
	protected void createMainInfoPanel(final Composite parent) {
		parent.setLayout(new GridLayout());

		// First row: Title + Buttons.
		Composite firstRow = new Composite(parent, SWT.NO_FOCUS);
		firstRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = 5;
		gl.marginRight = 5;
		gl.marginTop = 5;
		firstRow.setLayout(gl);

		// left: title
		Composite title = new Composite(firstRow, SWT.NO_FOCUS);
		title.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(title);

		// Right: buttons
		Composite buttons = new Composite(firstRow, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.heightHint = 30;
		buttons.setLayoutData(gd);
		// populateButtonsComposite(buttons);

		// NO TAGS for the time being
		// // 2nd line: Main Info Details
		// Composite details = toolkit.createComposite(parent, SWT.NO_FOCUS);
		// details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// populateMainInfoDetails(details);

		parent.layout();
	}

	protected void populateTitleComposite(Composite parent) {
		parent.setLayout(new FormLayout());

		new Label(parent, SWT.NONE).setText("TITLE");

		// // READ ONLY
		// final Composite readOnlyPanel = new Composite(parent,
		// SWT.NO_FOCUS);
		// PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		// readOnlyPanel.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
		// SWT.WRAP);
		// readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		//
		// // EDIT
		// final Composite editPanel = toolkit.createComposite(parent,
		// SWT.NO_FOCUS);
		// PeopleUiUtils.setSwitchingFormData(editPanel);
		//
		// // intern layout
		// editPanel.setLayout(new GridLayout(2, false));
		// final Text titleTxt = createGDText(editPanel, "Title",
		// "Rss feed title", 200, 1);
		//
		// final Text websiteTxt = createGDText(editPanel, "Website",
		// "This channel provider's website", 200, 1);
		// ((GridData) websiteTxt.getLayoutData()).grabExcessHorizontalSpace =
		// false;
		//
		// final Text descTxt = createGDText(editPanel, "A Description", "",
		// 400,
		// 2);
		//
		// editPanel.layout();
		//
		// final AbstractFormPart editPart = new AbstractFormPart() {
		// public void refresh() { // update display value
		// try {
		// super.refresh();
		// // EDIT PART
		// refreshTextValue(titleTxt, channel, Property.JCR_TITLE);
		// refreshTextValue(websiteTxt, channel, RSS_LINK);
		// refreshTextValue(descTxt, channel, Property.JCR_DESCRIPTION);
		//
		// // READ ONLY PART
		// readOnlyInfoLbl.setText(RssHtmlProvider
		// .getChannelTitle(channel));
		// // Manage switch
		// if (CommonsJcrUtils.isNodeCheckedOutByMe(channel))
		// editPanel.moveAbove(readOnlyPanel);
		// else
		// editPanel.moveBelow(readOnlyPanel);
		// editPanel.getParent().layout();
		// } catch (RepositoryException e) {
		// throw new PeopleException("Unable to get channel info", e);
		// }
		//
		// }
		// };
		//
		// // Must refresh a first time so that UI and JCR are in line before
		// // adding the listeners.
		// editPart.refresh();
		// // Listeners
		// addTxtModifyListener(editPart, titleTxt, channel, Property.JCR_TITLE,
		// PropertyType.STRING);
		// // addTxtModifyListener(editPart, titleTxt, channel, RSS_LINK,
		// // PropertyType.STRING);
		// addTxtModifyListener(editPart, descTxt, channel,
		// Property.JCR_DESCRIPTION, PropertyType.STRING);
		//
		// // compulsory because we broke normal life cycle while implementing
		// // IManageForm
		// editPart.initialize(getManagedForm());
		// getManagedForm().addPart(editPart);
	}

	protected void populateMainInfoDetails(Composite parent) {
		parent.setLayout(new GridLayout());

		// Details
		Composite details = new Composite(parent, SWT.NO_FOCUS);
		details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// populateTagsROPanel(details);

		// Create NEW tags
		Composite addTagPanel = new Composite(parent, SWT.NO_FOCUS);
		addTagPanel
				.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
		// populateAddTagComposite(addTagPanel);
	}

	protected void createBodyPart(Composite parent) {
		parent.setLayout(new GridLayout());
		// 1st line: Search + Buttons.
		// Composite buttons = toolkit.createComposite(parent, SWT.NO_FOCUS);
		// buttons.setLayout(gridLayoutNoBorder());
		// buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// toolkit.createLabel(buttons, "Add here search and buttons");

		// 2nd line: the list
		Composite list = new Composite(parent, SWT.NO_FOCUS);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createListPart(list, new RssListLblProvider(false));

		// set data
		// put that in a cleaner place it's too hidden here
		// refreshFilteredList(RssTypes.RSS_CHANNEL);
	}

	protected void createListPart(Composite parent, ILabelProvider labelProvider) {
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		parent.setLayout(tableColumnLayout);

		// Composite tableComposite = toolkit.createComposite(parent,
		// SWT.NO_FOCUS | SWT.V_SCROLL);
		// tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		// true));
		// // Corresponding table & style

		Table table = new Table(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(70));

		TableViewer v = new TableViewer(table);
		v.setLabelProvider(labelProvider);
		// v.setContentProvider(new BasicNodeListContentProvider());
		// v.addDoubleClickListener(new
		// NodeListDoubleClickListener(peopleService));
		feeds = v;

		TableColumn singleColumn = new TableColumn(table, SWT.LEFT);
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(90));

		refreshFilteredList(RssTypes.RSS_CHANNEL);
		// table.setLayout(tableColumnLayout);
	}

	protected void refreshFilteredList(String nodeType) {
		try {
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory factory = queryManager.getQOMFactory();
			final String typeSelector = "items";
			Selector source = factory.selector(RssTypes.RSS_ITEM, typeSelector);
			DescendantNode statOp = factory.descendantNode(
					source.getSelectorName(), channel.getParent().getPath());

			Ordering order = factory.descending(factory.lowerCase(factory
					.propertyValue(source.getSelectorName(), RSS_PUB_DATE)));

			QueryObjectModel query = factory.createQuery(source, statOp,
					new Ordering[] { order }, null);
			QueryResult result = query.execute();
			NodeIterator ni = result.getNodes();
			feeds.setInput(JcrUtils.nodeIteratorToList(ni));
		} catch (RepositoryException e) {
			throw new ArgeoException("Unable to list " + nodeType, e);
		}
	}

	// //////////////
	// Helpers

	/** Creates a text widget with RowData already set */
	protected Text createRDText(Composite parent, String msg, String toolTip,
			int width) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}

	/** Creates a text widget with GridData already set */
	protected Text createGDText(Composite parent, String msg, String toolTip,
			int width, int colSpan) {
		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = width;
		gd.horizontalSpan = colSpan;
		text.setLayoutData(gd);
		return text;
	}

	public String getlastUpdateMessage() {
		return "";
	}

}