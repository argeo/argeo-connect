package org.argeo.connect.ui.gps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.argeo.ArgeoException;
import org.argeo.connect.gps.TrackSpeed;
import org.argeo.geotools.StylingUtils;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

/** Centralises styling. */
public class GpsStyling {
	// private final static Log log = LogFactory.getLog(GpsStyling.class);

	private static StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	private static FilterFactory ff = CommonFactoryFinder
			.getFilterFactory(null);

	public static Style createGpsCleanStyle(String field, Boolean preview,
			Double maxSpeed, Double maxAbsoluteAcceleration,
			Double maxAbsoluteRotation, Double maxAbsoluteVerticalSpeed) {
		// map filters and colors
		Map<String, String> cqlFilters = new HashMap<String, String>();
		cqlFilters.put("speed>" + maxSpeed, "YELLOW");
		cqlFilters.put("azimuthVariation<" + (-maxAbsoluteRotation)
				+ " OR azimuthVariation>" + maxAbsoluteRotation, "RED");
		cqlFilters.put("verticalSpeed<" + (-maxAbsoluteVerticalSpeed)
				+ " OR verticalSpeed>" + maxAbsoluteVerticalSpeed, "GREEN");
		cqlFilters.put("acceleration<" + (-maxAbsoluteAcceleration)
				+ " OR acceleration>" + maxAbsoluteAcceleration, "BLUE");

		String unmatchedColor = "BLACK";
		Integer matchedWidth = 2;

		Filter unmatchedFilter = null;
		if (preview) {
			List<Filter> filters = new ArrayList<Filter>();
			for (String cqlFilter : cqlFilters.keySet()) {
				Filter filter;
				try {
					filter = CQL.toFilter(cqlFilter);
					filters.add(ff.not(filter));
				} catch (CQLException e) {
					throw new ArgeoException("Cannot parse CQL filter: "
							+ cqlFilter, e);
				}
			}
			unmatchedFilter = ff.and(filters);

		}

		List<Rule> rules = new ArrayList<Rule>();
		// unmatched
		Rule ruleUnMatched = null;
		if (field.equals(TrackSpeed.LINE)) {
			Integer unmatchedWidth = 1;
			ruleUnMatched = sf.createRule();
			ruleUnMatched.symbolizers().add(
					StylingUtils.createLineSymbolizer(unmatchedColor,
							unmatchedWidth != null ? unmatchedWidth
									: matchedWidth));
			rules.add(ruleUnMatched);
		} else if (field.equals(TrackSpeed.POSITION)) {
			Integer unmatchedWidth = 1;
			ruleUnMatched = sf.createRule();
			ruleUnMatched.symbolizers().add(
					StylingUtils.createPointSymbolizer(sf.getSquareMark(),
							unmatchedColor, unmatchedWidth, null, null));
			rules.add(ruleUnMatched);
		}

		if (unmatchedFilter != null && ruleUnMatched != null)
			ruleUnMatched.setFilter(unmatchedFilter);

		if (!preview) {

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

				if (field.equals(TrackSpeed.LINE)) {
					// matched line
					Rule ruleMatched = sf.createRule();
					Stroke stroke = sf.createStroke(
							ff.literal(stringToColor(matchedColor)),
							ff.literal(matchedWidth));
					ruleMatched.symbolizers().add(
							sf.createLineSymbolizer(stroke, field));
					ruleMatched.setFilter(filter);
					rules.add(ruleMatched);
				} else if (field.equals(TrackSpeed.POSITION)) {
					Integer markWidth = 7;
					// matched point
					Graphic gr = sf.createDefaultGraphic();
					Mark mark = sf.getCrossMark();
					mark.setStroke(sf.createStroke(
							ff.literal(stringToColor(matchedColor)),
							ff.literal(1)));
					// mark.setFill(sf.createFill(ff
					// .literal(stringToColor(matchedColor))));
					gr.graphicalSymbols().clear();
					gr.graphicalSymbols().add(mark);
					gr.setSize(ff.literal(markWidth));
					PointSymbolizer sym = sf.createPointSymbolizer(gr, field);
					Rule ruleMatchedPt = sf.createRule();
					ruleMatchedPt.symbolizers().add(sym);
					ruleMatchedPt.setFilter(filter);
					rules.add(ruleMatchedPt);
				}
			}
		}

		FeatureTypeStyle fts = sf.createFeatureTypeStyle(rules
				.toArray(new Rule[rules.size()]));
		Style style = sf.createStyle();
		style.featureTypeStyles().add(fts);
		return style;

	}

	public static PointSymbolizer createPointSymbolizer(Mark mark,
			String fillColor, Integer width, String strokeColor,
			Integer strokeWidth) {
		Graphic gr = sf.createDefaultGraphic();

		if (strokeColor != null)
			mark.setStroke(sf.createStroke(
					ff.literal(stringToColor(strokeColor)),
					ff.literal(strokeWidth)));

		mark.setFill(sf.createFill(ff.literal(stringToColor(fillColor))));

		gr.graphicalSymbols().clear();
		gr.graphicalSymbols().add(mark);
		gr.setSize(ff.literal(width));

		// Setting the geometryPropertyName arg to null signals that we want to
		// draw the default geometry of features
		PointSymbolizer sym = sf.createPointSymbolizer(gr, null);
		return sym;
	}

	static Color stringToColor(String color) {
		try {
			return (Color) Color.class.getField(color).get(null);
		} catch (Exception e) {
			throw new ArgeoException("Color " + color + " not found", e);
		}
	}

}
