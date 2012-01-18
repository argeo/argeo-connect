package org.argeo.connect.ui.gps.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.connect.ui.gps.commons.SliderViewer;
import org.argeo.connect.ui.gps.commons.SliderViewerListener;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class DefineParamsAndReviewPage extends AbstractCleanDataEditorPage {
	private final static Log log = LogFactory
			.getLog(DefineParamsAndReviewPage.class);
	public final static String ID = "cleanDataEditor.defineParamsAndReviewPage";

	// Defined parameters
	private List<Node> paramNodeList = new ArrayList<Node>();;

	private FormToolkit formToolkit;

	private SliderViewer maxSpeedViewer;
	private SliderViewer maxAccelerationViewer;
	private SliderViewer maxRotationViewer;

	private MapControlCreator mapControlCreator;
	private MapViewer mapViewer;

	// FIXME retrieve a proper name
	private String getCleanSession() {
		return "HARDCODED";
	}

	// FIXME retrieve a proper name
	private String getReferential() {
		return "HARDCODED";
	}

	public DefineParamsAndReviewPage(FormEditor editor, String title,
			MapControlCreator mapControlCreator) {
		super(editor, ID, title);
		this.mapControlCreator = mapControlCreator;
		try {
			Node session = getEditor().getCurrentSessionNode();
			NodeIterator ni = session.getNodes();
			while (ni.hasNext()) {
				Node node = ni.nextNode();
				if (node.getPrimaryNodeType().isNodeType(
						CONNECT_CLEAN_PARAMETER))
					paramNodeList.add(node);
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot initialize list of parameters", re);
		}
	}

	protected void createFormContent(IManagedForm managedForm) {

		// Initialize current form
		ScrolledForm form = managedForm.getForm();
		formToolkit = managedForm.getToolkit();
		Composite body = form.getBody();
		body.setLayout(new GridLayout(1, true));

		// Create and populate top part
		createParameterPart(body);

		// Create and populate bottom part
		createMapPart(body);

		try {
			addSpeedLayer(getCleanSession());
		} catch (Exception e) {
			ErrorFeedback
					.show("Cannot load speed layer. Did you import the GPX files from the previous tab?",
							e);
		}
	}

	private void createParameterPart(Composite top) {
		SliderViewerListener sliderViewerListener = new SliderViewerListener() {
			public void valueChanged(Double value) {
				refreshSpeedLayer(getCleanSession());
			}
		};
		Composite parent = formToolkit.createComposite(top);
		parent.setLayout(new GridLayout(6, false));
		maxSpeedViewer = new SliderViewer(formToolkit, parent, "Max Speed", 0d,
				250d, 200d);
		maxSpeedViewer.setListener(sliderViewerListener);
		maxAccelerationViewer = new SliderViewer(formToolkit, parent,
				"Max Acceleration", 0d, 5d, 2d);
		maxAccelerationViewer.setListener(sliderViewerListener);
		maxRotationViewer = new SliderViewer(formToolkit, parent,
				"Max Rotation", 0d, 360d, 90d);
		maxRotationViewer.setListener(sliderViewerListener);
		createValidationButtons(parent);
	}

	private void createValidationButtons(Composite parent) {
		GridData gridData;
		// visualize button
		// Button visualize = formToolkit.createButton(parent,
		// ConnectUiPlugin.getGPSMessage(VISUALIZE_BUTTON_LBL), SWT.PUSH);
		// Button visualize = formToolkit.createButton(parent, "EPSG:3857",
		// SWT.PUSH);
		// visualize.setToolTipText("Not working!!");
		// GridData gridData = new GridData();
		// gridData.horizontalAlignment = GridData.BEGINNING;
		// visualize.setLayoutData(gridData);
		//
		// Listener visualizeListener = new Listener() {
		// public void handleEvent(Event event) {
		// // mapViewer.setCoordinateReferenceSystem("EPSG:3857");
		// }
		// };
		// visualize.addListener(SWT.Selection, visualizeListener);

		// Terminate button
		Button terminate = formToolkit.createButton(parent,
				ConnectUiGpsPlugin.getGPSMessage(LAUNCH_CLEAN_BUTTON_LBL),
				SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.horizontalSpan = 2;// span text and slider above
		terminate.setLayoutData(gridData);

		Listener terminateListener = new Listener() {
			public void handleEvent(Event event) {
				getEditor().getTrackDao().publishCleanPositions(
						getCleanSession(), getReferential(),
						getToCleanCqlFilter());
				if (log.isDebugEnabled())
					log.debug("Official import completed");
				// TODO implement computation & corresponding UI workflow.
			}
		};
		terminate.addListener(SWT.Selection, terminateListener);
	}

	protected void createMapPart(Composite parent) {
		Composite mapArea = formToolkit.createComposite(parent, SWT.BORDER);
		mapArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		FillLayout layout = new FillLayout();
		mapArea.setLayout(layout);
		mapViewer = mapControlCreator.createMapControl(getEditor()
				.getCurrentSessionNode(), mapArea);
		getEditor().addBaseLayers(mapViewer);
	}

	/*
	 * GIS
	 */
	protected void addSpeedLayer(String cleanSession) {
		// Add speeds layer
		String trackSpeedsPath = getEditor().getTrackDao()
				.getTrackSpeedsSource(cleanSession);
		try {

			Node layerNode = getEditor().getCurrentSessionNode().getSession()
					.getNode(trackSpeedsPath);
			mapViewer.addLayer(layerNode, createToCleanStyle());

			// mapViewer.setCoordinateReferenceSystem("EPSG:3857");

			ReferencedEnvelope areaOfInterest = getFeatureSource(cleanSession)
					.getBounds();
			mapViewer.setAreaOfInterest(areaOfInterest);
		} catch (Exception e) {
			throw new ArgeoException("Cannot add layer " + trackSpeedsPath, e);
		}

	}

	protected FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(
			String cleanSession) {
		String trackSpeedsPath = getEditor().getTrackDao()
				.getTrackSpeedsSource(cleanSession);
		try {

			Node layerNode = getEditor().getCurrentSessionNode().getSession()
					.getNode(trackSpeedsPath);
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = mapViewer
					.getGeoJcrMapper().getFeatureSource(layerNode);
			return featureSource;
		} catch (Exception e) {
			throw new ArgeoException("Cannot get feature source "
					+ trackSpeedsPath, e);
		}
	}

	protected void refreshSpeedLayer(String cleanSession) {
		String trackSpeedsPath = getEditor().getTrackDao()
				.getTrackSpeedsSource(cleanSession);
		mapViewer.setStyle(trackSpeedsPath, createToCleanStyle());
	}

	protected Style createToCleanStyle() {
		Map<String, String> cqlFilters = new HashMap<String, String>();
		Double maxSpeed = maxSpeedViewer.getValue();
		cqlFilters.put("speed>" + maxSpeed, "GREEN");
		Double maxAbsoluteRotation = maxRotationViewer.getValue();
		cqlFilters.put("azimuthVariation<" + (-maxAbsoluteRotation)
				+ " OR azimuthVariation>" + maxAbsoluteRotation, "RED");
		Double maxAbsoluteAcceleration = maxAccelerationViewer.getValue();
		cqlFilters.put("acceleration<" + (-maxAbsoluteAcceleration)
				+ " OR acceleration>" + maxAbsoluteAcceleration, "BLUE");

		// Style style = StylingUtils.createFilteredLineStyle(
		// getToCleanCqlFilter(), "RED", 3, "BLACK", 1);
		Style style = StylingUtils.createFilteredLineStyle(cqlFilters, 3,
				"BLACK", 1);
		return style;
	}

	protected String getToCleanCqlFilter() {
		Double maxSpeed = maxSpeedViewer.getValue();
		Double maxAbsoluteRotation = maxRotationViewer.getValue();
		Double maxAbsoluteAcceleration = maxAccelerationViewer.getValue();
		String cql = "speed>" + maxSpeed + " OR azimuthVariation<"
				+ (-maxAbsoluteRotation) + " OR azimuthVariation>"
				+ maxAbsoluteRotation + " OR acceleration<"
				+ (-maxAbsoluteAcceleration) + " OR acceleration>"
				+ maxAbsoluteAcceleration;
		log.debug(cql);
		return cql;
	}

}
