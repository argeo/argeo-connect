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
package org.argeo.geotools;

import java.awt.Color;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.argeo.ArgeoException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/** Utilities related to GeoTools styling */
public class StylingUtils {
	static StyleFactory styleFactory = CommonFactoryFinder
			.getStyleFactory(null);
	static FilterFactory filterFactory = CommonFactoryFinder
			.getFilterFactory(null);

	/**
	 * Style for a line
	 * 
	 * @param color
	 *            the AWT color in upper case
	 * @param width
	 *            the width of the line
	 * @param cqlFilter
	 *            filter in CQL format restricting the feature upon which the
	 *            style will apply
	 */
	public static Style createLineStyle(String color, Integer width) {
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(createLineSymbolizer(color, width));
		FeatureTypeStyle fts = styleFactory
				.createFeatureTypeStyle(new Rule[] { rule });
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}

	/**
	 * @param markWellKnownName
	 *            Square, Circle, Cross, X, Triangle, Star
	 * @param strokeColor
	 *            can be null to indicate no stroke
	 */
	public static Style createPointStyle(String markWellKnownName,
			String fillColor, Integer width, String strokeColor,
			Integer strokeWidth) {
		Mark mark = styleFactory.getDefaultMark();
		mark.setWellKnownName(filterFactory.literal(markWellKnownName));
		PointSymbolizer symb = createPointSymbolizer(mark, fillColor, width,
				strokeColor, strokeWidth);
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(symb);
		FeatureTypeStyle fts = styleFactory
				.createFeatureTypeStyle(new Rule[] { rule });
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}

	public static Style createImagePointStyle(URL url) {
		StyleBuilder styleBuilder = new StyleBuilder(styleFactory);
		Style style = styleBuilder.createStyle();
		PointSymbolizer pointSymbolizer = styleBuilder.createPointSymbolizer();

		Graphic graphic = styleBuilder.createGraphic();
		String ext = FilenameUtils.getExtension(url.getFile()).toLowerCase();
		String mimeType = null;
		if ("gif".equals(ext))
			mimeType = "image/gif";
		else if ("png".equals(ext))
			mimeType = "image/gif";
		else if ("jpg".equals(ext) || "jpeg".equals(ext))
			mimeType = "image/gif";

		ExternalGraphic external = styleBuilder.createExternalGraphic(url,
				mimeType);
		graphic.graphicalSymbols().add(external);
		graphic.graphicalSymbols().add(styleBuilder.createMark("circle"));

		pointSymbolizer.setGraphic(graphic);
		Rule rule = styleBuilder.createRule(pointSymbolizer);
		FeatureTypeStyle featureTypeStyle = styleBuilder
				.createFeatureTypeStyle("Feature", rule);
		style.featureTypeStyles().add(featureTypeStyle);
		return style;
	}

	public static Style createFilteredLineStyle(String cqlFilter,
			String matchedColor, Integer matchedWidth, String unmatchedColor,
			Integer unmatchedWidth) {
		Map<String, String> cqlFilters = new HashMap<String, String>();
		cqlFilters.put(cqlFilter, matchedColor);
		return createFilteredLineStyle(cqlFilters, matchedWidth,
				unmatchedColor, unmatchedWidth);
	}

	public static Style createFilteredLineStyle(Map<String, String> cqlFilters,
			Integer matchedWidth, String unmatchedColor, Integer unmatchedWidth) {
		// unmatched
		Rule ruleUnMatched = null;
		if (unmatchedColor != null) {
			ruleUnMatched = styleFactory.createRule();
			ruleUnMatched.symbolizers().add(
					createLineSymbolizer(unmatchedColor,
							unmatchedWidth != null ? unmatchedWidth
									: matchedWidth));
			// ruleUnMatched.setFilter(filterFactory.not(filter));
		}

		List<Rule> rules = new ArrayList<Rule>();
		// unmatched
		if (ruleUnMatched != null) {
			rules.add(ruleUnMatched);
		}

		for (String cqlFilter : cqlFilters.keySet()) {

			String matchedColor = cqlFilters.get(cqlFilter);

			// selection filter
			Filter filter;
			try {
				filter = CQL.toFilter(cqlFilter);
			} catch (CQLException e) {
				throw new ArgeoException("Cannot parse CQL filter: "
						+ cqlFilter, e);
			}

			// matched
			Rule ruleMatched = styleFactory.createRule();
			ruleMatched.symbolizers().add(
					createLineSymbolizer(matchedColor, matchedWidth));
			ruleMatched.setFilter(filter);

			rules.add(ruleMatched);
		}

		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(rules
				.toArray(new Rule[rules.size()]));
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}

	public static LineSymbolizer createLineSymbolizer(String color,
			Integer width) {
		Stroke stroke = styleFactory.createStroke(
				filterFactory.literal(stringToColor(color)),
				filterFactory.literal(width));
		return styleFactory.createLineSymbolizer(stroke, null);
	}

	public static PointSymbolizer createPointSymbolizer(Mark mark,
			String fillColor, Integer width, String strokeColor,
			Integer strokeWidth) {
		Graphic gr = styleFactory.createDefaultGraphic();

		if (strokeColor != null)
			mark.setStroke(styleFactory.createStroke(
					filterFactory.literal(stringToColor(strokeColor)),
					filterFactory.literal(strokeWidth)));

		mark.setFill(styleFactory.createFill(filterFactory
				.literal(stringToColor(fillColor))));

		gr.graphicalSymbols().clear();
		gr.graphicalSymbols().add(mark);
		gr.setSize(filterFactory.literal(width));

		// Setting the geometryPropertyName arg to null signals that we want to
		// draw the default geometry of features
		PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);
		return sym;
	}

	/** Write a {@link Style} in the standard SLC XML format. */
	public void writeStyleAsSld(Style style, OutputStream out) {
		SLDTransformer sldTransformer = new SLDTransformer();
		try {
			sldTransformer.transform(style, out);
		} catch (TransformerException e) {
			throw new ArgeoException("Cannot write SLD", e);
		}
	}

	/**
	 * Converts a string to a color, using reflection, so that other methods
	 * don't need AWT dependencies in their signature. Package protected and not
	 * public so that it has less impact on the overall signature.
	 */
	static Color stringToColor(String color) {
		try {
			return (Color) Color.class.getField(color).get(null);
		} catch (Exception e) {
			throw new ArgeoException("Color " + color + " not found", e);
		}
	}
}
