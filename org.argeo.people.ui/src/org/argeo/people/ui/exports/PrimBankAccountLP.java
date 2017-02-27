package org.argeo.people.ui.exports;

import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import javax.jcr.Node;

import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.jcr.lists.SimpleJcrNodeLabelProvider;
import org.argeo.people.util.OrgJcrUtils;

/**
 * Provides Bank Account information given a row that contains a selector that
 * has such an account
 */
public class PrimBankAccountLP extends SimpleJcrNodeLabelProvider {
	private static final long serialVersionUID = 1L;

	private String selectorName;

	public PrimBankAccountLP(String selectorName, String propertyName) {
		super(propertyName);
		if (notEmpty(selectorName))
			this.selectorName = selectorName;
	}

	@Override
	public String getText(Object element) {
		Node currNode = ConnectJcrUtils.getNodeFromElement(element,
				selectorName);
		// Get corresponding Primary Bank Account
		Node bankAccount = OrgJcrUtils.getPrimaryPaymentAccount(currNode);
		if (bankAccount == null)
			return "";
		else
			return super.getText(bankAccount);
	}
}
