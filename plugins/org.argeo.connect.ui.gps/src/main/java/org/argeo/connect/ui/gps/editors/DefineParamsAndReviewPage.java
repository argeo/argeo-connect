package org.argeo.connect.ui.gps.editors;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.argeo.connect.ui.gps.commons.SliderViewer;
import org.argeo.connect.ui.gps.commons.SliderViewerListener;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.AbstractFormPart;
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
	// private List<Node> paramNodeList = new ArrayList<Node>();;

	private FormToolkit formToolkit;

	private SliderViewer maxSpeedViewer;
	private SliderViewer maxAccelerationViewer;
	private SliderViewer maxRotationViewer;

	private MapControlCreator mapControlCreator;
	private MapViewer mapViewer;

	// FIXME retrieve a proper name
	private String getReferential() {
		return "HARDCODED";
	}

	public DefineParamsAndReviewPage(FormEditor editor, String title,
			MapControlCreator mapControlCreator) {
		super(editor, ID, title);
		this.mapControlCreator = mapControlCreator;

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

		Composite parent = formToolkit.createComposite(top);
		parent.setLayout(new GridLayout(6, false));

		try {
			double min, max, val;
			ParamFormPart paramPart = null;
			ParamSliderViewerListener sliderViewerListener;
			Node sessionNode = getEditor().getCurrentSessionNode();

			// Max speed
			if (sessionNode.hasNode(ConnectConstants.CONNECT_PARAM_SPEED_MAX)) {
				Node curNode = sessionNode
						.getNode(ConnectConstants.CONNECT_PARAM_SPEED_MAX);
				min = curNode.getProperty(ConnectNames.CONNECT_PARAM_MIN_VALUE)
						.getDouble();
				max = curNode.getProperty(ConnectNames.CONNECT_PARAM_MAX_VALUE)
						.getDouble();
				val = curNode.getProperty(ConnectNames.CONNECT_PARAM_VALUE)
						.getDouble();
				maxSpeedViewer = new SliderViewer(formToolkit, parent,
						"Max Speed", min, max, val);
				paramPart = new ParamFormPart(curNode);
				sliderViewerListener = new ParamSliderViewerListener(paramPart);
				maxSpeedViewer.setListener(sliderViewerListener);
				getManagedForm().addPart(paramPart);
			}
			// Max acceleration
			if (sessionNode
					.hasNode(ConnectConstants.CONNECT_PARAM_ACCELERATION_MAX)) {
				Node curNode = sessionNode
						.getNode(ConnectConstants.CONNECT_PARAM_ACCELERATION_MAX);
				min = curNode.getProperty(ConnectNames.CONNECT_PARAM_MIN_VALUE)
						.getDouble();
				max = curNode.getProperty(ConnectNames.CONNECT_PARAM_MAX_VALUE)
						.getDouble();
				val = curNode.getProperty(ConnectNames.CONNECT_PARAM_VALUE)
						.getDouble();
				maxAccelerationViewer = new SliderViewer(formToolkit, parent,
						"Max Acceleration", min, max, val);
				paramPart = new ParamFormPart(curNode);
				sliderViewerListener = new ParamSliderViewerListener(paramPart);
				maxAccelerationViewer.setListener(sliderViewerListener);
				getManagedForm().addPart(paramPart);
			}
			// Max rotation
			if (sessionNode
					.hasNode(ConnectConstants.CONNECT_PARAM_ROTATION_MAX)) {
				Node curNode = sessionNode
						.getNode(ConnectConstants.CONNECT_PARAM_ROTATION_MAX);
				min = curNode.getProperty(ConnectNames.CONNECT_PARAM_MIN_VALUE)
						.getDouble();
				max = curNode.getProperty(ConnectNames.CONNECT_PARAM_MAX_VALUE)
						.getDouble();
				val = curNode.getProperty(ConnectNames.CONNECT_PARAM_VALUE)
						.getDouble();
				maxRotationViewer = new SliderViewer(formToolkit, parent,
						"Max Rotation", min, max, val);
				paramPart = new ParamFormPart(curNode);
				sliderViewerListener = new ParamSliderViewerListener(paramPart);
				maxRotationViewer.setListener(sliderViewerListener);
				getManagedForm().addPart(paramPart);
			}

			createValidationButtons(parent);

		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot initialize list of parameters", re);
		}

	}

	// Inner classes to handle param changes
	private class ParamFormPart extends AbstractFormPart {

		private Node paramNode;
		private Double paramValue = null;

		public ParamFormPart(Node paramNode) {
			this.paramNode = paramNode;
		}

		public void setValue(Double value) {
			paramValue = value;
		}

		public void commit(boolean onSave) {
			if (onSave)
				try {
					if (paramValue != null)
						paramNode.setProperty(ConnectNames.CONNECT_PARAM_VALUE,
								paramValue);
					super.commit(onSave);
				} catch (RepositoryException re) {
					throw new ArgeoException(
							"unexpected error while saving parameter value.",
							re);
				}
			else if (log.isDebugEnabled())
				log.debug("commit(false)");
		}
	}

	private class ParamSliderViewerListener implements SliderViewerListener {

		private ParamFormPart formPart;

		public ParamSliderViewerListener(ParamFormPart paramFormPart) {
			this.formPart = paramFormPart;
		}

		public void valueChanged(Double value) {
			refreshSpeedLayer(getCleanSession());
			formPart.setValue(value);
			formPart.markDirty();
		}
	};

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

				if (getReferential() == null) {
					ErrorFeedback
							.show("Please configure a valid referential as a target for the data import.");
				}// else if (getManagedForm().isDirty())
					// we rather use this one to be sure that ALL pages of the
					// editor have been changed.
				else if (getEditor().isDirty())
					ErrorFeedback
							.show("Please save the session prior to try launching the real import.");
				else {
					getEditor().getTrackDao().publishCleanPositions(
							getCleanSession(), getReferential(),
							getToCleanCqlFilter());
					// TODO implement computation & corresponding UI workflow.

					// prevent further modification of the Current clean session
					try {
						Node sessionNode = getEditor().getCurrentSessionNode();
						sessionNode.setProperty(
								ConnectNames.CONNECT_IS_SESSION_COMPLETE, true);
						JcrUtils.updateLastModified(sessionNode);
						sessionNode.getSession().save();
						((CleanDataEditor) getEditor()).refreshReadOnlyState();

					} catch (RepositoryException re) {
						throw new ArgeoException(
								"Unexpected error while finalising import", re);
					}
					MessageDialog dialog = new MessageDialog(
							getSite().getShell(),
							"Import done",
							null,
							"Clean data have been pushed to referential "
									+ getReferential()
									+ "\n Current clean session is now read only.",
							SWT.NONE, new String[] { "OK" }, 0);

					dialog.open();
				}
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
