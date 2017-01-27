package org.argeo.connect.people.rap.util;

import java.util.HashMap;
import java.util.Map;

import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

/**
 * Used to manage the checkout state of a node for various ui editors.
 * 
 * TODO: check this to simplify: http://wiki.eclipse.org/RCP_FAQ#
 * How_can_I_get_my_views_and_editors_to_coordinate_with_each_other.3F
 */
public class CheckoutSourceProvider extends AbstractSourceProvider {
	public final static String CHECKOUT_STATE = PeopleRapPlugin.PLUGIN_ID
			+ ".checkOutState";
	private final static String CHECKED_OUT = "checkedOut";
	private final static String NOT_CHECKED_OUT = "notCheckedOut";
	boolean isCheckedOut = true;

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { CHECKOUT_STATE };
	}

	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> currentState = new HashMap<String, String>(1);
		String checkoutState = isCheckedOut ? CHECKED_OUT : NOT_CHECKED_OUT;
		currentState.put(CHECKOUT_STATE, checkoutState);
		return currentState;
	}

	@Override
	public void dispose() {
	}

	/**
	 * Notify the framework that the check out state has been updated, so that
	 * it can disable or enable check out button when relevant item is
	 * respectively checked-out or checked in..
	 */
	public void setIsCurrentItemCheckedOut(boolean isCheckedOut) {
		this.isCheckedOut = isCheckedOut;
		String checkoutState = isCheckedOut ? CHECKED_OUT : NOT_CHECKED_OUT;
		fireSourceChanged(ISources.WORKBENCH, CHECKOUT_STATE, checkoutState);
	}
}