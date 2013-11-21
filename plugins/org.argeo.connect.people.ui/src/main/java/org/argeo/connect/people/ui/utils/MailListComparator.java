/*
 * Copyright (C) 2007-2012 Argeo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.argeo.connect.people.ui.utils;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Row;

import org.argeo.ArgeoException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class MailListComparator extends ViewerComparator {
	private static final long serialVersionUID = 7621278235801225428L;

	protected String propertyName;
	protected String selectorName;
	protected int propertyType;
	public static final int ASCENDING = 0, DESCENDING = 1;
	protected int direction = DESCENDING;

	public MailListComparator() {
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		int rc = 0;
		long lc = 0;

		try {
			Node n1 = ((Row) e1).getNode(selectorName);
			Node n2 = ((Row) e2).getNode(selectorName);

			// TODO use rowIterator rather than node iterator
			Value v1 = null;
			Value v2 = null;
			if (n1.hasProperty(propertyName))
				v1 = n1.getProperty(propertyName).getValue();
			if (n2.hasProperty(propertyName))
				v2 = n2.getProperty(propertyName).getValue();

			if (v2 == null && v1 == null)
				return 0;
			else if (v2 == null)
				return -1;
			else if (v1 == null)
				return 1;

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
				if (c1 == null || c2 == null)
					// log.trace("undefined date");
					;
				lc = c1.getTimeInMillis() - c2.getTimeInMillis();
				if (lc < Integer.MIN_VALUE)
					// rc = Integer.MIN_VALUE;
					rc = -1;
				else if (lc > Integer.MAX_VALUE)
					// rc = Integer.MAX_VALUE;
					rc = 1;
				else
					rc = (int) lc;
				break;
			case PropertyType.LONG:
				long l1;
				long l2;
				// FIXME sometimes an empty string is set instead of the id
				try {
					l1 = v1.getLong();
				} catch (ValueFormatException ve) {
					l1 = 0;
				}
				try {
					l2 = v2.getLong();
				} catch (ValueFormatException ve) {
					l2 = 0;
				}

				lc = l1 - l2;
				if (lc < Integer.MIN_VALUE)
					// rc = Integer.MIN_VALUE;
					rc = -1;
				else if (lc > Integer.MAX_VALUE)
					// rc = Integer.MAX_VALUE;
					rc = 1;
				else
					rc = (int) lc;
				break;
			case PropertyType.DECIMAL:
				BigDecimal bd1 = v1.getDecimal();
				BigDecimal bd2 = v2.getDecimal();
				rc = bd1.compareTo(bd2);
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
					+ "while comparing rows", re);
		}
		return rc;
	}

	/**
	 * @param propertyType
	 *            Corresponding JCR type
	 * @param selectorName
	 *            for instance PEOPLE_PERSON
	 * @param propertyName
	 *            name of the property to use.
	 */
	public void setColumn(int propertyType, String selectorName,
			String propertyName) {
		if (this.selectorName != null && this.propertyName != null
				&& this.selectorName.equals(selectorName)
				&& this.propertyName.equals(propertyName)) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do a descending sort
			this.propertyType = propertyType;
			this.propertyName = propertyName;
			this.selectorName = selectorName;
			direction = ASCENDING;
		}
	}
}