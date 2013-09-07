package org.argeo.connect.streams.ui.editors;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
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
import org.argeo.connect.people.PeopleValueCatalogs;
import org.argeo.connect.people.ui.JcrUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.editors.AbstractEntityEditor;
import org.argeo.connect.people.ui.editors.EntityAbstractFormPart;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.ui.RssUiPlugin;
import org.argeo.connect.streams.ui.providers.RssListLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;

// import org.eclipse.rap.rwt.RWT;

/**
 * Manage a single channel or a group:
 * <ul>
 * <li>enrich meta data for a given source.</li>
 * <li>display corresponding items</li>
 * </ul>
 */
public class ChannelEditor extends AbstractEntityEditor implements RssNames {
	final static Log log = LogFactory.getLog(ChannelEditor.class);

	// local constants
	public final static String ID = RssUiPlugin.PLUGIN_ID + ".channelEditor";
	// Main business Objects
	private Node channel;

	private TableViewer feeds;

	// private RssManager rssManager;

	@Override
	protected void populateTabFolder(CTabFolder tabFolder) {
		// TODO Auto-generated method stub
		// Jobs panel
		String tooltip = "Last feeds from channel";
		Composite innerPannel = addTabToFolder(tabFolder, SWT.NO_FOCUS,
				"Feeds", "rss:feedDisplay", tooltip);
		createListPart(innerPannel, new RssListLabelProvider(false));
		// set data
		refreshFilteredList(RssTypes.RSS_CHANNEL);
	}

	protected void refreshFilteredList(String nodeType) {
		try {
			QueryManager queryManager = getSession().getWorkspace()
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

	protected void createHeaderPart(final Composite header) {
		header.setLayout(new GridLayout(1, false));

		// General information panel
		final Composite mainInfoComposite = toolkit.createComposite(header,
				SWT.NO_FOCUS);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;

		// gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		mainInfoComposite.setLayoutData(gd);

		GridLayout gl = new GridLayout();
		gl.verticalSpacing = 0;
		mainInfoComposite.setLayout(gl);

		// The buttons
		Composite buttonPanel = toolkit.createComposite(mainInfoComposite,
				SWT.NO_FOCUS);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.TOP;
		buttonPanel.setLayoutData(gd);
		populateButtonsComposite(buttonPanel);

		// Main info panel
		Composite switchingPanel = toolkit.createComposite(mainInfoComposite,
				SWT.NO_FOCUS);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		populateMainInfoComposite(switchingPanel);

		// Add a new tag
		Composite addTagPanel = toolkit.createComposite(mainInfoComposite,
				SWT.NO_FOCUS);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.TOP;
		populateAddTagComposite(addTagPanel);

	}

	protected void createListPart(Composite parent, ILabelProvider labelProvider) {
		parent.setLayout(new GridLayout());

		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		tableComposite.setLayoutData(gd);

		TableViewer v = new TableViewer(tableComposite);
		v.setLabelProvider(labelProvider);

		TableColumn singleColumn = new TableColumn(v.getTable(), SWT.V_SCROLL);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(90));
		tableComposite.setLayout(tableColumnLayout);

		// Corresponding table & style
		Table table = v.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(75));
		v.setContentProvider(new BasicNodeListContentProvider());
		// v.addDoubleClickListener(new
		// NodeListDoubleClickListener(peopleService));
		feeds = v;
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		channel = getNode();
		setPartName(JcrUtils.getStringPropertyQuietly(channel,
				Property.JCR_TITLE));
	}

	@Override
	protected void populateMainInfoComposite(final Composite switchingPanel) {
		switchingPanel.setLayout(new FormLayout());

		// READ ONLY
		final Composite readOnlyPanel = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		readOnlyPanel.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);
		readOnlyPanel.setLayout(new GridLayout());
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		final ILabelProvider personLP = new RssListLabelProvider(false);

		// EDIT
		final Composite editPanel = toolkit.createComposite(switchingPanel,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);
		editPanel.setData(RWT.CUSTOM_VARIANT,
				PeopleUiConstants.PEOPLE_CSS_GENERALINFO_COMPOSITE);

		// intern layout
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		editPanel.setLayout(rl);

		// Create edit text
		final Text titleTxt = createText(editPanel, "Title", "Rss feed title",
				200);
		// final Text websiteTxt = createText(editPanel, "Website",
		// "This channel provider's website", 200);
		final Text descTxt = createText(editPanel, "A Description", "", 100);

		final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				// EDIT PART
				refreshTextValue(titleTxt, channel, Property.JCR_TITLE);
				// refreshTextValue(websiteTxt, channel, RSS_LINK);
				refreshTextValue(descTxt, channel, Property.JCR_DESCRIPTION);

				// READ ONLY PART
				String roText = personLP.getText(channel);
				readOnlyInfoLbl.setText(roText);

				// Manage switch
				if (CommonsJcrUtils.isNodeCheckedOutByMe(channel))
					editPanel.moveAbove(readOnlyPanel);
				else
					editPanel.moveBelow(readOnlyPanel);
				editPanel.getParent().layout();
			}
		};

		// Listeners
		addTxtModifyListener(editPart, titleTxt, channel, Property.JCR_TITLE,
				PropertyType.STRING);
		// addTxtModifyListener(editPart, titleTxt, channel, RSS_LINK,
		// PropertyType.STRING);
		addTxtModifyListener(editPart, descTxt, channel,
				Property.JCR_DESCRIPTION, PropertyType.STRING);
		getManagedForm().addPart(editPart);
	}

	// /** Populate a composite that enable addition of a new contact */
	// public void populateAddContactPanel(Composite parent, final Node entity)
	// {
	// parent.setLayout(new GridLayout(2, false));
	//
	// final Combo addContactCmb = new Combo(parent, SWT.NONE | SWT.READ_ONLY
	// | SWT.NO_FOCUS);
	// GridData gd = new GridData(SWT.TOP, SWT.CENTER, false, false);
	// gd.widthHint = 100;
	// addContactCmb.setLayoutData(gd);
	// addContactCmb.setItems(PeopleValueCatalogs.ARRAY_CONTACT_TYPES);
	// // Add a default value
	// addContactCmb.add("Add a contact", 0);
	// addContactCmb.select(0);
	//
	// final Composite editPanel = toolkit.createComposite(parent,
	// SWT.NO_FOCUS);
	// gd = new GridData(GridData.FILL_HORIZONTAL);
	// gd.grabExcessHorizontalSpace = true;
	// editPanel.setLayoutData(gd);
	//
	// editPanel.setVisible(false);
	//
	// final EntityAbstractFormPart editPart = new EntityAbstractFormPart() {
	// // Update values on refresh
	// public void refresh() {
	// super.refresh();
	// editPanel.setVisible(false);
	// addContactCmb.select(0);
	// }
	// };
	// getManagedForm().addPart(editPart);
	// parent.layout();
	//
	// // show the edit new contact panel when selection change
	// addContactCmb.addSelectionListener(new SelectionListener() {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// String selected = addContactCmb.getItem(addContactCmb
	// .getSelectionIndex());
	// populateAddTagComposite(editPanel, entity, PeopleValueCatalogs
	// .getKeyByValue(PeopleValueCatalogs.MAPS_CONTACT_TYPES,
	// selected));
	// editPanel.setVisible(true);
	// }
	//
	// @Override
	// public void widgetDefaultSelected(SelectionEvent e) {
	// }
	// });
	//
	// }

	public void populateAddTagComposite(Composite parent) {

		parent.setLayout(new RowLayout());

		final Text valueTxt = new Text(parent, SWT.BORDER);
		valueTxt.setMessage("One or more tags prefixed with #");
		RowData rd = new RowData(200, SWT.DEFAULT);
		valueTxt.setLayoutData(rd);

		final Combo addCatCmb = new Combo(parent, SWT.NONE);
		addCatCmb.setItems(PeopleValueCatalogs.ARRAY_CONTACT_CATEGORIES);
		rd = new RowData(200, SWT.DEFAULT);
		addCatCmb.setLayoutData(rd);
		addCatCmb.select(0);

		final Button validBtn = toolkit.createButton(parent, "Add tag",
				SWT.PUSH);
		rd = new RowData(200, SWT.DEFAULT);
		validBtn.setLayoutData(rd);

		validBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = valueTxt.getText();
				String cat = addCatCmb.getText();
				boolean wasCheckedout = CommonsJcrUtils
						.isNodeCheckedOut(channel);
				if (!wasCheckedout)
					CommonsJcrUtils.checkout(channel);
				try {
					channel.setProperty(RSS_CATEGORY, value.split("#"));
				} catch (RepositoryException re) {
					throw new ArgeoException("Unable to set tags", re);
				}
				if (!wasCheckedout)
					CommonsJcrUtils.saveAndCheckin(channel);
				else
					getManagedForm().dirtyStateChanged();
				for (IFormPart part : getManagedForm().getParts()) {
					((AbstractFormPart) part).markStale();
					part.refresh();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		parent.pack();
		parent.redraw();
		parent.layout();

		parent.getParent().layout();
	}

	protected void addTxtModifyListener(final AbstractFormPart part,
			final Text text, final Node entity, final String propName,
			final int propType) {
		text.addModifyListener(new ModifyListener() {
			private static final long serialVersionUID = 3940522217518729442L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (JcrUiUtils.setJcrProperty(entity, propName, propType,
						text.getText()))
					part.markDirty();
			}
		});
	}

	protected void refreshTextValue(Text text, Node entity, String propName) {
		String tmpStr = CommonsJcrUtils.getStringValue(entity, propName);
		if (CommonsJcrUtils.checkNotEmptyString(tmpStr))
			text.setText(tmpStr);
	}

	protected Text createText(Composite parent, String msg, String toolTip,
			int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}

	// /* DEPENDENCY INJECTION */
	// public void setPeopleService(PeopleService peopleService) {
	// this.peopleService = peopleService;
	// try {
	// session = peopleService.getRepository().login();
	// } catch (RepositoryException e) {
	// throw new ArgeoException("Unable to initialize "
	// + "session for view " + ID, e);
	// }
	// }
	// public void setRssManager(RssManager rssManager) {
	// this.rssManager = rssManager;
	// }

}