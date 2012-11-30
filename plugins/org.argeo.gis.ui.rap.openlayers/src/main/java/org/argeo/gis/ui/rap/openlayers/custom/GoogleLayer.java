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
package org.argeo.gis.ui.rap.openlayers.custom;

import org.polymap.openlayers.rap.widget.layers.Layer;

public class GoogleLayer extends Layer {
	public final static String G_PHYSICAL_MAP = "G_PHYSICAL_MAP";
	public final static String G_HYBRID_MAP = "G_HYBRID_MAP";
	public final static String G_SATELLITE_MAP = "G_SATELLITE_MAP";

	/** default (street) used if no type specified */
	public GoogleLayer(String name, String type) {
		super.setName(name);

		if (type == null)
			type = "";

		if (type.equals(G_PHYSICAL_MAP))
			super.create("new OpenLayers.Layer.Google( '" + name
					+ "',{'sphericalMercator': true,"
					+ " numZoomLevels: 20,type: G_PHYSICAL_MAP})");
		else if (type.equals(G_HYBRID_MAP))
			super.create("new OpenLayers.Layer.Google( '" + name
					+ "',{'sphericalMercator': true,"
					+ " numZoomLevels: 20,type: G_HYBRID_MAP})");
		else if (type.equals(G_SATELLITE_MAP))
			super.create("new OpenLayers.Layer.Google( '" + name
					+ "',{'sphericalMercator': true,"
					+ " numZoomLevels: 22,type: G_SATELLITE_MAP})");
		else
			super.create("new OpenLayers.Layer.Google( '" + name
					+ "',{'sphericalMercator': true," + " numZoomLevels: 22})");
	}
}
