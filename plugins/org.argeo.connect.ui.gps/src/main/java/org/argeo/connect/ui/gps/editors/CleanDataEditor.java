package org.argeo.connect.ui.gps.editors;

import java.math.BigDecimal;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.eclipse.ui.Error;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class CleanDataEditor extends EditorPart {
	private final static Log log = LogFactory.getLog(CleanDataEditor.class);

	public final static String ID = "org.argeo.connect.ui.gps.cleanDataEditor";

	// WARNING: this is experimental, no doc found on the subject, needs further
	// validation.
	private final int sliderMaxValueDef = 90;

	private CleanDataEditorInput input;

	private Text paramSetLabel;
	private Text paramSetComments;

	private Slider speedMinSlider;
	private Text speedMinValue;
	private Slider speedMaxSlider;
	private Text speedMaxValue;
	private Slider accelMinSlider;
	private Text accelMinValue;
	private Slider accelMaxSlider;
	private Text accelMaxValue;

	private MapControlCreator mapControlCreator;

	// Initialization of the editor; recovering old parameter set must be
	// handled here.
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof CleanDataEditorInput)) {
			throw new RuntimeException("Wrong input");
		}
		this.input = (CleanDataEditorInput) input;
		setSite(site);
		setInput(input);
	}

	public final void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL);
		sashForm.setSashWidth(4);
		sashForm.setLayout(new FillLayout());

		Composite top = new Composite(sashForm, SWT.NONE);
		createParameterPart(top);

		Composite mapArea = new Composite(sashForm, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mapArea.setLayout(layout);
		mapArea.setLayout(new GridLayout(1, false));
		createMapPart(mapArea);
	}

	protected void createMapPart(Composite parent) {
		Composite mapArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mapArea.setLayout(layout);
		// FIXME we need a node
		// Node editedNode = null;
		// MapViewer mapViewer = mapControlCreator.createMapControl(editedNode,
		// mapArea);
		// mapViewer.getControl().setLayoutData(
		// new GridData(SWT.FILL, SWT.FILL, true, true));

	}

	private void createParameterPart(Composite parent) {

		parent.setLayout(new GridLayout(3, false));
		GridData gd;

		// Name and comments
		Label label = new Label(parent, SWT.NONE);
		label.setText("Name for the current parameter set :");
		paramSetLabel = new Text(parent, SWT.FILL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		paramSetLabel.setLayoutData(gd);

		label = new Label(parent, SWT.NONE);
		label.setText("Comments :");
		paramSetComments = new Text(parent, SWT.FILL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		paramSetComments.setLayoutData(gd);

		float defaultValue;
		// speed min value
		defaultValue = (getStoredValue("speedMin") == null ? 0f
				: getStoredValue("speedMin").floatValue());
		createLineWithSlider(parent, "Enter minimal acceptable speed value :",
				speedMinSlider, speedMinValue, 0, -1000, defaultValue);

		// speed max value
		defaultValue = (getStoredValue("speedMax") == null ? 500f
				: getStoredValue("speedMax").floatValue());
		createLineWithSlider(parent, "Enter maximal acceptable speed value :",
				speedMaxSlider, speedMaxValue, 2000, 0, defaultValue);

		// acceleration min value
		defaultValue = (getStoredValue("accelMin") == null ? -1000f
				: getStoredValue("accelMin").floatValue());
		createLineWithSlider(parent,
				"Enter minimal acceptable acceleration value :",
				accelMinSlider, accelMinValue, 1000, -1000, defaultValue);

		// acceleration max value
		defaultValue = (getStoredValue("accelMax") == null ? 1000f
				: getStoredValue("accelMax").floatValue());
		createLineWithSlider(parent,
				"Enter maximal acceptable acceleration value :",
				accelMaxSlider, accelMaxValue, 1000, -1000, defaultValue);

		// Validation buttons
		addValidationButton(parent);
	}

	private void addValidationButton(Composite parent) {

		// visualize button
		Button visualize = new Button(parent, SWT.PUSH);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		visualize.setLayoutData(gridData);
		visualize.setText("Pre-visualize clean data");

		Listener visualizeListener = new Listener() {
			public void handleEvent(Event event) {

				if (log.isDebugEnabled())
					log.debug("Execute computation");
				// TODO implement computation
				// TODO Handle UI workflow.
			}
		};

		visualize.addListener(SWT.Selection, visualizeListener);

		// Terminate button
		Button terminate = new Button(parent, SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		terminate.setLayoutData(gridData);
		terminate.setText("Launch effective process");

		Listener terminateListener = new Listener() {
			public void handleEvent(Event event) {

				if (log.isDebugEnabled())
					log.debug("Terminating process");
				// TODO implement computation
				// TODO Handle UI workflow.
			}
		};

		terminate.addListener(SWT.Selection, terminateListener);

	}

	// TODO : implement this method to be able to retrieve stored value
	private Float getStoredValue(String keyId) {
		return null;
	}

	// Factorises creation of lines with slider for various paramaters
	private void createLineWithSlider(Composite parent, String labelStr,
			Slider slider, Text text, float maxValue, float minValue,
			float storedValue) {

		Label label = new Label(parent, SWT.NONE);
		label.setText(labelStr);

		text = new Text(parent, 0);
		slider = new Slider(parent, SWT.HORIZONTAL);

		// Layout management
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		text.setLayoutData(gd);

		// Initialize values
		text.setText((new Float(storedValue)).toString());
		// move slider accordingly
		float a = storedValue - minValue;
		float b = maxValue - minValue;
		float c = sliderMaxValueDef;
		float percent = a / b * c;
		int result = Math.round(percent);
		slider.setSelection(result);

		addListenersToLine(slider, text, maxValue, minValue, storedValue);
	}

	// used to force object to be final
	private void addListenersToLine(final Slider slider, final Text text,
			final float maxValue, final float minValue, final float storedValue) {

		slider.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// compute the value depending its boundaries
				float a = slider.getSelection();
				float b = sliderMaxValueDef;
				float percent = a / b;
				float result = minValue + percent * (maxValue - minValue);
				BigDecimal param = new BigDecimal(result).setScale(2,
						BigDecimal.ROUND_HALF_EVEN);
				text.setText(param.toString());
			}
		});

		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				String valueStr = text.getText();
				float value;

				try {
					value = (new Float(valueStr)).floatValue();
				} catch (Exception e) {
					Error.show(
							"Value "
									+ valueStr
									+ " is not valid, please enter a number within the following range : ["
									+ minValue + ", " + maxValue + "]", e);
					return;
				}

				// Check boundaries
				if (value < minValue || value > maxValue) {
					Error.show("Value "
							+ value
							+ " is not within acceptable range, must be within : ["
							+ minValue + ", " + maxValue + "]");
				}

				// move slider accordingly
				float a = value - minValue;
				float b = maxValue - minValue;
				float c = sliderMaxValueDef;
				float percent = a / b * c;
				int result = Math.round(percent);
				slider.setSelection(result);
			}
		});

	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
