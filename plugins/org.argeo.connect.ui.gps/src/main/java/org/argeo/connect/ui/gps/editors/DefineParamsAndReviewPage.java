/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.gps.GpsConstants;
import org.argeo.connect.gps.TrackSpeed;
import org.argeo.connect.ui.gps.ConnectGpsLabels;
import org.argeo.connect.ui.gps.ConnectGpsUiPlugin;
import org.argeo.connect.ui.gps.GpsStyling;
import org.argeo.connect.ui.gps.GpsUiGisServices;
import org.argeo.connect.ui.gps.GpsUiJcrServices;
import org.argeo.connect.ui.gps.commons.SliderViewer;
import org.argeo.connect.ui.gps.commons.SliderViewerListener;
import org.argeo.connect.ui.gps.views.GpsBrowserView;
import org.argeo.eclipse.ui.ErrorFeedback;
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

public class DefineParamsAndReviewPage extends AbstractCleanDataEditorPage
		implements ConnectNames, GpsConstants {
	private final static Log log = LogFactory
			.getLog(DefineParamsAndReviewPage.class);
	public final static String ID = "cleanDataEditor.defineParamsAndReviewPage";

	// This page widget
	private FormToolkit formToolkit;
	private SliderViewer maxSpeedViewer;
	private SliderViewer maxAccelerationViewer;
	private SliderViewer maxRotationViewer;
	private SliderViewer maxVerticalSpeedViewer;

	private Button preview;
	private Button lines;
	private Button points;

	private MapViewer mapViewer;

	// Local object used as cache
	private GpsUiJcrServices uiJcrServices;
	private GpsUiGisServices uiGisServices;
	private Node currCleanSession;

	public DefineParamsAndReviewPage(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	protected void createFormContent(IManagedForm managedForm) {
		// initialize cache
		uiJcrServices = getEditor().getUiJcrServices();
		uiGisServices = getEditor().getUiGisServices();
		currCleanSession = getEditor().getCurrentCleanSession();

		// Initialize current form
		ScrolledForm form = managedForm.getForm();
		formToolkit = managedForm.getToolkit();
		Composite body = form.getBody();
		body.setLayout(new GridLayout(1, true));

		// Create and populate top part
		createParameterPart(body);
		// Create and populate bottom part
		createMapPart(body);

		// TODO Enable this @ RAP WORKAROUND FOR LEUVEN PROJECT
		try {
			addSpeedLayer(uiJcrServices
					.getCleanSessionTechName(currCleanSession));
		} catch (Exception e) {
			ErrorFeedback
					.show("Cannot load speed layer. Did you import the GPX files from the previous tab?",
							e);
			getEditor().setActivePage(GpxFilesProcessingPage.ID);
		}
	}

	protected void createParameterPart(Composite top) {
		Composite parent = formToolkit.createComposite(top);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		parent.setLayout(new GridLayout(6, false));

		try {
			double min, max, val;
			ParamFormPart paramPart = null;
			ParamSliderViewerListener sliderViewerListener;

			// Max rotation
			if (currCleanSession.hasNode(CONNECT_PARAM_ROTATION_MAX)) {
				Node curNode = currCleanSession
						.getNode(CONNECT_PARAM_ROTATION_MAX);
				min = curNode.getProperty(CONNECT_PARAM_MIN_VALUE).getDouble();
				max = curNode.getProperty(CONNECT_PARAM_MAX_VALUE).getDouble();
				val = curNode.getProperty(CONNECT_PARAM_VALUE).getDouble();
				maxRotationViewer = new SliderViewer(formToolkit, parent,
						"Rotation", min, max, val, SWT.COLOR_RED);
				paramPart = new ParamFormPart(curNode);
				sliderViewerListener = new ParamSliderViewerListener(paramPart);
				maxRotationViewer.setListener(sliderViewerListener);
				getManagedForm().addPart(paramPart);
			}
			// Max vertical speed
			if (currCleanSession.hasNode(CONNECT_PARAM_VERTICAL_SPEED_MAX)) {
				Node curNode = currCleanSession
						.getNode(CONNECT_PARAM_VERTICAL_SPEED_MAX);
				min = curNode.getProperty(CONNECT_PARAM_MIN_VALUE).getDouble();
				max = curNode.getProperty(CONNECT_PARAM_MAX_VALUE).getDouble();
				val = curNode.getProperty(CONNECT_PARAM_VALUE).getDouble();
				maxVerticalSpeedViewer = new SliderViewer(formToolkit, parent,
						"Vertical Speed", min, max, val, SWT.COLOR_GREEN);
				paramPart = new ParamFormPart(curNode);
				sliderViewerListener = new ParamSliderViewerListener(paramPart);
				maxVerticalSpeedViewer.setListener(sliderViewerListener);
				getManagedForm().addPart(paramPart);
			}
			// Max speed
			if (currCleanSession.hasNode(CONNECT_PARAM_SPEED_MAX)) {
				Node curNode = currCleanSession
						.getNode(CONNECT_PARAM_SPEED_MAX);
				min = curNode.getProperty(CONNECT_PARAM_MIN_VALUE).getDouble();
				max = curNode.getProperty(CONNECT_PARAM_MAX_VALUE).getDouble();
				val = curNode.getProperty(CONNECT_PARAM_VALUE).getDouble();
				maxSpeedViewer = new SliderViewer(formToolkit, parent, "Speed",
						min, max, val, SWT.COLOR_BLUE);
				paramPart = new ParamFormPart(curNode);
				sliderViewerListener = new ParamSliderViewerListener(paramPart);
				maxSpeedViewer.setListener(sliderViewerListener);
				getManagedForm().addPart(paramPart);
			}
			// Max acceleration
			if (currCleanSession.hasNode(CONNECT_PARAM_ACCELERATION_MAX)) {
				Node curNode = currCleanSession
						.getNode(CONNECT_PARAM_ACCELERATION_MAX);
				min = curNode.getProperty(CONNECT_PARAM_MIN_VALUE).getDouble();
				max = curNode.getProperty(CONNECT_PARAM_MAX_VALUE).getDouble();
				val = curNode.getProperty(CONNECT_PARAM_VALUE).getDouble();
				maxAccelerationViewer = new SliderViewer(formToolkit, parent,
						"Acceleration", min, max, val, SWT.COLOR_YELLOW);
				paramPart = new ParamFormPart(curNode);
				sliderViewerListener = new ParamSliderViewerListener(paramPart);
				maxAccelerationViewer.setListener(sliderViewerListener);
				getManagedForm().addPart(paramPart);
			}

			createDisplayControls(parent);
			createValidationButtons(parent);

		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot initialize list of parameters", re);
		}
	}

	protected void createDisplayControls(Composite parent) {
		Composite panel = formToolkit.createComposite(parent);
		panel.setLayout(new FillLayout());
		panel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false,
				false, 3, 1));

		lines = formToolkit.createButton(panel, "Lines", SWT.RADIO);
		lines.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				refreshSpeedLayer();
			}
		});
		lines.setSelection(true);

		points = formToolkit.createButton(panel, "Points", SWT.RADIO);
		points.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				refreshSpeedLayer();
			}
		});

		preview = formToolkit.createButton(panel, "Preview", SWT.CHECK);
		preview.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				refreshSpeedLayer();
			}
		});
	}

	protected void createValidationButtons(Composite parent) {
		Composite panel = formToolkit.createComposite(parent);
		panel.setLayout(new FillLayout());
		panel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false,
				false, 3, 1));

		// GridData gridData;

		// Terminate button
		Button terminate = formToolkit.createButton(panel, ConnectGpsUiPlugin
				.getGPSMessage(ConnectGpsLabels.LAUNCH_CLEAN_BUTTON_LBL),
				SWT.PUSH);
		// gridData = new GridData();
		// gridData.horizontalAlignment = GridData.BEGINNING;
		// gridData.horizontalSpan = 2;// span text and slider above
		// terminate.setLayoutData(gridData);

		Listener terminateListener = new Listener() {
			public void handleEvent(Event event) {

				GpsUiJcrServices uiServices = getEditor().getUiJcrServices();
				Node currCS = getEditor().getCurrentCleanSession();

				if (uiServices.getLinkedReferential(currCS) == null) {
					ErrorFeedback
							.show("Please configure a valid referential as a target for the data import.");
				} else if (getEditor().isDirty())
					ErrorFeedback
							.show("Please save the session prior to try launching the real import.");
				else {
					pushDataToLocalRepo();
					MessageDialog dialog = new MessageDialog(
							getSite().getShell(),
							"Import done",
							null,
							"Clean data have been pushed to referential.\n"
									+ uiServices
											.getReferentialDisplayName(uiServices
													.getLinkedReferential(currCS))
									+ "Current clean session is now read only.",
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
		GridLayout layout = new GridLayout();
		mapArea.setLayout(layout);
		// TODO Enable this @ RAP WORKAROUND FOR LEUVEN PROJECT
		MapControlCreator mcc = uiGisServices.getMapControlCreator();
		mapViewer = mcc.createMapControl(currCleanSession, mapArea);
		mapViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

		mapViewer.setCoordinateReferenceSystem(ConnectGpsUiPlugin.EPSG_3857);
		uiGisServices.addBaseLayers(mapViewer);
	}

	protected void pushDataToLocalRepo() {
		uiGisServices.getTrackDao().publishCleanPositions(
				uiJcrServices.getCleanSessionTechName(currCleanSession),
				uiJcrServices.getLinkedReferentialTechName(currCleanSession),
				getToCleanCqlFilter());

		try {
			// to be able to refresh after the move.
			Node sessionPar = currCleanSession.getParent();
			// prevent further modification of the Current clean session
			currCleanSession.setProperty(CONNECT_IS_SESSION_COMPLETE, true);
			JcrUtils.updateLastModified(currCleanSession);
			currCleanSession.getSession().save();

			// When a session has been completed, we must access it via
			// the corresponding Local Repository to enable further
			// delete.
			Node currRepo = uiJcrServices
					.getLinkedReferential(currCleanSession);
			Workspace ws = currRepo.getSession().getWorkspace();
			ws.move(currCleanSession.getPath(), currRepo.getPath() + "/"
					+ currCleanSession.getName());

			getEditor().refreshReadOnlyState();

			GpsBrowserView gbView = (GpsBrowserView) ConnectGpsUiPlugin
					.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(GpsBrowserView.ID);
			gbView.refresh(sessionPar);
			gbView.refresh(currRepo);
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while finalising import", re);
		}
	}

	/*
	 * GIS
	 */
	protected void addSpeedLayer(String cleanSession) {
		// Add speeds layer
		String trackSpeedsPath = uiGisServices.getTrackDao()
				.getTrackSpeedsSource(cleanSession);
		try {

			Node layerNode = currCleanSession.getSession().getNode(
					trackSpeedsPath);
			// mapViewer.addLayer(layerNode,
			// createToCleanStyle(TrackSpeed.LINE));
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
		String trackSpeedsPath = uiGisServices.getTrackDao()
				.getTrackSpeedsSource(cleanSession);
		try {
			Node layerNode = currCleanSession.getSession().getNode(
					trackSpeedsPath);
			FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = mapViewer
					.getGeoJcrMapper().getFeatureSource(layerNode);
			return featureSource;
		} catch (Exception e) {
			throw new ArgeoException("Cannot get feature source "
					+ trackSpeedsPath, e);
		}
	}

	protected void refreshSpeedLayer() {
		String cleanSession = uiJcrServices
				.getCleanSessionTechName(currCleanSession);
		String trackSpeedsPath = uiGisServices.getTrackDao()
				.getTrackSpeedsSource(cleanSession);
		mapViewer.setStyle(trackSpeedsPath, createToCleanStyle());
	}

	protected Style createToCleanStyle() {
		String displayedField = lines.getSelection() ? TrackSpeed.LINE
				: TrackSpeed.POSITION;
		return GpsStyling.createGpsCleanStyle(displayedField,
				preview.getSelection(), maxSpeedViewer.getValue(),
				maxAccelerationViewer.getValue(), maxRotationViewer.getValue(),
				maxVerticalSpeedViewer.getValue());
	}

	protected String getToCleanCqlFilter() {
		Double maxAbsoluteRotation = maxRotationViewer.getValue();
		Double maxAbsoluteVerticalSpeed = maxVerticalSpeedViewer.getValue();
		Double maxSpeed = maxSpeedViewer.getValue();
		Double maxAbsoluteAcceleration = maxAccelerationViewer.getValue();
		String cql = "speed>" + maxSpeed + " OR azimuthVariation<"
				+ (-maxAbsoluteRotation) + " OR azimuthVariation>"
				+ maxAbsoluteRotation + " OR verticalSpeed<"
				+ (-maxAbsoluteVerticalSpeed) + " OR verticalSpeed>"
				+ maxAbsoluteVerticalSpeed + " OR acceleration<"
				+ (-maxAbsoluteAcceleration) + " OR acceleration>"
				+ maxAbsoluteAcceleration;
		if (log.isDebugEnabled())
			log.debug(cql);
		return cql;
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
						paramNode.setProperty(CONNECT_PARAM_VALUE, paramValue);
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
			// TODO Enable this @ RAP WORKAROUND FOR LEUVEN PROJECT
			refreshSpeedLayer();
			formPart.setValue(value);
			formPart.markDirty();
		}
	};

}