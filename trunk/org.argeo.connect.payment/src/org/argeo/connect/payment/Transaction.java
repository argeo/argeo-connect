package org.argeo.connect.payment;

public interface Transaction {
	public Object getBusinessId();

	public Counterparty getSource();

	public Counterparty getTarget();
}
