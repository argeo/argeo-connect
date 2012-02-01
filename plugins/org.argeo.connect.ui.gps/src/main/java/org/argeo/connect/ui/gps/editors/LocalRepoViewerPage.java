package org.argeo.connect.ui.gps.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class LocalRepoViewerPage extends FormPage {
	private final static Log log = LogFactory.getLog(LocalRepoViewerPage.class);
	public final static String ID = "localRepoEditor.localRepoViewerPage";

	// This page widgets
	private FormToolkit ft;
	private MapViewer mapViewer;
	private Button displayAllSensorsChk, showBaseLayerChk;
	private Combo chooseSensorCmb;

	public LocalRepoViewerPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	public LocalRepoEditor getEditor() {
		return (LocalRepoEditor) super.getEditor();
	}

	protected void createFormContent(IManagedForm managedForm) {
		// Initialize current form
		ScrolledForm form = managedForm.getForm();
		ft = managedForm.getToolkit();

		Composite body = form.getBody();
		body.setLayout(new GridLayout(1, true));

		createParameterPart(body);
		createMapPart(body);
		try {
			addPositionsLayer();
		} catch (Exception e) {
			ErrorFeedback.show("Cannot load data layer", e);
		}
	}

	private void createParameterPart(Composite top) {
		Composite parent = ft.createComposite(top);
		parent.setLayout(new GridLayout(4, false));
		GridData gd;

		// Choose a sensor to enlight
		ft.createLabel(parent, "Choose a specific sensor:", SWT.NONE);
		chooseSensorCmb = new Combo(parent, SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL);
		gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		chooseSensorCmb.setLayoutData(gd);
		populateChooseSensorCmb(chooseSensorCmb);
		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateLayers();
			}
		};
		chooseSensorCmb.addModifyListener(modifyListener);
		// Manage layers to display
		displayAllSensorsChk = ft.createButton(parent, "Show all sensors",
				SWT.CHECK | SWT.LEFT);
		showBaseLayerChk = ft.createButton(parent, "Show base layer", SWT.CHECK
				| SWT.LEFT);
		Listener executeListener = new Listener() {
			public void handleEvent(Event event) {
				updateLayers();
			}
		};
		displayAllSensorsChk.addListener(SWT.Selection, executeListener);
		showBaseLayerChk.addListener(SWT.Selection, executeListener);
	}

	private void populateChooseSensorCmb(Combo combo) {
		combo.removeAll();
		List<String> sensors = getEditor().getUiJcrServices().getSensors(
				getEditor().getCurrentRepoNode());
		for (String sensor : sensors) {
			combo.add(sensor);
		}
	}

	private void updateLayers() {
		refreshSensorLayer();
		// TODO implement
		log.debug("Update Layer... ");
	}

	protected void createMapPart(Composite parent) {
		Composite mapArea = getManagedForm().getToolkit().createComposite(
				parent, SWT.BORDER);
		mapArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		FillLayout layout = new FillLayout();
		mapArea.setLayout(layout);
		mapViewer = getEditor().getUiGisServices().getMapControlCreator()
				.createMapControl(getEditor().getCurrentRepoNode(), mapArea);
		getEditor().getUiGisServices().addBaseLayers(mapViewer);
	}

	/*
	 * GIS
	 */

	// public void addBaseLayers(MapViewer mapViewer) {
	// for (String alias : baseLayers) {
	// String layerPath = (GisConstants.DATA_STORES_BASE_PATH + alias)
	// .trim();
	// try {
	// Node layerNode = currentSession.getNode(layerPath);
	// mapViewer.addLayer(layerNode,
	// StylingUtils.createLineStyle("LIGHT_GRAY", 1));
	// } catch (RepositoryException e) {
	// log.warn("Cannot retrieve " + alias + ": " + e);
	// }
	// }
	// }

	protected void addPositionsLayer() {
		// Add position layer
		String positionsDisplayPath = getEditor()
				.getUiGisServices()
				.getTrackDao()
				.getPositionsDisplaySource(
						getEditor().getUiJcrServices().getReferentialTechName(
								getEditor().getCurrentRepoNode()));
		try {
			Node layerNode = getEditor().getCurrentRepoNode().getSession()
					.getNode(positionsDisplayPath);
			Style style = createSensorStyle();
			mapViewer.addLayer(layerNode, style);

			// mapViewer.setCoordinateReferenceSystem("EPSG:3857");

			ReferencedEnvelope areaOfInterest = getPositionsFeatureSource()
					.getBounds();
			mapViewer.setAreaOfInterest(areaOfInterest);
		} catch (Exception e) {
			throw new ArgeoException(
					"Cannot add layer " + positionsDisplayPath, e);
		}
	}

	protected FeatureSource<SimpleFeatureType, SimpleFeature> getPositionsFeatureSource() {
		String trackSpeedsPath = getEditor()
				.getUiGisServices()
				.getTrackDao()
				.getPositionsSource(
						getEditor().getUiJcrServices().getReferentialTechName(
								getEditor().getCurrentRepoNode()));
		try {

			Node layerNode = getEditor().getCurrentRepoNode().getSession()
					.getNode(trackSpeedsPath);
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = mapViewer
					.getGeoJcrMapper().getFeatureSource(layerNode);
			return featureSource;
		} catch (Exception e) {
			throw new ArgeoException("Cannot get feature source "
					+ trackSpeedsPath, e);
		}
	}

	protected void refreshSensorLayer() {
		String positionsDisplayPath = getEditor()
				.getUiGisServices()
				.getTrackDao()
				.getPositionsDisplaySource(
						getEditor().getUiJcrServices().getReferentialTechName(
								getEditor().getCurrentRepoNode()));
		mapViewer.setStyle(positionsDisplayPath, createSensorStyle());
	}

	protected Style createSensorStyle() {
		Map<String, String> cqlFilters = new HashMap<String, String>();
		if (chooseSensorCmb.getSelectionIndex() != -1) {
			String sensor = chooseSensorCmb.getItem(chooseSensorCmb
					.getSelectionIndex());
			cqlFilters.put("sensor='" + sensor + "'", "RED");
		}
		String color = displayAllSensorsChk.getSelection() ? "BLACK" : null;

		Style style = StylingUtils.createFilteredLineStyle(cqlFilters, 3,
				color, 1);
		return style;
	}

}
