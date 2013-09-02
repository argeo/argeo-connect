package org.argeo.connect.people.ui.editors;

/** Enable centralisation of versioned items check-out and in management */
public interface IVersionedItemEditor {

	/** Processing the ability to checkout is delegated to the editor */
	public boolean canBeCheckedOutByMe();

	/** Checkout is delegated to the editor */
	public void checkoutItem();

	/** Cancel pending changes and check back in */
	public void cancelAndCheckInItem();
}