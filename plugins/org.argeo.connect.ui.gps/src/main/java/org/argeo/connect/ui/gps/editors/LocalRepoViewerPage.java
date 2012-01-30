package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;

import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.swt.SWT;
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
	// private final static Log log =
	// LogFactory.getLog(LocalRepoViewerPage.class);
	public final static String ID = "localRepoEditor.localRepoViewerPage";

	private MapControlCreator mapControlCreator;
	private MapViewer mapViewer;
	private FormToolkit ft;

	private Button displayAllSensorsChk, showBaseLayerChk;
	private Combo chooseUserCmb;

	public LocalRepoViewerPage(FormEditor editor, String title,
			MapControlCreator mapControlCreator) {
		super(editor, ID, title);
		this.mapControlCreator = mapControlCreator;

	}

	public LocalRepoEditor getEditor() {
		return (LocalRepoEditor) super.getEditor();
	}

	private String getReferential() {
		return getEditor().getEditorInput().getName();
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
		ft.createLabel(parent,"Choose a specific sensor:", SWT.NONE);
		chooseUserCmb = new Combo(parent, SWT.BORDER | SWT.READ_ONLY
				| SWT.V_SCROLL);
		gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		chooseUserCmb.setLayoutData(gd);
		populateChooseUserCmb(chooseUserCmb);

		// Manage layers to display
		displayAllSensorsChk = ft.createButton(parent, "Show all sensors", SWT.CHECK | SWT.LEFT);
		showBaseLayerChk  = ft.createButton(parent, "Show base layer", SWT.CHECK | SWT.LEFT);
		Listener executeListener = new Listener() {
			public void handleEvent(Event event) {
				displayAllSensors();
			}
		};
		displayAllSensorsChk.addListener(SWT.Selection, executeListener);
		showBaseLayerChk.addListener(SWT.Selection, executeListener);
	}

	private void populateChooseUserCmb(Combo combo) {
		// TODO implement
	}

	private void displayAllSensors() {
		// TODO implement
	}

	protected void createMapPart(Composite parent) {
		Composite mapArea = getManagedForm().getToolkit().createComposite(
				parent, SWT.BORDER);
		mapArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		FillLayout layout = new FillLayout();
		mapArea.setLayout(layout);
		mapViewer = mapControlCreator.createMapControl(getEditor()
				.getCurrentRepoNode(), mapArea);
		getEditor().addBaseLayers(mapViewer);
	}

	/*
	 * GIS
	 */
	protected void addPositionsLayer() {
		// Add speeds layer
		String positionsDisplayPath = getEditor().getTrackDao()
				.getPositionsDisplaySource(getReferential());
		try {
			Node layerNode = getEditor().getCurrentRepoNode().getSession()
					.getNode(positionsDisplayPath);
			Style style = StylingUtils.createLineStyle("BLACK", 1);
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
		String trackSpeedsPath = getEditor().getTrackDao().getPositionsSource(
				getReferential());
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

}
