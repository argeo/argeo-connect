package org.argeo.connect.people.ui.extracts;

import org.argeo.eclipse.ui.jcr.lists.ColumnDefinition;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Experimental class that also bound a label provider to the column definition
 * in order to ease table management in a JCR Context. Might be factorized if it
 * seems relevant after being used for a while.
 */
public class PeopleColumnDefinition extends ColumnDefinition {

	private ColumnLabelProvider labelProvider;

	public PeopleColumnDefinition(String selectorName, String propertyName,
			int propertyType, String headerLabel,
			ColumnLabelProvider labelProvider) {
		super(selectorName, propertyName, propertyType, headerLabel);
		this.labelProvider = labelProvider;
	}

	public PeopleColumnDefinition(String selectorName, String propertyName,
			int propertyType, String headerLabel,
			ColumnLabelProvider labelProvider, int columnSize) {
		super(selectorName, propertyName, propertyType, headerLabel, columnSize);
		this.labelProvider = labelProvider;
	}

	public ColumnLabelProvider getColumnLabelProvider() {
		return labelProvider;
	}

	public void setColumnLabelProvider(ColumnLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

}