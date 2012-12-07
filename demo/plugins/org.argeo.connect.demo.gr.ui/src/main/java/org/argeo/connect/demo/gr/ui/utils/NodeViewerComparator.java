package org.argeo.connect.demo.gr.ui.utils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.ArgeoException;
import org.argeo.eclipse.ui.GenericTableComparator;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provide a comparator for a table that display JCR nodes.
 * 
 * TODO : implement all data types
 * 
 * TODO : move to commons.
 */

public class NodeViewerComparator extends GenericTableComparator {
	private final static Log log = LogFactory
			.getLog(NodeViewerComparator.class);

	private List<String> propertiesList;
	private List<Integer> propertyTypesList;
	private Integer propertyType;
	private String property;

	public NodeViewerComparator(int defaultColIndex, int defaultDirection,
			List<String> propertiesList, List<Integer> propertyTypesList) {
		super(defaultColIndex, defaultDirection);
		this.propertiesList = propertiesList;
		this.propertyTypesList = propertyTypesList;
		this.propertyIndex = defaultColIndex;
		this.propertyType = propertyTypesList.get(defaultColIndex);
		this.property = propertiesList.get(defaultColIndex);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int rc = 0;
		long lc = 0;

		try {

			Node n1 = (Node) e1;
			Node n2 = (Node) e2;

			Value v1 = null;
			Value v2 = null;
			if (n1.hasProperty(property))
				v1 = n1.getProperty(property).getValue();
			if (n2.hasProperty(property))
				v2 = n2.getProperty(property).getValue();

			if (v2 == null && v1 == null)
				return 0;
			else if (v2 == null)
				return 1;
			else if (v1 == null)
				return -1;

			switch (propertyType) {
			case PropertyType.STRING:
				rc = v1.getString().compareTo(v2.getString());
				break;
			case PropertyType.BOOLEAN:
				boolean b1 = v1.getBoolean();
				boolean b2 = v2.getBoolean();
				if (b1 == b2)
					rc = 0;
				else
					// we assume true is greater than false
					rc = b1 ? 1 : -1;
				break;
			case PropertyType.DATE:
				Calendar c1 = v1.getDate();
				Calendar c2 = v2.getDate();
				lc = c1.getTimeInMillis() - c2.getTimeInMillis();
				if (lc < Integer.MIN_VALUE)
					rc = Integer.MIN_VALUE;
				else if (lc > Integer.MAX_VALUE)
					rc = Integer.MAX_VALUE;
				else
					rc = (int) lc;
				break;
			case PropertyType.LONG:
				long l1 = v1.getLong();
				long l2 = v2.getLong();
				lc = l1 - l2;
				if (lc < Integer.MIN_VALUE)
					rc = Integer.MIN_VALUE;
				else if (lc > Integer.MAX_VALUE)
					rc = Integer.MAX_VALUE;
				else
					rc = (int) lc;
				break;
			case PropertyType.DECIMAL:
				BigDecimal bd1 = v1.getDecimal();
				BigDecimal bd2 = v2.getDecimal();
				rc = bd1.compareTo(bd2);
				break;
			case PropertyType.DOUBLE:
				Double d1 = v1.getDouble();
				Double d2 = v2.getDouble();
				rc = d1.compareTo(d2);
				break;
			default:
				throw new ArgeoException(
						"Unimplemented comparaison for PropertyType "
								+ propertyType);
			}

			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}

		} catch (RepositoryException re) {
			throw new ArgeoException("Unexpected error "
					+ "while comparing nodes", re);
		}
		return rc;
	}

	@Override
	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do a descending sort
			this.propertyIndex = column;
			this.propertyType = propertyTypesList.get(column);
			this.property = propertiesList.get(column);
			direction = DESCENDING;
			if (log.isTraceEnabled())
				log.trace("column[" + column + "] set for property: "
						+ property);

		}
	}
}