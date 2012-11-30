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
package org.argeo.connect.ui.gps.commons;

import java.math.BigDecimal;

import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.ErrorFeedback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/** TODO factorize it in Argeo Commons as a generic component ? */
public class SliderViewer {
	private final static Integer MIN_SLIDER = 0;
	// Add a -10 offset to MAX_SLIDER before computing the value to workaround a
	// bug of the slider widget (max value is not reachable while using the mouse)
	private final static Integer MAX_SLIDER = 110;
	private final static Integer MAX_OFFSET = 10;

	private String label;
	private Double maxValue;
	private Double minValue;
	private Double defaultValue;

	private FormToolkit formToolkit;
	private Slider slider;
	private Text txt;

	/** TODO multiple listeners */
	private SliderViewerListener listener;

	public SliderViewer(FormToolkit formToolkit, Composite parent,
			String label, Double minValue, Double maxValue, Double defaultValue) {
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
		formToolkit.createLabel(parent, getDisplayedLabel());

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
					ErrorFeedback.show("Value '" + valueStr
							+ "' is not within acceptable range [" + minValue
							+ ", " + maxValue + "]");
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
		return label + "  [" + minValue + "," + maxValue + "]";
	}

	public Double getValue() {
		int sliderValue = slider.getSelection();
		return ((maxValue - minValue) * sliderValue)
				/ (MAX_SLIDER - MIN_SLIDER - MAX_OFFSET);
	}

	protected Integer convertToSliderSelection(Double value) {
		return (int) Math
				.round(((double) (value * (MAX_SLIDER - MIN_SLIDER)) / (maxValue - minValue)));
	}

	public void setListener(SliderViewerListener listener) {
		this.listener = listener;
	}

}