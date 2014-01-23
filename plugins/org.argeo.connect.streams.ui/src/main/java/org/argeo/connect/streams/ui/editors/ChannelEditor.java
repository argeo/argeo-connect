package org.argeo.connect.streams.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
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
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.editors.utils.AbstractEntityEditor;
import org.argeo.connect.people.ui.providers.BasicNodeListContentProvider;
import org.argeo.connect.people.ui.utils.JcrUiUtils;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.streams.RssNames;
import org.argeo.connect.streams.RssTypes;
import org.argeo.connect.streams.ui.RssUiPlugin;
import org.argeo.connect.streams.ui.providers.RssHtmlProvider;
import org.argeo.connect.streams.ui.providers.RssListLabelProvider;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Manage a single channel
 * <ul>
 * <li>enrich meta data for a given source.</li>
 * <li>display corresponding items</li>
 * </ul>
 */
public class ChannelEditor extends AbstractEntityEditor implements RssNames {
	final static Log log = LogFactory.getLog(ChannelEditor.class);
	public final static String ID = RssUiPlugin.PLUGIN_ID + ".channelEditor";

	// Main business Objects
	private Node channel;
	private TableViewer feeds;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		channel = getEntity();
	}

	/**
	 * Overwrite default main ppanel creation to spare some space.
	 */
	protected void createMainInfoPanel(final Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());

		// First row: Title + Buttons.
		Composite firstRow = toolkit.createComposite(parent, SWT.NO_FOCUS);
		firstRow.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout gl = new GridLayout(2, false);
		gl.marginLeft = 5;
		gl.marginRight = 5;
		gl.marginTop = 5;
		firstRow.setLayout(gl);

		// left: title
		Composite title = toolkit.createComposite(firstRow, SWT.NO_FOCUS);
		title.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		populateTitleComposite(title);

		// Right: buttons
		Composite buttons = toolkit.createComposite(firstRow, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		gd.heightHint = 30;
		buttons.setLayoutData(gd);
		populateButtonsComposite(buttons);

		// NO TAGS for the time being
		// // 2nd line: Main Info Details
		// Composite details = toolkit.createComposite(parent, SWT.NO_FOCUS);
		// details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// populateMainInfoDetails(details);

		parent.layout();
	}

	@Override
	protected void populateTitleComposite(Composite parent) {
		parent.setLayout(new FormLayout());

		// READ ONLY
		final Composite readOnlyPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(readOnlyPanel);
		readOnlyPanel.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		final Label readOnlyInfoLbl = toolkit.createLabel(readOnlyPanel, "",
				SWT.WRAP);
		readOnlyInfoLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);

		// EDIT
		final Composite editPanel = toolkit.createComposite(parent,
				SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(editPanel);

		// intern layout
		editPanel.setLayout(new GridLayout(2, false));
		final Text titleTxt = createGDText(editPanel, "Title",
				"Rss feed title", 200, 1);

		final Text websiteTxt = createGDText(editPanel, "Website",
				"This channel provider's website", 200, 1);
		((GridData) websiteTxt.getLayoutData()).grabExcessHorizontalSpace = false;

		final Text descTxt = createGDText(editPanel, "A Description", "", 400,
				2);

		editPanel.layout();

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() { // update display value
				try {
					super.refresh();
					// EDIT PART
					refreshTextValue(titleTxt, channel, Property.JCR_TITLE);
					refreshTextValue(websiteTxt, channel, RSS_LINK);
					refreshTextValue(descTxt, channel, Property.JCR_DESCRIPTION);

					// READ ONLY PART
					readOnlyInfoLbl.setText(RssHtmlProvider
							.getChannelTitle(channel));
					// Manage switch
					if (CommonsJcrUtils.isNodeCheckedOutByMe(channel))
						editPanel.moveAbove(readOnlyPanel);
					else
						editPanel.moveBelow(readOnlyPanel);
					editPanel.getParent().layout();
				} catch (RepositoryException e) {
					throw new PeopleException("Unable to get channel info", e);
				}

			}
		};

		// Must refresh a first time so that UI and JCR are in line before
		// adding the listeners.
		editPart.refresh();
		// Listeners
		addTxtModifyListener(editPart, titleTxt, channel, Property.JCR_TITLE,
				PropertyType.STRING);
		// addTxtModifyListener(editPart, titleTxt, channel, RSS_LINK,
		// PropertyType.STRING);
		addTxtModifyListener(editPart, descTxt, channel,
				Property.JCR_DESCRIPTION, PropertyType.STRING);

		// compulsory because we broke normal life cycle while implementing
		// IManageForm
		editPart.initialize(getManagedForm());
		getManagedForm().addPart(editPart);
	}

	@Override
	protected Boolean deleteParentOnRemove() {
		return true;
	}

	@Override
	protected void populateMainInfoDetails(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// Details
		Composite details = toolkit.createComposite(parent, SWT.NO_FOCUS);
		details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		populateTagsROPanel(details);

		// Create NEW tags
		Composite addTagPanel = toolkit.createComposite(parent, SWT.NO_FOCUS);
		addTagPanel
				.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
		populateAddTagComposite(addTagPanel);
	}

	@Override
	protected void createBodyPart(Composite parent) {
		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		// 1st line: Search + Buttons.
		// Composite buttons = toolkit.createComposite(parent, SWT.NO_FOCUS);
		// buttons.setLayout(gridLayoutNoBorder());
		// buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// toolkit.createLabel(buttons, "Add here search and buttons");

		// 2nd line: the list
		Composite list = toolkit.createComposite(parent, SWT.NO_FOCUS);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createListPart(list, new RssListLabelProvider(false));

		// set data
		// put that in a cleaner place it's too hidden here
		// refreshFilteredList(RssTypes.RSS_CHANNEL);
	}

	protected void populateTagsROPanel(final Composite parent) {
		parent.setLayout(new FormLayout());
		// Show only TAGS for the time being, so it is the same for R/O & Edit
		// mode
		final Composite panel = toolkit.createComposite(parent, SWT.NO_FOCUS);
		PeopleUiUtils.setSwitchingFormData(panel);

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		panel.setLayout(tableColumnLayout);

		int style = SWT.NO_SCROLL;
		Table table = new Table(panel, style);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.CUSTOM_VARIANT, "uniqueCellTable");
		// table.setData(RWT.CUSTOM_VARIANT, "RSS-uniqueCellTable");

		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(70));

		// Does not work: adding a tag within the <a> tag unvalid the
		// target="_RWT" parameter
		// ResourceManager resourceManager = RWT.getResourceManager();
		// if (!resourceManager.isRegistered("icons/close.png")) {
		// InputStream inputStream = this.getClass().getClassLoader()
		// .getResourceAsStream("icons/close.png");
		// try {
		// resourceManager.register("icons/close.png", inputStream);
		// } finally {
		// IOUtils.closeQuietly(inputStream);
		// }
		// }
		// final String src = RWT.getResourceManager().getLocation(
		// "icons/close.png");

		final TableViewer viewer = new TableViewer(table);
		viewer.setLabelProvider(new LabelProvider() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(Object element) {
				try {
					Node node = (Node) element;
					StringBuilder tags = new StringBuilder();
					if (node.hasProperty(RSS_CATEGORY)) {
						tags.append("<span style=\"float:left;padding:0px;white-space:pre-wrap;text-decoration:none;\">");
						Value[] values = channel.getProperty(RSS_CATEGORY)
								.getValues();
						for (int i = 0; i < values.length; i++) {
							String currStr = values[i].getString();
							tags.append("#");
							tags.append(currStr).append("&#160;");
							tags.append("<i><a style=\"text-decoration:none;\" href=\"");
							tags.append(currStr);
							tags.append("\" target=\"_rwt\">" + "X</a></i>")
									.append("&#160;&#160; ");
						}
						tags.append("</span>");
					}
					return tags.toString();
				} catch (RepositoryException re) {
					throw new PeopleException("unable to get tags", re);
				}
			}

		});
		viewer.setContentProvider(new BasicNodeListContentProvider());

		TableColumn singleColumn = new TableColumn(table, SWT.LEFT);
		singleColumn.setData(RWT.CUSTOM_VARIANT, "uniqueCellTable");

		tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(90));

		final AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() { // update display value
				super.refresh();
				List<Node> nodes = new ArrayList<Node>();
				nodes.add(channel);
				viewer.refresh();
			}
		};

		table.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			public void widgetSelected(SelectionEvent event) {
				if (event.detail == RWT.HYPERLINK) {
					try {

						String tagToRemove = event.text;
						if (CommonsJcrUtils.checkNotEmptyString(tagToRemove)) {
							List<String> tags = new ArrayList<String>();
							if (channel.hasProperty(RSS_CATEGORY)) {
								Value[] values = channel.getProperty(
										RSS_CATEGORY).getValues();
								for (int i = 0; i < values.length; i++) {
									String curr = values[i].getString();
									if (!tagToRemove.equals(curr))
										tags.add(curr);
								}
							}
							boolean wasCheckedout = CommonsJcrUtils
									.isNodeCheckedOut(channel);
							if (!wasCheckedout)
								CommonsJcrUtils.checkout(channel);
							channel.setProperty(RSS_CATEGORY,
									tags.toArray(new String[tags.size()]));
							if (!wasCheckedout)
								CommonsJcrUtils.saveAndCheckin(channel);
							else
								getManagedForm().dirtyStateChanged();
						}
						editPart.refresh();
					} catch (RepositoryException re) {
						throw new ArgeoException("Unable to set tags", re);
					}
				}
			}
		});
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(channel);
		viewer.setInput(nodes);
		// editPart.refresh();
		getManagedForm().addPart(editPart);
	}

	public void populateAddTagComposite(Composite parent) {
		parent.setLayout(new RowLayout());
		final Text tagTxt = new Text(parent, SWT.BORDER);
		tagTxt.setMessage("Enter a new tag");
		RowData rd = new RowData(200, SWT.DEFAULT);
		tagTxt.setLayoutData(rd);

		final Button validBtn = toolkit.createButton(parent, "Add", SWT.PUSH);
		rd = new RowData(80, SWT.DEFAULT);
		validBtn.setLayoutData(rd);

		validBtn.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				String newTag = tagTxt.getText();
				boolean wasCheckedout = CommonsJcrUtils
						.isNodeCheckedOut(channel);
				if (!wasCheckedout)
					CommonsJcrUtils.checkout(channel);
				try {
					Value[] values;
					String[] valuesStr;
					if (channel.hasProperty(RSS_CATEGORY)) {
						values = channel.getProperty(RSS_CATEGORY).getValues();
						valuesStr = new String[values.length + 1];
						int i;
						for (i = 0; i < values.length; i++) {
							valuesStr[i] = values[i].getString();
						}
						valuesStr[i] = newTag;
					} else {
						valuesStr = new String[1];
						valuesStr[0] = newTag;
					}
					channel.setProperty(RSS_CATEGORY, valuesStr);
				} catch (RepositoryException re) {
					throw new ArgeoException("Unable to set tags", re);
				}
				if (!wasCheckedout)
					CommonsJcrUtils.saveAndCheckin(channel);
				else
					getManagedForm().dirtyStateChanged();

				forceRefresh();
				// for (IFormPart part : getManagedForm().getParts()) {
				// ((AbstractFormPart) part).markStale();
				// part.refresh();
				// }
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		// parent.pack();
		// parent.redraw();
		// parent.layout();
		// parent.getParent().layout();
	}

	protected void createListPart(Composite parent, ILabelProvider labelProvider) {
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		parent.setLayout(tableColumnLayout);

		// Composite tableComposite = toolkit.createComposite(parent,
		// SWT.NO_FOCUS | SWT.V_SCROLL);
		// tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		// true));
		// // Corresponding table & style

		Table table = toolkit.createTable(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		// Enable markups
		table.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		table.setData(RWT.CUSTOM_ITEM_HEIGHT, Integer.valueOf(70));

		TableViewer v = new TableViewer(table);
		v.setLabelProvider(labelProvider);
		v.setContentProvider(new BasicNodeListContentProvider());
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

	// //////////////
	// Helpers

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

	/** Creates a text widget with RowData already set */
	protected Text createRDText(Composite parent, String msg, String toolTip,
			int width) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		text.setLayoutData(new RowData(width, SWT.DEFAULT));
		return text;
	}

	/** Creates a text widget with GridData already set */
	protected Text createGDText(Composite parent, String msg, String toolTip,
			int width, int colSpan) {
		Text text = toolkit.createText(parent, "", SWT.BORDER | SWT.SINGLE
				| SWT.LEFT);
		text.setMessage(msg);
		text.setToolTipText(toolTip);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.widthHint = width;
		gd.horizontalSpan = colSpan;
		text.setLayoutData(gd);
		return text;
	}
	
	public String getlastUpdateMessage(){
		return "";
	}
		
}