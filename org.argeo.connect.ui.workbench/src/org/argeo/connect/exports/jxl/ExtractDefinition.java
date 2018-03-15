package org.argeo.connect.exports.jxl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeType;

import org.argeo.eclipse.ui.jcr.lists.JcrColumnDefinition;

public interface ExtractDefinition {

	public final static List<JcrColumnDefinition> EXTRACT_SIMPLE_MAILING_LIST = new ArrayList<JcrColumnDefinition>() {
		private static final long serialVersionUID = 1L;
		{
			add(new JcrColumnDefinition(NodeType.MIX_TITLE, Property.JCR_TITLE, PropertyType.STRING, "Display name"));
		}
	};

	// TIP: create a List in one line
	// List<String> list3 = new ArrayList<String>(
	// Arrays.asList("String A", "String B", "String C")
	// );
}
