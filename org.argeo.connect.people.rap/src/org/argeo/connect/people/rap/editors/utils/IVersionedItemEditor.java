package org.argeo.connect.people.rap.editors.utils;

/** Enable centralisation of versioned items check-out and in management */
public interface IVersionedItemEditor {
	/** Processing the ability to checkout is delegated to the editor */
	public boolean isCheckedOutByMe();

	/** Processing the ability to checkout is delegated to the editor */
	public boolean canBeCheckedOutByMe();

	/** Checkout is delegated to the editor */
	public void checkoutItem();

	/** Cancel pending changes and check back in */
	public void cancelAndCheckInItem();

	/** Cancel pending changes and check back in */
	public void saveAndCheckInItem();

	/**
	 * Retrieves the "last updated" by information
	 * 
	 * @return a message that should look like
	 *         "Paul Durand, last updated on Wed, 22 Jan at 17:24 by demo."
	 */
	public String getlastUpdateMessage();
}