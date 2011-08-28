package org.argeo.gis.ui.rap.openlayers.custom;

import java.util.Iterator;
import java.util.Map;

import org.polymap.openlayers.rap.widget.base_types.StyleMap;

public class AdvancedStyleMap extends StyleMap {
    public AdvancedStyleMap() {
       // super.create("new OpenLayers.StyleMap( {fillOpacity:1, pointRadius:10} );");
    }

    public void addUniqueValueRules(String intent, String type,
			Map<String, String> lookup) {
		StringBuffer bufLookup = new StringBuffer("{");
		Iterator<String> keys = lookup.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			bufLookup.append("\"").append(key).append("\"")
					.append(":{externalGraphic:\"").append(lookup.get(key))
					.append("\"}");
			if (keys.hasNext())
				bufLookup.append(',');
		}
		bufLookup.append("}");
		addObjModCode("var lookup=" + bufLookup + ";obj.addUniqueValueRules(\""
				+ intent + "\", \"" + type + "\", lookup);");
	}
}
