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
