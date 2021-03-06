package org.argeo.connect;

/** Provide assembly of the various {@code AppMaintenanceService} of a system */
public interface SystemMaintenanceService extends AppMaintenanceService {
	@Override
	default String getDefaultBasePath() {
		throw new UnsupportedOperationException();
	}
}
