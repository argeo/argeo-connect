package org.argeo.gis.ui.rap.openlayers.custom;

import org.polymap.openlayers.rap.widget.layers.Layer;

public class VirtualEarthLayer extends Layer {
	public final static String SHADED = "VEMapStyle.Shaded";
	public final static String HYBRID = "VEMapStyle.Hybrid";
	public final static String AERIAL = "VEMapStyle.Aerial";

	public VirtualEarthLayer(String name, String type, Boolean sphericalMercator) {
		super.setName(name);
		super.create("new OpenLayers.Layer.VirtualEarth({name:'"
				+ name
				+ "' ,type:'"
				+ type
				+ "' ,sphericalMercator:"
				+ sphericalMercator
				+ ",maxExtent: new OpenLayers.Bounds(-20037508.34,-20037508.34,20037508.34,20037508.34)"
				+ "})");

	}
}
