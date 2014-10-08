package org.argeo.connect.people.rap.utils;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;

/** Some static utils methods that might be factorized in a near future */
public class JcrUiUtils {

	/**
	 * Centralizes management of updating property value. Among other to avoid
	 * infinite loop when the new value is the same as the ones that is already
	 * stored in JCR.
	 * 
	 * Rather use {@link CommonsJcrUtils.setJcrProperty(}
	 * 
	 * @return true if the value as changed
	 * 
	 * 
	 */
	@Deprecated
	public static boolean setJcrProperty(Node node, String propName,
			int propertyType, Object value) {
		try {
			// int propertyType = getPic().getProperty(propName).getType();
			switch (propertyType) {
			case PropertyType.STRING:
				if ("".equals((String) value)
						&& (!node.hasProperty(propName) || node
								.hasProperty(propName)
								&& "".equals(node.getProperty(propName)
										.getString())))
					// workaround the fact that the Text widget value cannot be
					// set to null
					return false;
				else if (node.hasProperty(propName)
						&& node.getProperty(propName).getString()
								.equals((String) value))
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (String) value);
					return true;
				}
			case PropertyType.BOOLEAN:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getBoolean() == (Boolean) value)
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (Boolean) value);
					return true;
				}
			case PropertyType.DATE:
				if (node.hasProperty(propName)
						&& node.getProperty(propName).getDate()
								.equals((Calendar) value))
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, (Calendar) value);
					return true;
				}
			case PropertyType.LONG:
				Long lgValue = (Long) value;

				if (lgValue == null)
					lgValue = 0L;

				if (node.hasProperty(propName)
						&& node.getProperty(propName).getLong() == lgValue)
					// nothing changed yet
					return false;
				else {
					node.setProperty(propName, lgValue);
					return true;
				}

			default:
				throw new ArgeoException("Unimplemented property save");
			}
		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error while setting property",
					re);
		}
	}
}
