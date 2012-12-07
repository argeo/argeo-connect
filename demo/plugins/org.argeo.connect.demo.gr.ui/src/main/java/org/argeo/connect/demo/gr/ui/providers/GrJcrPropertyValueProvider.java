package org.argeo.connect.demo.gr.ui.providers;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrConstants;

/**
 * Provides the ability to retrieve a correctly formatted String corresponding
 * of the requested property value depending on the current context and data
 * model only with a property name and a node.
 * 
 * It addresses by instance case when the given property is on a child node or a
 * node referenced by given node
 */
public class GrJcrPropertyValueProvider implements IJcrPropertyLabelProvider {

	private DateFormat dateFormat;
	private NumberFormat numberFormat;

	public GrJcrPropertyValueProvider() {
		dateFormat = new SimpleDateFormat(GrConstants.DATE_FORMAT);
		numberFormat = DecimalFormat.getInstance();
		((DecimalFormat) numberFormat).applyPattern(GrConstants.NUMBER_FORMAT);
	}

	@Override
	public String getFormattedPropertyValue(Node node, String propertyName) {
		try {
			// TODO implement other property type formatting
			if (node.hasProperty(propertyName)
					&& !node.getProperty(propertyName).isMultiple()) {
				Value value = node.getProperty(propertyName).getValue();
				if (value.getType() == PropertyType.DATE)
					return dateFormat.format(value.getDate().getTime());
				else if (value.getType() == PropertyType.DECIMAL)
					return numberFormat.format(value.getDecimal());
				else if (value.getType() == PropertyType.DOUBLE)
					return numberFormat.format(value.getDouble());
				else if (value.getType() == PropertyType.BOOLEAN)
					return "" + value.getBoolean();
				else if (value.getType() == PropertyType.REFERENCE) {
					Node ref = node.getProperty(propertyName).getNode();
					if (ref.hasProperty(Property.JCR_TITLE))
						return ref.getProperty(Property.JCR_TITLE).getString();
					else
						return "";
				} else
					return value.getString();
			} else
				return "";
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while getting property values", re);
		}
	}
}