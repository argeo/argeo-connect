package org.argeo.connect.people.ui.providers;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a label provider for group members
 */
public class SimpleJcrRowLabelProvider extends ColumnLabelProvider implements
		PeopleNames {
	private static final long serialVersionUID = 9156065705311297011L;

	final private String propertyName;
	final private String selectorName;
	// final private int propertyType;

	DateFormat dateFormat;
	NumberFormat numberFormat;

	public SimpleJcrRowLabelProvider(String selectorName, String propertyName) { // ,
																					// int
		// propertyType) {
		this.propertyName = propertyName;
		this.selectorName = selectorName;
		// this.propertyType = propertyType;
		dateFormat = new SimpleDateFormat(PeopleUiConstants.DEFAULT_DATE_FORMAT);
		numberFormat = DecimalFormat.getInstance();
		((DecimalFormat) numberFormat)
				.applyPattern(PeopleUiConstants.DEFAULT_NUMBER_FORMAT);
	}

	@Override
	public String getText(Object element) {
		try {
			Row currRow = (Row) element;
			Node currNode = currRow.getNode(selectorName);

			Value value = null;
			try {
				value = currNode.getProperty(propertyName).getValue();
			} catch (ItemNotFoundException infe) {
				return "";
			}

			if (value.getType() == PropertyType.DATE)
				return dateFormat.format(value.getDate().getTime());
			else if (value.getType() == PropertyType.DECIMAL)
				return numberFormat.format(value.getDecimal());
			else if (value.getType() == PropertyType.BOOLEAN)
				return "" + value.getBoolean();
			else if (value.getType() == PropertyType.REFERENCE) {
				// TODO implement this
				return "";
			} else
				return value.getString();
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to get text from row", re);
		}
	}
}