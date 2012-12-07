package org.argeo.connect.demo.gr.ui.exports;

import javax.jcr.Property;

import org.argeo.connect.demo.gr.GrNames;
import org.argeo.connect.demo.gr.ui.GrMessages;

/**
 * utility to map JCR property names to the corresponding internationalized
 * label in current context
 */
public class CalcExtractHeaderLabelProvider {

	public static String getLabelFromPropertyName(String propName) {
		String label = propName;
		if (GrNames.GR_UUID.equals(propName))
			label = "UID";
		else if (GrNames.GR_ECOLI_RATE.equals(propName))
			label = GrMessages.get().eColiRateLbl;
		else if (GrNames.GR_WATER_LEVEL.equals(propName))
			label = GrMessages.get().waterLevelLbl;
		else if (GrNames.GR_WITHDRAWN_WATER.equals(propName))
			label = GrMessages.get().withdrawnWaterLbl;
		else if (Property.JCR_TITLE.equals(propName))
			label = GrMessages.get().name_lbl;
		return label;
	}
}