package org.argeo.connect.ui.gps.editors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.SimpleFormatter;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.connect.ui.ConnectUiPlugin;
import org.argeo.eclipse.ui.Error;
import org.argeo.geotools.styling.StylingUtils;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class DefineParamsAndReviewPage extends AbstractCleanDataEditorPage {
	private final static Log log = LogFactory
			.getLog(DefineParamsAndReviewPage.class);
	public final static String ID = "cleanDataEditor.defineParamsAndReviewPage";

	// WARNING: this is experimental, no doc found on the subject, needs further
	// validation.
	private final int sliderMaxValueDef = 90;

	// Defined parameters
	private List<Node> paramNodeList = new ArrayList<Node>();;

	private FormToolkit formToolkit;

	private SliderViewer maxSpeedViewer;
	private SliderViewer maxAccelerationViewer;
	private SliderViewer maxRotationViewer;

	private MapControlCreator mapControlCreator;
	private MapViewer mapViewer;

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

		// create and initialize sashForm
		// SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		// sashForm.setSashWidth(4);
		// sashForm.setLayout(new FillLayout());
		// formToolkit.adapt(sashForm);

		// Create and populate top part
		createParameterPart(body);

		// Create and populate bottom part
		createMapPart(body);

		addSpeedLayer();
	}

	private void createParameterPart(Composite top) {
		SliderViewerListener sliderViewerListener = new SliderViewerListener() {
			public void valueChanged(Double value) {
				refreshSpeedLayer();
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
		// visualize button
		Button visualize = formToolkit.createButton(parent,
				ConnectUiPlugin.getGPSMessage(VISUALIZE_BUTTON_LBL), SWT.PUSH);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		visualize.setLayoutData(gridData);

		Listener visualizeListener = new Listener() {
			public void handleEvent(Event event) {

				if (log.isDebugEnabled())
					log.debug("Execute computation");
				// TODO implement computation & corresponding UI workflow.
			}
		};
		visualize.addListener(SWT.Selection, visualizeListener);

		// Terminate button
		Button terminate = formToolkit.createButton(parent,
				ConnectUiPlugin.getGPSMessage(LAUNCH_CLEAN_BUTTON_LBL),
				SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.horizontalSpan = 2;// span text and slider above
		terminate.setLayoutData(gridData);

		Listener terminateListener = new Listener() {
			public void handleEvent(Event event) {

				if (log.isDebugEnabled())
					log.debug("Terminating process");
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

	/** @deprecated */
	private Section createParameterPartOld(Composite parent) {

		// Section
		final Section section = formToolkit.createSection(parent,
				Section.DESCRIPTION | Section.TWISTIE | Section.EXPANDED);
		section.setText(ConnectUiPlugin.getGPSMessage(PARAMS_SECTION_TITLE));
		formToolkit.createCompositeSeparator(section);
		section.setDescription(ConnectUiPlugin
				.getGPSMessage(PARAMS_SECTION_DESC));

		// TODO : add here an expansion listener to reset sashForm sizes when
		// expansion state changes.

		// Section client
		addLinesToSection(section);

		AbstractFormPart part = new SectionPart(section) {
			public void commit(boolean onSave) {
				// implements here what must be done while committing and saving
				// (if onSave = true)
				super.commit(onSave);
			}

			public void refresh() {
				super.refresh();
				// if (log.isDebugEnabled())
				// log.debug("Refresh in progress.");
				addLinesWithDispose(section);
				// addLinesToSection(section);
			}
		};
		getManagedForm().addPart(part);
		return section;
	}

	/** used in the refresh process to insure displayed data are up to date */
	private void addLinesWithDispose(Section section) {
		Control oldBody = section.getClient();
		if (oldBody != null)
			// log.debug("do nothing");
			oldBody.dispose();
		// section.getParent().pack(true);
		addLinesToSection(section);
	}

	/** Create body of parameter section */
	private void addLinesToSection(Section section) {
		Composite newBody = formToolkit.createComposite(section);
		newBody.setLayout(new GridLayout(3, false));
		section.setClient(newBody);

		Iterator<Node> it = paramNodeList.iterator();
		try {
			while (it.hasNext()) {
				Node node = it.next();
				String name = node.getName();
				if (node.getProperty(CONNECT_PARAM_IS_USED).getBoolean()) {
					// log.debug("Creating line for param : " + name);
					createLineWithSlider(newBody, node);
				}
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot instantiate parameters widgets ",
					re);
		}

		// newBody.changed(controls);
		// section.pack(true);
		// section.update();

		// Use to force the section to take newly created widget into account
		section.layout();
		// Re-compute size & layout of the "top" part of the sash form.
		section.getParent().pack(true);
	}

	private Composite createLineWithSlider(Composite composite, Node curNode) {
		Text text;
		Slider slider;
		String curLbl;
		String minValueTxt, maxValueTxt;

		try {
			// Sanity check
			if (!curNode.getPrimaryNodeType().isNodeType(
					CONNECT_CLEAN_PARAMETER))
				throw new ArgeoException("Invalid node type, must be of type "
						+ CONNECT_CLEAN_PARAMETER);
			// Get values
			Double storedValue = curNode.getProperty(CONNECT_PARAM_VALUE)
					.getDouble();
			Double minValue = curNode.getProperty(CONNECT_PARAM_MIN_VALUE)
					.getDouble();
			Double maxValue = curNode.getProperty(CONNECT_PARAM_MAX_VALUE)
					.getDouble();

			// update storedvalue if it's outside of new boundaries
			if (storedValue < minValue || storedValue > maxValue) {
				Double result = (maxValue - minValue) / 2;
				storedValue = new Double((new BigDecimal(result).setScale(2,
						BigDecimal.ROUND_HALF_EVEN)).floatValue());
				curNode.setProperty(CONNECT_PARAM_VALUE, storedValue);
				getEditor().getCurrentSessionNode().getSession().save();
			}

			minValueTxt = Double.toString(minValue);
			maxValueTxt = Double.toString(maxValue);
			curLbl = curNode.getProperty(CONNECT_PARAM_LABEL).getString()
					+ " [" + minValueTxt + "; " + maxValueTxt + "] :";

			// create widgets
			Label label = formToolkit.createLabel(composite, curLbl);
			text = formToolkit.createText(composite,
					Double.toString(storedValue));
			GridData txtGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			txtGd.widthHint = 50;
			text.setLayoutData(txtGd);
			slider = new Slider(composite, SWT.HORIZONTAL);

			// move slider accordingly
			Double a = storedValue - minValue;
			Double b = maxValue - minValue;
			Double c = new Double(sliderMaxValueDef);
			Double percent = a / b * c;
			int result = (int) Math.round(percent);
			slider.setSelection(result);
			addListenersToLine(slider, text, maxValue, minValue, storedValue);

		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Cannot construct define parameter set UI from node", re);
		}
		return composite;
	}

	// method introduced to force object to be final
	private void addListenersToLine(final Slider slider, final Text text,
			final Double maxValue, final Double minValue,
			final Double storedValue) {

		// slider.addMouseListener(new MouseListener() {
		//
		// @Override
		// public void mouseUp(MouseEvent e) {
		// }
		//
		// @Override
		// public void mouseDown(MouseEvent e) {
		// log.debug("Mouse down on slider ");
		// slider.setFocus();
		// log.debug("Focus control ? : " + slider.isFocusControl());
		// }
		//
		// @Override
		// public void mouseDoubleClick(MouseEvent e) {
		// }
		// });

		slider.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// log.debug("Focus control ? : " + slider.isFocusControl());
				// // VERY IMPORTANT : otherwise UI crash on value change.
				// if (!slider.isFocusControl())
				// return;

				// compute the value depending its boundaries
				float a = slider.getSelection();
				float b = sliderMaxValueDef;
				float percent = a / b;
				Double result = minValue + percent * (maxValue - minValue);
				BigDecimal param = new BigDecimal(result).setScale(2,
						BigDecimal.ROUND_HALF_EVEN);
				text.setText(param.toString());
			}
		});

		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				// VERY IMPORTANT : otherwise UI crash on value change.
				if (!text.isFocusControl())
					return;

				String valueStr = text.getText();
				Double value;

				try {
					value = Double.parseDouble(valueStr);
				} catch (NumberFormatException e) {
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
				Double a = value - minValue;
				Double b = maxValue - minValue;
				Double c = new Double(sliderMaxValueDef);
				Double percent = a / b * c;
				int result = (int) Math.round(percent);
				slider.setSelection(result);
			}
		});

	}

	// Inner Helpers
	private Node getParameterNode(String paramName) {
		try {
			return getEditor().getCurrentSessionNode().getNode(paramName);
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot get node parameter named "
					+ paramName, re);
		}
	}

	/*
	 * UI UTILITIES
	 */
	/** TODO factorize it in Argeo Commons as a generic component ? */
	static class SliderViewer {
		private final static Integer MIN_SLIDER = 0;
		private final static Integer MAX_SLIDER = 100;

		private String label;
		private Double maxValue;
		private Double minValue;
		private Double defaultValue;

		private FormToolkit formToolkit;
		private Label lbl;
		private Slider slider;
		private Text txt;

		/** TODO multiple listeners */
		private SliderViewerListener listener;

		public SliderViewer(FormToolkit formToolkit, Composite parent,
				String label, Double minValue, Double maxValue,
				Double defaultValue) {
			this.formToolkit = formToolkit;
			this.label = label;
			this.minValue = minValue;
			if (maxValue <= minValue)
				throw new ArgeoException("Max value " + maxValue
						+ " is lesser or equal than min value " + minValue);
			this.maxValue = maxValue;
			if (defaultValue < minValue || defaultValue > maxValue)
				throw new ArgeoException("Default value " + defaultValue
						+ " is not within acceptable range [" + minValue + ", "
						+ maxValue + "]");
			this.defaultValue = defaultValue;

			createControls(parent);
			addListeners();

			slider.setSelection(convertToSliderSelection(this.defaultValue));
			txt.setText(Double.toString(defaultValue));
		}

		protected void createControls(Composite parent) {
			lbl = formToolkit.createLabel(parent, getDisplayedLabel());

			slider = new Slider(parent, SWT.NONE);
			slider.setMinimum(MIN_SLIDER);
			slider.setMaximum(MAX_SLIDER);

			txt = formToolkit.createText(parent, "");
		}

		protected void addListeners() {
			// FIXME are we in stack overflow?(does each event trigger the
			// other?)

			slider.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					Double value = getValue();
					BigDecimal param = new BigDecimal(value).setScale(2,
							BigDecimal.ROUND_HALF_EVEN);
					txt.setText(param.toString());
					if (listener != null)
						listener.valueChanged(value);
				}
			});

			txt.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					// VERY IMPORTANT : otherwise UI crash on value change.
					if (!txt.isFocusControl())
						return;

					String valueStr = txt.getText().trim();
					Double value = null;
					try {
						value = Double.parseDouble(valueStr);
						if (value < minValue || value > maxValue) {
							value = null;
						}
					} catch (NumberFormatException e) {
						// silent, value is null
					}

					// Check boundaries
					if (value == null) {
						Error.show("Value '" + valueStr
								+ "' is not within acceptable range ["
								+ minValue + ", " + maxValue + "]");
						return;
					}
					slider.setSelection(convertToSliderSelection(value));
					if (listener != null)
						listener.valueChanged(value);
				}
			});

		}

		/** Label combined with min / max value */
		protected String getDisplayedLabel() {
			return label + " (" + minValue + "," + maxValue + ")";
		}

		public Double getValue() {
			int sliderValue = slider.getSelection();
			return ((maxValue - minValue) * sliderValue)
					/ (MAX_SLIDER - MIN_SLIDER);
		}

		protected Integer convertToSliderSelection(Double value) {
			return (int) Math
					.round(((double) (value * (MAX_SLIDER - MIN_SLIDER)) / (maxValue - minValue)));
		}

		public void setListener(SliderViewerListener listener) {
			this.listener = listener;
		}

	}

	static interface SliderViewerListener {
		public void valueChanged(Double value);
	}

	/*
	 * GIS
	 */
	protected void addSpeedLayer() {
		// Add speeds layer
		String trackSpeedsPath = getEditor().getTrackDao()
				.getTrackSpeedsSource();
		try {

			Node layerNode = getEditor().getCurrentSessionNode().getSession()
					.getNode(trackSpeedsPath);
			mapViewer.addLayer(layerNode, createToCleanStyle());

			ReferencedEnvelope areaOfInterest = getFeatureSource().getBounds();
			mapViewer.setAreaOfInterest(areaOfInterest);
		} catch (Exception e) {
			throw new ArgeoException("Cannot add layer " + trackSpeedsPath, e);
		}

	}

	protected FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource() {
		String trackSpeedsPath = getEditor().getTrackDao()
				.getTrackSpeedsSource();
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

	protected void refreshSpeedLayer() {
		String trackSpeedsPath = getEditor().getTrackDao()
				.getTrackSpeedsSource();
		mapViewer.setStyle(trackSpeedsPath, createToCleanStyle());
	}

	protected Style createToCleanStyle() {
		Style style = StylingUtils.createFilteredLineStyle(
				getToCleanCqlFilter(), "RED", 3, "BLACK", 1);
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
