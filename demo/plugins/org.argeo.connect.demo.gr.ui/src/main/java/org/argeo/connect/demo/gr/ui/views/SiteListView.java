package org.argeo.connect.demo.gr.ui.views;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;

import org.argeo.connect.demo.gr.GrConstants;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.ui.GrMessages;
import org.argeo.connect.demo.gr.ui.GrUiPlugin;
import org.argeo.connect.demo.gr.ui.exports.ICalcExtractProvider;
import org.argeo.connect.demo.gr.ui.providers.GrTableLabelProvider;
import org.argeo.connect.demo.gr.ui.utils.NodeViewerComparator;
import org.argeo.eclipse.ui.utils.ViewerUtils;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/** List of all PICs */
public class SiteListView extends AbstractSitesView implements
		ICalcExtractProvider {

	// private final static Log log = LogFactory.getLog(PicListView.class);
	public static final String ID = GrUiPlugin.PLUGIN_ID + ".siteListView";

	// This page widgets
	private Combo siteTypeCmb;
	private Text maxWLevelTxt;
	private Text maxWithdrawnTxt;
	private Text minEColiRateTxt;

	protected SiteListView() {
		super(ID);
	}

	public void createHeaderPart(Composite body) {
		Label lbl;

		// Layout
		GridLayout layout = new GridLayout(2, false);
		body.setLayout(layout);

		// Site Type
		lbl = new Label(body, SWT.NONE);
		lbl.setText(GrMessages.get().siteListView_siteTypeCmb);

		siteTypeCmb = new Combo(body, SWT.BORDER | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		siteTypeCmb.setLayoutData(gd);
		List<String> siteTypes = new ArrayList<String>();
		// Add a blank choice to enable to see all sites
		siteTypes.add("");
		siteTypes.add(GrConstants.MONITORED);
		siteTypes.add(GrConstants.VISITED);
		siteTypes.add(GrConstants.REGISTERED);
		String[] items = siteTypes.toArray(new String[siteTypes.size()]);
		siteTypeCmb.setItems(items);

		siteTypeCmb.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});

		// Water level
		lbl = new Label(body, SWT.NONE);
		lbl.setText(GrMessages.get().siteListView_maxWaterLevelLbl);
		maxWLevelTxt = new Text(body, SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		maxWLevelTxt.setLayoutData(gd);
		maxWLevelTxt.setText("");
		maxWLevelTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});

		// Withdrawn water
		lbl = new Label(body, SWT.NONE);
		lbl.setText(GrMessages.get().siteListView_maxWithdrawnWaterLbl);
		maxWithdrawnTxt = new Text(body, SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		maxWithdrawnTxt.setLayoutData(gd);
		maxWithdrawnTxt.setText("");
		maxWithdrawnTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});

		// E-Coli Rate
		lbl = new Label(body, SWT.NONE);
		lbl.setText(GrMessages.get().siteListView_minEColiRateLbl);

		minEColiRateTxt = new Text(body, SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		minEColiRateTxt.setLayoutData(gd);
		minEColiRateTxt.setText("");
		minEColiRateTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				refreshFilteredList();
			}
		});
	}

	@Override
	protected void refreshFilteredList() {
		StringBuffer whereClause = new StringBuffer();
		boolean hasPreviousClause = false;

		// Site Type
		int index = siteTypeCmb.getSelectionIndex();
		if (index > 0) {
			whereClause.append("sites.[" + GrNames.GR_SITE_TYPE + "] =");
			String valStr = siteTypeCmb.getItem(index);
			whereClause.append("'" + valStr + "'");
			hasPreviousClause = true;
		}

		// TODO : validate double values
		// water level
		String maxWLevel = maxWLevelTxt.getText();
		if (!"".equals(maxWLevel)) {
			if (hasPreviousClause)
				whereClause.append(" AND ");
			whereClause.append("sites.[" + GrNames.GR_WATER_LEVEL + "] <= ");
			whereClause.append("CAST(" + maxWLevel + " AS DOUBLE)");
			hasPreviousClause = true;
		}

		// Withdrawn water
		String maxWithdrawn = maxWithdrawnTxt.getText();
		if (!"".equals(maxWithdrawn)) {
			if (hasPreviousClause)
				whereClause.append(" AND ");
			whereClause.append("sites.[" + GrNames.GR_WITHDRAWN_WATER + "]<=");
			whereClause.append("CAST(" + maxWithdrawn + " AS DOUBLE)");
			hasPreviousClause = true;
		}

		// E-Coli rate
		String minEColi = minEColiRateTxt.getText();
		if (!"".equals(minEColi.trim())) {
			if (hasPreviousClause)
				whereClause.append(" AND ");
			whereClause.append("sites.[" + GrNames.GR_ECOLI_RATE + "]>=");
			whereClause.append("CAST(" + minEColi + " AS DOUBLE)");
		}
		refresh(super.getSitesWithWhereClause(whereClause.toString()));
	}

	@Override
	protected TableViewer createTableViewer(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION;

		Table table = new Table(parent, style);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		propertiesList = new ArrayList<String>();
		propertyTypesList = new ArrayList<Integer>();

		// this column is used to store Sites UID for double click listener but
		// is
		// not displayed.
		String currColLbl = "jcr:identifier";
		TableColumn column = ViewerUtils.createColumn(table, currColLbl,
				SWT.LEFT, 0);
		propertiesList.add(GrNames.GR_UUID);
		propertyTypesList.add(PropertyType.STRING);
		// column.addSelectionListener(getSelectionAdapter(column, 0));

		currColLbl = GrMessages.get().siteLbl;
		column = ViewerUtils.createColumn(table, currColLbl, SWT.LEFT, 120);
		propertiesList.add(Property.JCR_TITLE);
		propertyTypesList.add(PropertyType.STRING);
		column.addSelectionListener(getSelectionAdapter(column, 1));

		// Water level
		currColLbl = GrMessages.get().waterLevelShortLbl;
		column = ViewerUtils.createColumn(table, currColLbl, SWT.RIGHT, 100);
		propertiesList.add(GrNames.GR_WATER_LEVEL);
		propertyTypesList.add(PropertyType.DOUBLE);
		column.addSelectionListener(getSelectionAdapter(column, 2));
		column.setToolTipText(GrMessages.get().waterLevelLbl);

		// Withdrawn water
		currColLbl = GrMessages.get().withdrawnWaterShortLbl;
		column = ViewerUtils.createColumn(table, currColLbl, SWT.RIGHT, 120);
		propertiesList.add(GrNames.GR_WITHDRAWN_WATER);
		propertyTypesList.add(PropertyType.DOUBLE);
		column.addSelectionListener(getSelectionAdapter(column, 3));
		column.setToolTipText(GrMessages.get().withdrawnWaterLbl);

		// E-Coli rate
		currColLbl = GrMessages.get().eColiRateShortLbl;
		column = ViewerUtils.createColumn(table, currColLbl, SWT.RIGHT, 100);
		propertiesList.add(GrNames.GR_ECOLI_RATE);
		propertyTypesList.add(PropertyType.DOUBLE);
		column.addSelectionListener(getSelectionAdapter(column, 4));
		column.setToolTipText(GrMessages.get().eColiRateLbl);

		tableViewer = new TableViewer(table);
		tableViewer.setLabelProvider(new GrTableLabelProvider(propertiesList));

		comparator = new NodeViewerComparator(1,
				NodeViewerComparator.DESCENDING, propertiesList,
				propertyTypesList);
		tableViewer.setComparator(comparator);

		return tableViewer;
	}
}